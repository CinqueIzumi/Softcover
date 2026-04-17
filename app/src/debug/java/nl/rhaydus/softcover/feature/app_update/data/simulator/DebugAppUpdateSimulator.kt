package nl.rhaydus.softcover.feature.app_update.data.simulator

import com.google.android.play.core.appupdate.testing.FakeAppUpdateManager
import nl.rhaydus.softcover.feature.app_update.data.datasource.AppUpdateDataSourceImpl
import nl.rhaydus.softcover.feature.app_update.domain.model.AppUpdateState
import nl.rhaydus.softcover.feature.app_update.domain.simulator.AppUpdateSimulator
import nl.rhaydus.softcover.feature.app_update.domain.usecase.CheckForAppUpdateUseCase

class DebugAppUpdateSimulator(
    private val fakeAppUpdateManager: FakeAppUpdateManager,
    private val appUpdateDataSource: AppUpdateDataSourceImpl,
    private val checkForAppUpdateUseCase: CheckForAppUpdateUseCase,
) : AppUpdateSimulator {
    override val isEnabled: Boolean = true

    override suspend fun simulateUpdateAvailable() {
        fakeAppUpdateManager.setUpdateAvailable(FAKE_AVAILABLE_VERSION_CODE)

        checkForAppUpdateUseCase()
    }

    override fun simulateDownloading() {
        appUpdateDataSource.forceState(AppUpdateState.Downloading)
    }

    override fun simulateDownloaded() {
        appUpdateDataSource.forceState(AppUpdateState.Downloaded)
    }

    override fun simulateFailed() {
        appUpdateDataSource.forceState(AppUpdateState.Failed)
    }

    override suspend fun reset() {
        fakeAppUpdateManager.setUpdateNotAvailable()
        appUpdateDataSource.forceState(AppUpdateState.Idle)
    }

    companion object {
        private const val FAKE_AVAILABLE_VERSION_CODE = 9_999
    }
}
