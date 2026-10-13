package nl.rhaydus.softcover.core.component.selection

/** What an unselected [SelectionMark] sits over, deciding its ring's fill. */
enum class SelectionMarkBackdrop {
    /** Over a book cover. */
    Image,

    /** Over a flat app surface. */
    Surface,
}
