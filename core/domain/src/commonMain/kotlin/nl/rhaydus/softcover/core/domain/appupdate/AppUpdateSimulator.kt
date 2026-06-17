package nl.rhaydus.softcover.core.domain.appupdate

interface AppUpdateSimulator {
    val isEnabled: Boolean

    suspend fun simulateUpdateAvailable()

    fun simulateDownloading()

    fun simulateDownloaded()

    fun simulateFailed()

    suspend fun reset()
}
