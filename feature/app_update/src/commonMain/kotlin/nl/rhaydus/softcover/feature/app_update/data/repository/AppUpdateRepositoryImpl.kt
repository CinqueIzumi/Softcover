package nl.rhaydus.softcover.feature.app_update.data.repository

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.domain.model.AppUpdateState
import nl.rhaydus.softcover.feature.app_update.data.datasource.AppUpdateDataSource
import nl.rhaydus.softcover.feature.app_update.domain.repository.AppUpdateRepository

internal class AppUpdateRepositoryImpl(private val appUpdateDataSource: AppUpdateDataSource) : AppUpdateRepository {
    override val updateState: Flow<AppUpdateState> = appUpdateDataSource.updateState

    override suspend fun checkForUpdate() {
        appUpdateDataSource.checkForUpdate()
    }

    override fun completeUpdate() {
        appUpdateDataSource.completeUpdate()
    }
}
