package nl.rhaydus.softcover.feature.app_update.data.simulator

import nl.rhaydus.softcover.feature.app_update.domain.simulator.AppUpdateSimulator

object NoOpAppUpdateSimulator : AppUpdateSimulator {
    override val isEnabled: Boolean = false

    override suspend fun simulateUpdateAvailable() = Unit

    override fun simulateDownloading() = Unit

    override fun simulateDownloaded() = Unit

    override fun simulateFailed() = Unit

    override suspend fun reset() = Unit
}
