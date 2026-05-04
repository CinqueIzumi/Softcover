package nl.rhaydus.softcover.feature.connectivity.data.repository

import nl.rhaydus.softcover.core.domain.connectivity.OfflineProgressQueue
import nl.rhaydus.softcover.core.domain.connectivity.PendingProgressUpdate
import nl.rhaydus.softcover.feature.connectivity.data.dao.PendingProgressUpdateDao
import nl.rhaydus.softcover.feature.connectivity.data.model.PendingProgressUpdateEntity

class OfflineProgressQueueImpl(
    private val dao: PendingProgressUpdateDao,
) : OfflineProgressQueue {
    override suspend fun enqueue(update: PendingProgressUpdate) {
        dao.insertReplacing(
            PendingProgressUpdateEntity(
                kind = update.kind.name,
                userBookId = update.userBookId,
                userBookReadId = update.userBookReadId,
                bookId = update.bookId,
                editionId = update.editionId,
                progressPages = update.progressPages,
                progressSeconds = update.progressSeconds,
                startedAt = update.startedAt,
                finishedAt = update.finishedAt,
                enqueuedAt = update.enqueuedAt,
            ),
        )
    }
}
