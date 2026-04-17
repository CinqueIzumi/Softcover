package nl.rhaydus.softcover.feature.app_update.domain.simulator

interface AppUpdateSimulator {
    val isEnabled: Boolean

    suspend fun simulateUpdateAvailable()

    fun simulateDownloading()

    fun simulateDownloaded()

    fun simulateFailed()

    suspend fun reset()
}
