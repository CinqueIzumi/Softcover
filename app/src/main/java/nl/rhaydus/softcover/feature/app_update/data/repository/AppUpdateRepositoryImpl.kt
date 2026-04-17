package nl.rhaydus.softcover.feature.app_update.data.repository

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.feature.app_update.data.datasource.AppUpdateDataSource
import nl.rhaydus.softcover.feature.app_update.domain.model.AppUpdateState
import nl.rhaydus.softcover.feature.app_update.domain.repository.AppUpdateRepository

class AppUpdateRepositoryImpl(private val appUpdateDataSource: AppUpdateDataSource) : AppUpdateRepository {
    override val updateState: Flow<AppUpdateState> = appUpdateDataSource.updateState

    override suspend fun checkForUpdate() {
        appUpdateDataSource.checkForUpdate()
    }

    override fun startUpdateFlow(launcher: ActivityResultLauncher<IntentSenderRequest>) {
        appUpdateDataSource.startUpdateFlow(launcher = launcher)
    }

    override fun completeUpdate() {
        appUpdateDataSource.completeUpdate()
    }
}
