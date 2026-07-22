package nl.rhaydus.softcover.core.connectivity.data.sync

import nl.rhaydus.common.AppLog
import nl.rhaydus.offlinesync.ReplayOutcome
import nl.rhaydus.softcover.core.book.data.datasource.BooksRemoteDataSource
import nl.rhaydus.softcover.core.database.mapper.reviewDocumentFromJson
import nl.rhaydus.softcover.core.domain.connectivity.PendingUserBookWrite
import nl.rhaydus.softcover.core.domain.connectivity.PendingUserBookWriteKind

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
) {
    suspend operator fun invoke(payload: PendingUserBookWrite): ReplayOutcome = when (payload.kind) {
        PendingUserBookWriteKind.UPDATE_PROGRESS -> {
            booksRemoteDataSource.replayUpdateBookProgress(
                userBookReadId = payload.userBookReadId,
                editionId = payload.editionId,
                progressPages = payload.progressPages,
                progressSeconds = payload.progressSeconds,
                startedAt = payload.startedAt,
                finishedAt = payload.finishedAt,
                actionAt = payload.actionAt,
            )

            ReplayOutcome.SYNCED
        }

        PendingUserBookWriteKind.MARK_AS_READ -> {
            booksRemoteDataSource.replayMarkBookAsRead(
                bookId = payload.bookId,
                userDate = payload.enqueuedAt.substringBefore('T'),
                actionAt = payload.actionAt,
            )

            ReplayOutcome.SYNCED
        }

        PendingUserBookWriteKind.UPDATE_RATING -> replayRating(payload = payload)

        PendingUserBookWriteKind.UPDATE_REVIEW -> replayReview(payload = payload)
    }

    private suspend fun replayRating(payload: PendingUserBookWrite): ReplayOutcome {
        val rating: Double = payload.rating ?: run {
            AppLog.w("Pending rating update for user-book ${payload.userBookId} has no rating; discarding")

            return ReplayOutcome.DISCARDED
        }

        booksRemoteDataSource.replayUpdateBookRating(
            userBookId = payload.userBookId,
            rating = rating,
        )

        return ReplayOutcome.SYNCED
    }

    private suspend fun replayReview(payload: PendingUserBookWrite): ReplayOutcome {
        val document = reviewDocumentFromJson(json = payload.reviewSlateJson) ?: run {
            AppLog.w("Pending review update for user-book ${payload.userBookId} has no body; discarding")

            return ReplayOutcome.DISCARDED
        }

        booksRemoteDataSource.replayUpdateBookReview(
            userBookId = payload.userBookId,
            review = document,
            hasSpoilers = payload.reviewHasSpoilers ?: false,
            reviewedAt = payload.enqueuedAt.substringBefore('T'),
        )

        return ReplayOutcome.SYNCED
    }
}
