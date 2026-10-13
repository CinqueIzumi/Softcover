package nl.rhaydus.softcover.feature.library.presentation.collector

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.component.sheet.SheetRowCount
import nl.rhaydus.softcover.core.component.sheet.SheetRowUiModel
import nl.rhaydus.softcover.core.component.sheet.openOrUpdate
import nl.rhaydus.softcover.core.component.sheet.requestClose
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.presentation.model.LibraryTab
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryTabStats
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.softcover.feature.library.presentation.state.shelvesSheetUiModel
import nl.rhaydus.softcover.feature.library.presentation.util.formatBookCount
import nl.rhaydus.toad.ActionScope

internal class ShelvesSheetCollector : LibraryCollector {
    override suspend fun onLaunch(
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
        dependencies: LibraryDependencies,
    ) {
        scope.state
            .map { state ->
                val rows = shelvesSheetRowsFor(
                    visibleTabs = state.visibleTabs,
                    selectedTabId = state.selectedTabId,
                    tabStatsByTab = state.tabStatsByTab,
                )

                rows.takeIf { state.isShelvesSheetRequested }
            }
            .distinctUntilChanged()
            .collectLatest { rows ->
                scope.setState {
                    it.copy(
                        shelvesSheet = if (rows == null || it.isShelvesSheetRequested.not()) {
                            it.shelvesSheet.requestClose()
                        } else {
                            it.shelvesSheet.openOrUpdate(model = shelvesSheetUiModel(rows = rows))
                        },
                    )
                }
            }
    }
}

internal fun shelvesSheetRowsFor(
    visibleTabs: List<LibraryTab>,
    selectedTabId: String,
    tabStatsByTab: Map<String, LibraryTabStats>,
): ImmutableList<SheetRowUiModel<String>> =
    visibleTabs
        .map { tab ->
            val itemCount = tabStatsByTab[tab.id]?.itemCount ?: 0

            SheetRowUiModel(
                key = tab.id,
                label = tab.label,
                selected = tab.id == selectedTabId,
                leadingIcon = if (tab is LibraryTab.CustomList) SoftcoverIcon.LibraryBooks else SoftcoverIcon.Shelf,
                count = SheetRowCount(
                    text = itemCount.toString(),
                    description = formatBookCount(count = itemCount),
                ),
            )
        }
        .toImmutableList()
