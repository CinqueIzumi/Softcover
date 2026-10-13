package nl.rhaydus.softcover.core.component.progress

import nl.rhaydus.softcover.core.component.field.TextFieldUiModel

/**
 * What a book's progress is measured in, and where it currently stands.
 *
 * The branch is keyed on the edition's `ReadingFormat` (`BookEdition.isAudiobook`) rather than on
 * whether a duration happens to be present. [Timed] is the primary entry unit on an audiobook,
 * [Paged] everywhere else.
 */
sealed interface ProgressSheetMedium {
    /**
     * Whether the total is known. Percentage entry converts a fraction into pages (or seconds), so
     * without a total it could only ever record 0 — the sheet offers that tab only when this is true.
     */
    val hasKnownTotal: Boolean

    /**
     * Print or ebook: progress is a page number out of [totalPages]. [suffix] is the "of N pages"
     * line under the number, `null` when the total is unknown.
     */
    data class Paged(
        val totalPages: Int,
        val currentPage: Int,
        val suffix: String?,
    ) : ProgressSheetMedium {
        override val hasKnownTotal: Boolean
            get() = totalPages > 0
    }

    /**
     * Audiobook: progress is a position out of [totalSeconds]. The three fields hold the reader's
     * in-progress entry; [fraction] is that entry over the total (0 when the total is unknown) and
     * [suffix] the "of hh:mm:ss" line, `null` when the total is unknown.
     */
    data class Timed(
        val totalSeconds: Int,
        val hours: TextFieldUiModel,
        val minutes: TextFieldUiModel,
        val seconds: TextFieldUiModel,
        val suffix: String?,
        val fraction: Float,
        val canDecreaseMinutes: Boolean,
        val canIncreaseMinutes: Boolean,
    ) : ProgressSheetMedium {
        override val hasKnownTotal: Boolean
            get() = totalSeconds > 0
    }
}
