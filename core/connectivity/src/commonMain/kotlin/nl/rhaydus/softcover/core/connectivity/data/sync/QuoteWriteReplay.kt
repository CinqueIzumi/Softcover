package nl.rhaydus.softcover.core.connectivity.data.sync

import kotlinx.coroutines.CancellationException
import nl.rhaydus.common.AppLog
import nl.rhaydus.offlinesync.ReplayOutcome
import nl.rhaydus.softcover.core.database.model.QuoteEntity
import nl.rhaydus.softcover.core.database.model.QuoteSyncState
import nl.rhaydus.softcover.core.domain.connectivity.PendingQuoteWrite
import nl.rhaydus.softcover.core.domain.connectivity.PendingQuoteWriteKind
import nl.rhaydus.softcover.core.domain.exception.RetryableSyncException
import nl.rhaydus.softcover.core.personal.data.datasource.QuoteLocalDataSource
import nl.rhaydus.softcover.core.personal.data.datasource.QuoteRemoteDataSource

/**
 * Replays a single queued quote write. A [RetryableSyncException] is transient and halts the pass; any other
 * failure means the server processed and rejected the write, so the drainer drops it and this undoes the
 * optimistic local change first: a rejected insert deletes the quote row, a rejected delete restores it.
 */
internal class QuoteWriteReplay(
    private val remoteDataSource: QuoteRemoteDataSource,
    private val localDataSource: QuoteLocalDataSource,
) {
    suspend operator fun invoke(payload: PendingQuoteWrite): ReplayOutcome {
        val quote: QuoteEntity = localDataSource.getById(localId = payload.quoteLocalId)
            ?: return ReplayOutcome.DISCARDED

        return when (payload.kind) {
            PendingQuoteWriteKind.INSERT -> replayInsert(
                quote = quote,
                isRetry = payload.attempts > 0,
            )

            PendingQuoteWriteKind.DELETE -> replayDelete(quote = quote)
        }
    }

    /**
     * Insert replay is at-least-once: a response lost after the server committed leaves nothing local to
     * tell the two apart, so a retry looks for the server's copy before posting again.
     */
    private suspend fun replayInsert(
        quote: QuoteEntity,
        isRetry: Boolean,
    ): ReplayOutcome {
        if (quote.syncState == QuoteSyncState.SYNCED) return ReplayOutcome.SYNCED

        val adoptedRemoteId: Long? = if (isRetry) findRemoteCopy(quote = quote) else null

        val remoteId: Long = adoptedRemoteId ?: runCatching {
            remoteDataSource.insertQuote(entity = quote)
        }.getOrElse { failure ->
            if (isRejection(error = failure)) localDataSource.deleteRow(localId = quote.localId)

            throw failure
        }

        val rowStillExisted: Boolean = localDataSource.markSynced(
            localId = quote.localId,
            remoteId = remoteId,
        )

        if (rowStillExisted.not()) deleteOrphanedRemote(remoteId = remoteId)

        return ReplayOutcome.SYNCED
    }

    private suspend fun findRemoteCopy(quote: QuoteEntity): Long? = runCatching {
        remoteDataSource
            .getQuotes(bookId = quote.bookId)
            .firstOrNull { quote.isSameQuoteAs(other = it) }
            ?.remoteId
    }.getOrElse { failure ->
        rethrowIfCancelled(error = failure)

        AppLog.e("Could not look for a server copy of quote ${quote.localId}: ${failure.message}")

        null
    }

    private suspend fun deleteOrphanedRemote(remoteId: Long) {
        runCatching {
            remoteDataSource.deleteQuote(remoteId = remoteId)
        }.onFailure { failure ->
            rethrowIfCancelled(error = failure)

            AppLog.e("Could not delete quote $remoteId after its local row was removed: ${failure.message}")
        }
    }

    private suspend fun replayDelete(quote: QuoteEntity): ReplayOutcome {
        val remoteId: Long = quote.remoteId ?: return ReplayOutcome.DISCARDED

        runCatching {
            remoteDataSource.deleteQuote(remoteId = remoteId)
        }.onFailure { failure ->
            if (isRejection(error = failure)) localDataSource.restoreSynced(localId = quote.localId)

            throw failure
        }

        localDataSource.deleteRow(localId = quote.localId)

        return ReplayOutcome.SYNCED
    }

    private fun rethrowIfCancelled(error: Throwable) {
        if (error is CancellationException) throw error
    }

    private fun isRejection(error: Throwable): Boolean =
        error !is CancellationException && error !is RetryableSyncException
}
