package nl.rhaydus.softcover.core.connectivity.data.sync

import kotlinx.coroutines.CancellationException
import kotlinx.datetime.LocalDate
import nl.rhaydus.common.AppLog
import nl.rhaydus.offlinesync.ReplayOutcome
import nl.rhaydus.softcover.core.book.data.datasource.BooksRemoteDataSource
import nl.rhaydus.softcover.core.book.data.datasource.ProgressHistoryLocalDataSource
import nl.rhaydus.softcover.core.database.mapper.reviewDocumentFromJson
import nl.rhaydus.softcover.core.domain.connectivity.PendingUserBookWrite
import nl.rhaydus.softcover.core.domain.connectivity.PendingUserBookWriteKind
import nl.rhaydus.softcover.core.domain.connectivity.UserBookWriteLock
import nl.rhaydus.softcover.core.domain.connectivity.UserBookWriteQueue
import nl.rhaydus.softcover.core.domain.exception.RetryableSyncException
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.domain.model.canonical
import nl.rhaydus.softcover.core.domain.util.localDateOf
import kotlin.math.abs

/**
 * Replays a single queued user-book mutation. [ReplayOutcome.SYNCED] means the mutation reached the server
 * and the affected book was genuinely updated; [ReplayOutcome.DISCARDED] means the payload was
 * unprocessable (a missing field) and the row should be dropped without counting as a server-side sync.
 *
 * A thrown exception is the drainer's business: a [nl.rhaydus.softcover.core.domain.exception.RetryableSyncException]
 * is transient (the write never got a processable response) and halts the pass for a later retry; anything
 * else means the server processed and rejected the payload, so replaying it would fail identically.
 */
