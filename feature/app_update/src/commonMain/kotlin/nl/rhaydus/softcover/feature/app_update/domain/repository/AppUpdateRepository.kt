package nl.rhaydus.softcover.feature.app_update.domain.repository

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.domain.model.AppUpdateState

interface AppUpdateRepository {
    val updateState: Flow<AppUpdateState>

    suspend fun checkForUpdate()

    fun completeUpdate()
}
