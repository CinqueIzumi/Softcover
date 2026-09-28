package nl.rhaydus.softcover.core.component.selection

/**
 * Every surface [SelectionMark] renders on, one entry per surface (`component-contract.md` § 7.2
 * R2) — see [SelectionMarkDimensions.forVariant] for each surface's size and icon size, and
 * `SelectionMark`'s tone resolution for each surface's unselected container colour.
 */
enum class SelectionMarkVariant {
    /** Pinned to a grid cover's corner in library selection mode. */
    OnCover,

    /** Leading a row item, ahead of its cover. */
    RowLeading,

    /** Centred over an edition card in the edition selector. */
    EditionCard,
}
