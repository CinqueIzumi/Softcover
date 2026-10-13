package nl.rhaydus.softcover.core.component.sheet

/** How a [SheetScaffold] arranges its regions when the body is taller than the sheet. */
enum class SheetLayout {
    /** The whole sheet scrolls as one; the footer comes at the end of the content. */
    Scrolling,

    /** The dismiss label and header stay fixed and the footer sticks; the body scrolls above the footer. */
    Pinned,
}