internal class UserBookWriteReplay(
    private val booksRemoteDataSource: BooksRemoteDataSource,
    private val progressHistoryLocalDataSource: ProgressHistoryLocalDataSource,
    private val writeLock: UserBookWriteLock,
    private val queue: UserBookWriteQueue,
) {
    // A stale row means a direct send already settled or replaced it, so replaying would double-send.
    suspend operator fun invoke(payload: PendingUserBookWrite): ReplayOutcome = writeLock.withLock(bookId = payload.bookId) {
        if (queue.isCurrent(write = payload)) {
            replayCurrent(payload = payload)
        } else {
            ReplayOutcome.DISCARDED
        }
    }

    private suspend fun replayCurrent(payload: PendingUserBookWrite): ReplayOutcome = when (payload.kind) {
        PendingUserBookWriteKind.UPDATE_PROGRESS -> replayProgress(payload = payload)

        PendingUserBookWriteKind.MARK_AS_READ -> {
            if (isMarkAsReadApplied(payload = payload)) return ReplayOutcome.SYNCED

            booksRemoteDataSource.replayMarkBookAsRead(
                bookId = payload.bookId,
                editionId = payload.editionId,
                userDate = localDateOf(actionAt = payload.enqueuedAt),
                actionAt = payload.actionAt,
            )

            ReplayOutcome.SYNCED
        }

        PendingUserBookWriteKind.UPDATE_RATING -> replayRating(payload = payload)

        PendingUserBookWriteKind.UPDATE_REVIEW -> replayReview(payload = payload)

        PendingUserBookWriteKind.SET_STATUS -> replayStatus(payload = payload)
    }

    // A rejected write is dropped by the drainer, so its history row must go too or it would stay pending forever.
    private suspend fun replayProgress(payload: PendingUserBookWrite): ReplayOutcome {
        val replayed: Boolean = runCatching {
            val isPlaceholderRead = payload.userBookReadId < 0

            // A negative read id is a local placeholder; the server read it stands for only exists by now.
            val unfinishedRead = if (isPlaceholderRead) {
                booksRemoteDataSource.fetchLatestUnfinishedRead(userBookId = payload.userBookId)
                    ?: return@runCatching false
            } else {
                null
            }

            val readId = unfinishedRead?.id ?: payload.userBookReadId

            val isApplied = isProgressApplied(
                payload = payload,
                readId = readId,
            )

            if (isApplied) return@runCatching true

            booksRemoteDataSource.replayUpdateBookProgress(
                userBookReadId = readId,
                editionId = payload.editionId,
                progressPages = payload.progressPages,
                progressSeconds = payload.progressSeconds,
                startedAt = if (unfinishedRead != null) unfinishedRead.startedAt else payload.startedAt,
                finishedAt = if (unfinishedRead != null) null else payload.finishedAt,
                actionAt = payload.actionAt,
            )

            true
        }.onFailure { error ->
            val isRejection = error !is CancellationException && error !is RetryableSyncException

            if (isRejection) {
                payload.progressHistoryId?.let { progressHistoryLocalDataSource.delete(localId = it) }
            }
        }.getOrThrow()

        if (replayed.not()) {
            AppLog.w("Pending progress for user-book ${payload.userBookId} has no unfinished read to land on; discarding")

            payload.progressHistoryId?.let { progressHistoryLocalDataSource.delete(localId = it) }

            return ReplayOutcome.DISCARDED
        }

        payload.progressHistoryId?.let { progressHistoryLocalDataSource.markSynced(localId = it) }

        return ReplayOutcome.SYNCED
    }

    private suspend fun replayRating(payload: PendingUserBookWrite): ReplayOutcome {
        val rating: Double = payload.rating ?: run {
            AppLog.w("Pending rating update for user-book ${payload.userBookId} has no rating; discarding")

            return ReplayOutcome.DISCARDED
        }

        if (booksRemoteDataSource.fetchUserBookState(bookId = payload.bookId)?.rating == rating) {
            return ReplayOutcome.SYNCED
        }

        booksRemoteDataSource.replayUpdateBookRating(
            userBookId = payload.userBookId,
            rating = rating,
        )

        return ReplayOutcome.SYNCED
    }

    private suspend fun replayStatus(payload: PendingUserBookWrite): ReplayOutcome {
        val statusCode: Int = payload.statusCode ?: run {
            AppLog.w("Pending status update for user-book ${payload.userBookId} has no status; discarding")

            return ReplayOutcome.DISCARDED
        }

        if (booksRemoteDataSource.fetchUserBookState(bookId = payload.bookId)?.statusCode == statusCode) {
            return ReplayOutcome.SYNCED
        }

        booksRemoteDataSource.replayUpdateUserBookStatus(
            userBookId = payload.userBookId,
            statusCode = statusCode,
            userDate = localDateOf(actionAt = payload.enqueuedAt),
        )

        return ReplayOutcome.SYNCED
    }

    private suspend fun replayReview(payload: PendingUserBookWrite): ReplayOutcome {
        val document = reviewDocumentFromJson(json = payload.reviewSlateJson) ?: run {
            AppLog.w("Pending review update for user-book ${payload.userBookId} has no body; discarding")

            return ReplayOutcome.DISCARDED
        }

        val hasSpoilers: Boolean = payload.reviewHasSpoilers ?: false

        val serverState = booksRemoteDataSource.fetchUserBookState(bookId = payload.bookId)

        val isApplied: Boolean = serverState != null &&
            serverState.reviewHasSpoilers == hasSpoilers &&
            serverState.reviewDocument?.canonical() == document.canonical()

        if (isApplied) return ReplayOutcome.SYNCED

        booksRemoteDataSource.replayUpdateBookReview(
            userBookId = payload.userBookId,
            review = document,
            hasSpoilers = hasSpoilers,
            reviewedAt = localDateOf(actionAt = payload.enqueuedAt),
        )

        return ReplayOutcome.SYNCED
    }

    private suspend fun isProgressApplied(
        payload: PendingUserBookWrite,
        readId: Int,
    ): Boolean {
        val read = booksRemoteDataSource.fetchUserBookState(bookId = payload.bookId)
            ?.reads
            ?.firstOrNull { it.id == readId }
            ?: return false

        return if (payload.progressSeconds != null) {
            read.progressSeconds == payload.progressSeconds
        } else {
            payload.progressPages != null && read.progressPages == payload.progressPages
        }
    }

    private suspend fun isMarkAsReadApplied(payload: PendingUserBookWrite): Boolean {
        val state = booksRemoteDataSource.fetchUserBookState(bookId = payload.bookId) ?: return false

        val expected: LocalDate = runCatching {
            LocalDate.parse(localDateOf(actionAt = payload.actionAt ?: payload.enqueuedAt))
        }.getOrNull() ?: return false

        return state.statusCode == UserBookStatus.READ.code && state.reads.any { read ->
            val finishedOn: LocalDate = read.finishedAt
                ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
                ?: return@any false

            abs(finishedOn.toEpochDays() - expected.toEpochDays()) <= 1
        }
    }
}
