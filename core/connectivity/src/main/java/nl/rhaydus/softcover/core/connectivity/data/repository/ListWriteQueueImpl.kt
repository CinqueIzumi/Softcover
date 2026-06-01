package nl.rhaydus.softcover.core.connectivity.data.repository

import nl.rhaydus.softcover.core.connectivity.data.mapper.toEntity
import nl.rhaydus.softcover.core.data.database.dao.PendingListWriteDao
import nl.rhaydus.softcover.core.domain.connectivity.ListWriteQueue
import nl.rhaydus.softcover.core.domain.connectivity.PendingListWrite

class ListWriteQueueImpl(
    private val dao: PendingListWriteDao,
) : ListWriteQueue {
    override suspend fun enqueue(write: PendingListWrite) {
        dao.insert(entity = write.toEntity())
    }
}
