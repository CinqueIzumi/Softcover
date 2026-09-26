package nl.rhaydus.softcover.core.component.verdict

/**
 * Which caller raised [VerdictSheet] and why — swaps the sheet's copy (eyebrow, headline,
 * description, dismiss label, save label) only; the anatomy is identical either way.
 */
enum class VerdictSheetContext {
    /** `feature/reading` raising the sheet once, on a genuine mark-as-read transition. */
    FINISHED,

    /** `feature/book_detail` reopening the sheet from the book page to change an existing verdict. */
    EDIT,
}
