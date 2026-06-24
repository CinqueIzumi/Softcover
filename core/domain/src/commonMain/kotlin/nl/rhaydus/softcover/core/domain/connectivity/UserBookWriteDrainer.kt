package nl.rhaydus.softcover.core.domain.connectivity

import kotlinx.coroutines.CoroutineScope

interface UserBookWriteDrainer {
    fun start(scope: CoroutineScope)

    suspend fun drainPendingUpdates(): Set<Int>
}
