package nl.rhaydus.softcover.feature.library.presentation.collector

import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.presentation.model.LibraryTab
import nl.rhaydus.softcover.feature.library.presentation.action.withTabSelected
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilters
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.softcover.feature.library.presentation.state.TagSelection
import nl.rhaydus.softcover.feature.library.presentation.state.UserTagKey
import nl.rhaydus.toad.ActionScope

/**
 * Applies the own-tag filter another screen asked for: the Read shelf when it is visible, otherwise All,
 * showing only that tag.
 */
internal class LibraryTagFilterRequestCollector : LibraryCollector {
    override suspend fun onLaunch(
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
        dependencies: LibraryDependencies,
    ) {
        val readTabId = LibraryTab.Status.of(UserBookStatus.READ).id

        combine(
            dependencies.libraryFilterRequests.pending,
            scope.state.map { it.tabsLoaded }.distinctUntilChanged(),
        ) { request, tabsLoaded -> request.takeIf { tabsLoaded } }
            .filter { it != null }
            .collect { request ->
                if (request == null) return@collect

                scope.setState { state ->
                    val target = if (state.visibleTabs.any { it.id == readTabId }) readTabId else LibraryTab.All.id

                    val filters = LibraryFilters(
                        yourTags = TagSelection(
                            included = persistentSetOf(
                                UserTagKey(
                                    category = request.category,
                                    name = request.name,
                                ),
                            ),
                        ),
                    )

                    state.withTabSelected(tabId = target).copy(
                        filtersByTab = (state.filtersByTab + (target to filters)).toImmutableMap(),
                        filterDraft = null,
                        isFilterSheetExpanded = false,
                        searchQuery = "",
                    )
                }

                dependencies.libraryFilterRequests.consume(request = request)
            }
    }
}
