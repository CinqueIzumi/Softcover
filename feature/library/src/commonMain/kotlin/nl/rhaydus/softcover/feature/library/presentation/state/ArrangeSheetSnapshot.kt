package nl.rhaydus.softcover.feature.library.presentation.state

import nl.rhaydus.softcover.core.component.control.ToggleRowUiModel
import nl.rhaydus.softcover.core.presentation.model.LibraryTab

internal data class ArrangeSheetSnapshot(
    val resultCount: Int,
    val titlesToggle: ToggleRowUiModel?,
    val makeOrderedListId: Int?,
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
                        ToggleRowUiModel(
                            key = "show-titles",
                            title = "Show titles & authors",
                            gloss = null,
                            checked = layout.showsTitles,
                        )
                    },
                makeOrderedListId = unrankedList?.id,
            )
        }
    }
}
