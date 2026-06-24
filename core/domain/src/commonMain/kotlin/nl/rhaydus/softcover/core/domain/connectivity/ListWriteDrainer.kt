package nl.rhaydus.softcover.core.domain.connectivity

import kotlinx.coroutines.CoroutineScope

interface ListWriteDrainer {
    fun start(scope: CoroutineScope)

    suspend fun drainPendingWrites()
}
