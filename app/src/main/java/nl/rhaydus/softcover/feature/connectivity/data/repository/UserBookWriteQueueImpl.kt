package nl.rhaydus.softcover.feature.connectivity.data.repository

import nl.rhaydus.softcover.core.domain.connectivity.UserBookWriteQueue
import nl.rhaydus.softcover.core.domain.connectivity.PendingUserBookWrite
import nl.rhaydus.softcover.feature.connectivity.data.dao.PendingUserBookWriteDao
import nl.rhaydus.softcover.feature.connectivity.data.model.PendingUserBookWriteEntity

class UserBookWriteQueueImpl(
    private val dao: PendingUserBookWriteDao,
) : UserBookWriteQueue {
    override suspend fun enqueue(update: PendingUserBookWrite) {
        dao.insertReplacing(
            PendingUserBookWriteEntity(
                kind = update.kind.name,
                userBookId = update.userBookId,
                userBookReadId = update.userBookReadId,
                bookId = update.bookId,
                editionId = update.editionId,
                progressPages = update.progressPages,
                progressSeconds = update.progressSeconds,
                startedAt = update.startedAt,
                finishedAt = update.finishedAt,
                rating = update.rating,
                enqueuedAt = update.enqueuedAt,
            ),
        )
    }
}
