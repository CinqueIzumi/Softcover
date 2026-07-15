package nl.rhaydus.softcover.feature.library.presentation.state

import nl.rhaydus.softcover.core.domain.model.LibraryGridLayout

/**
 * The Arrange sheet's three layout chips (Grid·2 / Grid·3 / List). Pure UI-facing bucketing over
 * the persisted [LibraryGridLayout] — no new persisted state. `LIST_COMPACT` collapses into [LIST]
 * alongside `LIST_LARGE`: the redesigned picker only ever writes `LIST_LARGE`, but a
 * previously-persisted `LIST_COMPACT` still renders as the List chip rather than falling through.
 */
internal enum class LibraryLayoutChip {
    GRID_TWO,
    GRID_THREE,
    LIST,
}

/** Which [LibraryLayoutChip] [this] belongs to. */
internal val LibraryGridLayout.chip: LibraryLayoutChip
    get() = when (this) {
        LibraryGridLayout.GRID_TWO_COLUMNS,
        LibraryGridLayout.GRID_TWO_COLUMNS_COVER_ONLY,
            -> LibraryLayoutChip.GRID_TWO

        LibraryGridLayout.GRID_THREE_COLUMNS,
        LibraryGridLayout.GRID_THREE_COLUMNS_COVER_ONLY,
            -> LibraryLayoutChip.GRID_THREE

        LibraryGridLayout.LIST_COMPACT,
        LibraryGridLayout.LIST_LARGE,
            -> LibraryLayoutChip.LIST
    }

/**
 * Resolves the concrete [LibraryGridLayout] for selecting [this] chip while preserving [showTitles]
 * — e.g. picking Grid·3 from a cover-only Grid·2 stays cover-only. List has no cover-only sibling
 * and only ever resolves to `LIST_LARGE` (the redesign's de-carded row); `LIST_COMPACT` is not
 * offered by the picker, per the layout-model note in the redesign brief.
 */
internal fun LibraryLayoutChip.toLayout(showTitles: Boolean): LibraryGridLayout = when (this) {
    LibraryLayoutChip.GRID_TWO ->
        if (showTitles) LibraryGridLayout.GRID_TWO_COLUMNS else LibraryGridLayout.GRID_TWO_COLUMNS_COVER_ONLY

    LibraryLayoutChip.GRID_THREE ->
        if (showTitles) LibraryGridLayout.GRID_THREE_COLUMNS else LibraryGridLayout.GRID_THREE_COLUMNS_COVER_ONLY

    LibraryLayoutChip.LIST -> LibraryGridLayout.LIST_LARGE
}

/** Whether [this] layout renders titles/authors alongside covers. List layouts always show them. */
internal val LibraryGridLayout.showsTitles: Boolean
    get() = when (this) {
        LibraryGridLayout.GRID_TWO_COLUMNS_COVER_ONLY,
        LibraryGridLayout.GRID_THREE_COLUMNS_COVER_ONLY,
            -> false

        LibraryGridLayout.GRID_TWO_COLUMNS,
        LibraryGridLayout.GRID_THREE_COLUMNS,
        LibraryGridLayout.LIST_COMPACT,
        LibraryGridLayout.LIST_LARGE,
            -> true
    }

/**
 * Flips titled-ness while staying on the same [chip] bucket — the "Show titles & authors" toggle's
 * effect. A no-op on List, which has no cover-only sibling; the toggle is hidden there in the UI.
 */
internal fun LibraryGridLayout.withTitlesShown(show: Boolean): LibraryGridLayout = when (this) {
    LibraryGridLayout.GRID_TWO_COLUMNS,
    LibraryGridLayout.GRID_TWO_COLUMNS_COVER_ONLY,
        -> if (show) LibraryGridLayout.GRID_TWO_COLUMNS else LibraryGridLayout.GRID_TWO_COLUMNS_COVER_ONLY

    LibraryGridLayout.GRID_THREE_COLUMNS,
    LibraryGridLayout.GRID_THREE_COLUMNS_COVER_ONLY,
        -> if (show) LibraryGridLayout.GRID_THREE_COLUMNS else LibraryGridLayout.GRID_THREE_COLUMNS_COVER_ONLY

    LibraryGridLayout.LIST_COMPACT,
    LibraryGridLayout.LIST_LARGE,
        -> this
}
