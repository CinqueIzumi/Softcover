package nl.rhaydus.softcover.feature.library.presentation.state

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import nl.rhaydus.softcover.core.component.chip.ChipTone
import nl.rhaydus.softcover.core.component.chip.ChipTrailing
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.component.chip.toChipSet
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookDeadline
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.domain.model.SortDirection
import nl.rhaydus.softcover.core.presentation.model.LibraryTab
import nl.rhaydus.softcover.feature.library.presentation.sort.librarySortOptions

/**
 * Pure, dispatcher-friendly builder mapping the Arrange sheet's [LibraryArrangeDraft] to its two
 * chip rows and its sort-row header (`component-contract.md` § 7.2 R9) — kept top-level, like
 * [buildLibraryFilterChips], so `ArrangeDraftChipsCollector` can call it on `Dispatchers.Default`.
 * `tab` resolves [draft]'s tabId against [visibleTabs] rather than taking a [LibraryTab] directly,
 * since the collector only holds state slices, not the render's own tab reference.
 */
internal fun buildLibraryArrangeChips(
    draft: LibraryArrangeDraft,
    visibleTabs: List<LibraryTab>,
    customLists: ImmutableList<BookList>,
    booksByTab: ImmutableMap<String, ImmutableList<Book>>,
    deadlines: ImmutableMap<Int, BookDeadline>,
): LibraryArrangeChips {
    val tab = visibleTabs.firstOrNull { it.id == draft.tabId }
        ?: return LibraryArrangeChips.Empty

    val layoutChips = LibraryLayoutChip.entries.map { chip ->
        ChipUiModel(
            key = chip.arrangeChipKey,
            label = chip.arrangeLabel,
            tone = ChipTone.Choice,
            selected = chip == draft.gridLayout.chip,
        ) to chip
    }.toChipSet()

    // librarySortOptions only reads customLists/booksByTab/deadlines off state; a scratch
    // LibraryUiState avoids widening its signature for this one caller.
    val sortState = LibraryUiState(
        customLists = customLists,
        booksByTab = booksByTab,
        deadlines = deadlines,
    )

    val sortChips = librarySortOptions(
        tab = tab,
        state = sortState,
    ).map { mode ->
        val isActive = mode == draft.sortMode
        val isPositional = mode == LibrarySortMode.MANUAL || mode == LibrarySortMode.ORDER

        ChipUiModel(
            key = mode.arrangeChipKey,
            label = mode.label,
            tone = ChipTone.Choice,
            selected = isActive,
            trailing = if (isActive && isPositional.not()) {
                mode.directionTrailing(direction = draft.sortDirection)
            } else {
                null
            },
        ) to mode
    }.toChipSet()

    val sortHeader = SectionHeaderUiModel.Label(eyebrow = "Sort by · ${tab.label}")

    return LibraryArrangeChips(
        layoutChips = layoutChips,
        sortChips = sortChips,
        sortHeader = sortHeader,
    )
}

private fun LibrarySortMode.directionTrailing(direction: SortDirection): ChipTrailing.Icon =
    if (direction == SortDirection.ASCENDING) {
        ChipTrailing.Icon(
            icon = SoftcoverIcon.ArrowDropUp,
            description = "Ascending",
        )
    } else {
        ChipTrailing.Icon(
            icon = SoftcoverIcon.ArrowDropDown,
            description = "Descending",
        )
    }

internal val LibraryLayoutChip.arrangeChipKey: String
    get() = "layout:$name"

internal val LibrarySortMode.arrangeChipKey: String
    get() = "sort:$name"

private val LibraryLayoutChip.arrangeLabel: String
    get() = when (this) {
        LibraryLayoutChip.GRID_TWO -> "Grid · 2"
        LibraryLayoutChip.GRID_THREE -> "Grid · 3"
        LibraryLayoutChip.LIST -> "List"
    }
