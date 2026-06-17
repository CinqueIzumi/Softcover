package nl.rhaydus.softcover.feature.app_update.data.datasource

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import nl.rhaydus.softcover.core.domain.model.AppUpdateState

/**
 * iOS has no in-app update mechanism — updates are an App Store concern — so the data source is a
 * no-op that permanently reports [AppUpdateState.Idle]. Bound (rather than left unbound) so common
 * consumers like the settings debug section resolve without crashing.
 */
internal class IosAppUpdateDataSource : AppUpdateDataSource {
    override val updateState: Flow<AppUpdateState> = MutableStateFlow(AppUpdateState.Idle).asStateFlow()

    override suspend fun checkForUpdate() = Unit

    override fun completeUpdate() = Unit
}
