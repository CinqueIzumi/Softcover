package nl.rhaydus.softcover.core.connectivity.data.repository

import nl.rhaydus.softcover.core.database.dao.PendingUserBookWriteDao
import nl.rhaydus.softcover.core.database.model.PendingUserBookWriteEntity
import nl.rhaydus.softcover.core.domain.connectivity.PendingUserBookWrite
import nl.rhaydus.softcover.core.domain.connectivity.UserBookWriteQueue

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
                reviewSlateJson = update.reviewSlateJson,
                reviewHasSpoilers = update.reviewHasSpoilers,
                enqueuedAt = update.enqueuedAt,
            ),
        )
    }
}
