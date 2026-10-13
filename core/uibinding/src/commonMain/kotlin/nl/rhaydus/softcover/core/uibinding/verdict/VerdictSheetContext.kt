package nl.rhaydus.softcover.core.uibinding.verdict

/**
 * Which caller raises the verdict sheet and why — the input that swaps the copy
 * [toVerdictSheetUiModel] writes (eyebrow, headline, description, dismiss and save labels) and whether
 * deletion is offered. The sheet's anatomy is identical either way.
 */
enum class VerdictSheetContext {
    /**
     * Raised once on a genuine mark-as-read transition — by `feature/reading`, and by `feature/book_detail`
     * when the book page marks the book as read.
     */
    FINISHED,

    /** `feature/book_detail` reopening the sheet from the book page to change an existing verdict. */
    EDIT,
}
