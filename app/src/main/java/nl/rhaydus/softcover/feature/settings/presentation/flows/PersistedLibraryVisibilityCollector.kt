package nl.rhaydus.softcover.feature.settings.presentation.flows

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.settings.presentation.event.LibraryVisibilitySettingsEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.LibraryVisibilitySettingsDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.LibraryVisibilitySettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.LibraryVisibilitySettingsUiState

class PersistedLibraryVisibilityCollector : LibraryVisibilityInitializer {
    override suspend fun onLaunch(
        scope: ActionScope<LibraryVisibilitySettingsUiState, LibraryVisibilitySettingsEvent, LibraryVisibilitySettingsLocalVariables>,
        dependencies: LibraryVisibilitySettingsDependencies,
    ) {
        combine(
            dependencies.getEnabledStatusCodesAsFlowUseCase(),
            dependencies.getEnabledListIdsAsFlowUseCase(),
        ) { codes: Set<Int>, ids: Set<Int> -> codes to ids }
            .collectLatest { (codes, ids) ->
                scope.setState { state ->
                    state.copy(
                        persistedEnabledStatusCodes = codes,
                        persistedEnabledListIds = ids,
                        draftEnabledStatusCodes = if (state.initialized) state.draftEnabledStatusCodes else codes,
                        draftEnabledListIds = if (state.initialized) state.draftEnabledListIds else ids,
                        initialized = true,
                    )
                }
            }
    }
}
