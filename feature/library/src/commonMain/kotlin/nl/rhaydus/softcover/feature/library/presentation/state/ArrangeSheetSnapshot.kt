package nl.rhaydus.softcover.feature.library.presentation.state

import nl.rhaydus.softcover.core.component.control.ToggleUiModel
import nl.rhaydus.softcover.core.component.control.ToggleVariant
import nl.rhaydus.softcover.core.presentation.model.LibraryTab

internal data class ArrangeSheetSnapshot(
    val resultCount: Int,
    val titlesToggle: ArrangeTitlesToggleUiModel?,
    val makeOrdered: ArrangeMakeOrderedUiModel?,
) {
    companion object {
        fun from(state: LibraryUiState): ArrangeSheetSnapshot {
            val tabId = state.selectedTabId

            val resultCount = state.displayEditionsFor(tabId = tabId)?.size
                ?: state.displayBooksFor(tabId = tabId)?.size
                ?: state.tabStatsFor(tabId = tabId).itemCount

            val gridLayout = state.arrangeDraft?.gridLayout

            val unrankedList = state.customLists.firstOrNull { list ->
                LibraryTab.CustomList.idFor(listId = list.id) == tabId && list.ranked.not()
            }

            return ArrangeSheetSnapshot(
                resultCount = resultCount,
                titlesToggle = gridLayout
                    ?.takeIf { it.chip != LibraryLayoutChip.LIST }
                    ?.let { layout ->
                        ArrangeTitlesToggleUiModel(
                            label = "Show titles & authors",
                            toggle = ToggleUiModel(
                                key = "show-titles",
                                checked = layout.showsTitles,
                                description = "Show titles & authors",
                                variant = ToggleVariant.Switch,
                            ),
                        )
                    },
                makeOrdered = unrankedList?.let { list ->
                    ArrangeMakeOrderedUiModel(
                        label = "✶ Make this list ordered",
                        listId = list.id,
                    )
                },
            )
        }
    }
}
