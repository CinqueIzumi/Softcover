package nl.rhaydus.softcover.feature.library.presentation.collector

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.sidebar.SidebarRowUiModel
import nl.rhaydus.softcover.core.component.sidebar.SidebarRowVariant
import nl.rhaydus.softcover.core.component.sidebar.SidebarSectionUiModel
import nl.rhaydus.softcover.core.presentation.model.LibraryTab
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope

internal class ShelfSidebarSectionsCollector : LibraryCollector {
    override suspend fun onLaunch(
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
        dependencies: LibraryDependencies,
    ) {
        scope.state
            .map { state ->
                shelfSidebarSectionsFor(
                    visibleTabs = state.visibleTabs,
                    selectedTabId = state.selectedTabId,
                    shelvesSidebarLabel = state.shelvesSidebarLabel,
                    listsSidebarLabel = state.listsSidebarLabel,
                )
            }
            .distinctUntilChanged()
            .collectLatest { sections ->
                scope.setState { it.copy(shelfSidebarSections = sections) }
            }
    }
}

internal fun shelfSidebarSectionsFor(
    visibleTabs: List<LibraryTab>,
    selectedTabId: String,
    shelvesSidebarLabel: SectionHeaderUiModel,
    listsSidebarLabel: SectionHeaderUiModel,
): ImmutableList<SidebarSectionUiModel<String>> {
    val shelves = visibleTabs.filterNot { it is LibraryTab.CustomList }
    val lists = visibleTabs.filterIsInstance<LibraryTab.CustomList>()

    fun rowFor(tab: LibraryTab) = SidebarRowUiModel(
        key = tab.id,
        label = tab.label,
        variant = SidebarRowVariant.Destination(
            selected = tab.id == selectedTabId,
            longPressable = true,
        ),
    )

    val sections = buildList {
        add(
            SidebarSectionUiModel(
                label = shelvesSidebarLabel,
                rows = shelves.map(::rowFor).toImmutableList(),
            ),
        )

        if (lists.isNotEmpty()) {
            add(
                SidebarSectionUiModel(
                    label = listsSidebarLabel,
                    rows = lists.map(::rowFor).toImmutableList(),
                ),
            )
        }
    }

    return sections.toImmutableList()
}
