package nl.rhaydus.softcover.core.component.progress

/**
 * What a book's progress is measured in, and where it currently stands.
 *
 * A sealed variant rather than four nullable totals beside an `isAudiobook` flag (R2): an audiobook
 * has no page count and a print edition has no runtime, so carrying both would let a caller build a
 * book that is somehow neither and oblige every reader of the model to remember which pair means
 * anything. The mapper picks the branch once; the sheet reads it.
 */
sealed interface ProgressSheetMedium {
    /**
     * Whether the total is known. Percentage entry converts a fraction into pages (or seconds), so
     * without a total it could only ever record 0 — the sheet offers that tab only when this is true.
     */
    val hasKnownTotal: Boolean

    /** Print or ebook: progress is a page number out of [totalPages]. */
    data class Paged(
        val totalPages: Int,
        val currentPage: Int,
    ) : ProgressSheetMedium {
        override val hasKnownTotal: Boolean
            get() = totalPages > 0
    }

    /** Audiobook: progress is a position out of [totalSeconds]. */
    data class Timed(
        val totalSeconds: Int,
        val currentSeconds: Int,
    ) : ProgressSheetMedium {
        override val hasKnownTotal: Boolean
            get() = totalSeconds > 0
    }
}
