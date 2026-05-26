package nl.rhaydus.softcover.feature.connectivity.data.repository

import nl.rhaydus.softcover.core.domain.connectivity.ListWriteQueue
import nl.rhaydus.softcover.core.domain.connectivity.PendingListWrite
import nl.rhaydus.softcover.feature.connectivity.data.dao.PendingListWriteDao
import nl.rhaydus.softcover.feature.connectivity.data.mapper.toEntity

class ListWriteQueueImpl(
    private val dao: PendingListWriteDao,
) : ListWriteQueue {
    override suspend fun enqueue(write: PendingListWrite) {
        dao.insert(entity = write.toEntity())
    }
}
