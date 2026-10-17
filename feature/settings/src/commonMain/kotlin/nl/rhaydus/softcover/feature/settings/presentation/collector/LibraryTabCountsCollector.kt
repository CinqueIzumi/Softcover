package nl.rhaydus.softcover.feature.settings.presentation.collector

import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.feature.settings.presentation.event.LibraryVisibilitySettingsEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.LibraryVisibilitySettingsDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.LibraryVisibilitySettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.LibraryVisibilitySettingsUiState
import nl.rhaydus.softcover.feature.settings.presentation.state.withOrderedEntries
import nl.rhaydus.toad.ActionScope

internal class LibraryTabCountsCollector : LibraryVisibilityCollector {
    override suspend fun onLaunch(
        scope: ActionScope<LibraryVisibilitySettingsUiState, LibraryVisibilitySettingsEvent, LibraryVisibilitySettingsLocalVariables>,
        dependencies: LibraryVisibilitySettingsDependencies,
    ) {
        dependencies.getAllUserBooksUseCase().map { all ->
            all.size to all.groupingBy { book -> book.status.code }.eachCount()
        }.collectLatest { (totalCount, statusCounts) ->
            scope.setState { state ->
                state
                    .copy(
                        totalCount = totalCount,
                        statusCounts = statusCounts.toImmutableMap(),
                    )
                    .withOrderedEntries()
            }
        }
    }
}
