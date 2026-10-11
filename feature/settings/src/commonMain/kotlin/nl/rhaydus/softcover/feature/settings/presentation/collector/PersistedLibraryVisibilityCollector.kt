package nl.rhaydus.softcover.feature.settings.presentation.collector

import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import nl.rhaydus.softcover.feature.settings.presentation.event.LibraryVisibilitySettingsEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.LibraryVisibilitySettingsDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.LibraryVisibilitySettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.LibraryVisibilitySettingsUiState
import nl.rhaydus.softcover.feature.settings.presentation.state.withOrderedEntries
import nl.rhaydus.toad.ActionScope

internal class PersistedLibraryVisibilityCollector : LibraryVisibilityCollector {
    override suspend fun onLaunch(
        scope: ActionScope<LibraryVisibilitySettingsUiState, LibraryVisibilitySettingsEvent, LibraryVisibilitySettingsLocalVariables>,
        dependencies: LibraryVisibilitySettingsDependencies,
    ) {
        combine(
            dependencies.getEnabledStatusCodesAsFlowUseCase(),
            dependencies.getEnabledListIdsAsFlowUseCase(),
            dependencies.getLibraryTabOrderAsFlowUseCase(),
        ) { codes: Set<Int>, ids: Set<Int>, order: List<String> ->
            Triple(
                codes,
                ids,
                order,
            )
        }.collectLatest { (codes, ids, order) ->
            scope.setState { state ->
                state
                    .copy(
                        persistedEnabledStatusCodes = codes.toImmutableSet(),
                        persistedEnabledListIds = ids.toImmutableSet(),
                        persistedTabOrder = order.toImmutableList(),
                        draftEnabledStatusCodes = if (state.initialized) state.draftEnabledStatusCodes else codes.toImmutableSet(),
                        draftEnabledListIds = if (state.initialized) state.draftEnabledListIds else ids.toImmutableSet(),
                        draftTabOrder = if (state.initialized) state.draftTabOrder else order.toImmutableList(),
                        initialized = true,
                    )
                    .withOrderedEntries()
            }
        }
    }
}
