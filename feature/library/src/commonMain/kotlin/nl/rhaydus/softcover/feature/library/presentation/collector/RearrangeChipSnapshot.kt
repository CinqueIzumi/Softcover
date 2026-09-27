package nl.rhaydus.softcover.feature.library.presentation.collector

import nl.rhaydus.softcover.core.component.chip.ChipLeading
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.presentation.model.LibraryTab

internal data class RearrangeChipSnapshot(
    val visibleTabs: List<LibraryTab>,
    val sortModeByTab: Map<String, LibrarySortMode>,
    val displayBookCountByTab: Map<String, Int>,
    val displayEditionCountByTab: Map<String, Int>,
    val rankedCustomListIds: Set<Int>,
    val isRearranging: Boolean,
) {
    fun compute(): Map<String, ChipUiModel> = buildMap {
        visibleTabs.forEach { tab ->
            if (canRearrange(tab = tab) || isRearranging) {
                put(
                    tab.id,
                    rearrangeChip(),
                )
            }
        }
    }

    /** Mirrors the reorder gating in `BookList` and `EditionList`, so the chip only shows where a drag persists. */
    private fun canRearrange(tab: LibraryTab): Boolean {
        val mode = sortModeByTab[tab.id] ?: LibraryTab.defaultSortMode(tabId = tab.id)

        return when (tab) {
            is LibraryTab.Status ->
                mode == LibrarySortMode.MANUAL &&
                    tab.status != UserBookStatus.DID_NOT_FINISH &&
                    (displayBookCountByTab[tab.id] ?: 0) >= 2

            is LibraryTab.CustomList ->
                mode == LibrarySortMode.ORDER &&
                    tab.listId in rankedCustomListIds &&
                    (displayEditionCountByTab[tab.id] ?: 0) >= 2

            LibraryTab.All -> false
        }
    }

    private fun rearrangeChip(): ChipUiModel = ChipUiModel(
        key = "rearrange",
        label = if (isRearranging) "Done" else "Reorder",
        selected = isRearranging,
        leading = ChipLeading.Icon(icon = SoftcoverIcon.DragHandle),
    )
}
