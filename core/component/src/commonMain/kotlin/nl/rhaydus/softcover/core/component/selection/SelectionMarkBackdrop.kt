package nl.rhaydus.softcover.core.component.selection

/**
 * What an unselected [SelectionMark] sits over, deciding its ring's fill. [SelectionMarkColors.of]
 * resolves each entry onto the theme.
 */
enum class SelectionMarkBackdrop {
    /** Over a book cover. */
    Image,

    /** Over a flat app surface. */
    Surface,
}
