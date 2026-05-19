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
            dependencies.getLibraryTabOrderAsFlowUseCase(),
        ) { codes: Set<Int>, ids: Set<Int>, order: List<String> ->
            Triple(codes, ids, order)
        }.collectLatest { (codes, ids, order) ->
            scope.setState { state ->
                state.copy(
                    persistedEnabledStatusCodes = codes,
                    persistedEnabledListIds = ids,
                    persistedTabOrder = order,
                    draftEnabledStatusCodes = if (state.initialized) state.draftEnabledStatusCodes else codes,
                    draftEnabledListIds = if (state.initialized) state.draftEnabledListIds else ids,
                    draftTabOrder = if (state.initialized) state.draftTabOrder else order,
                    initialized = true,
                )
            }
        }
    }
}
