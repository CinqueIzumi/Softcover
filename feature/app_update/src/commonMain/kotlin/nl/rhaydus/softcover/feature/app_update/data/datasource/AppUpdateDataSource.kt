package nl.rhaydus.softcover.feature.app_update.data.datasource

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.domain.model.AppUpdateState

interface AppUpdateDataSource {
    val updateState: Flow<AppUpdateState>

    suspend fun checkForUpdate()

    fun completeUpdate()
}
