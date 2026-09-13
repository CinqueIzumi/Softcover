package nl.rhaydus.softcover.core.uibinding.progress

import kotlin.math.roundToInt
import nl.rhaydus.softcover.core.component.progress.ProgressSheetMedium
import nl.rhaydus.softcover.core.component.progress.ProgressSheetTab
import nl.rhaydus.softcover.core.component.progress.ProgressSheetUiModel
import nl.rhaydus.softcover.core.domain.model.Book

/**
 * Maps a [Book] onto what the progress sheet renders. Promoted straight to `:core:uibinding` rather
 * than starting feature-local (R6) because it landed with two consumers already — `book_detail` and
 * `reading` both host the sheet off their own state.
 *
 * [selectedTab] is the reader's stored last-used unit, which the sheet may still override: a stored
 * `PAGE` on an audiobook is not a valid tab, and the sheet falls back to the medium's primary unit
 * rather than the mapper second-guessing it here.
 *
 * **Two resolution asymmetries are deliberate and preserved from the pre-migration sheet.** The page
 * total falls back to the book's default edition when the current one has no page count, but the
 * audio total does *not* — it reads the current edition only. And the medium is chosen from the
 * current edition alone, so a print edition shelved against a book whose default is an audiobook
 * still logs pages. Adding a fallback to either would silently change which tabs a book offers.
 */
fun Book.toProgressSheetUiModel(selectedTab: ProgressSheetTab): ProgressSheetUiModel {
    val edition = currentEdition

    val medium = if (edition?.isAudiobook == true) {
        ProgressSheetMedium.Timed(
            totalSeconds = edition.audioSeconds ?: 0,
            currentSeconds = userBookRead?.currentSeconds ?: 0,
        )
    } else {
        ProgressSheetMedium.Paged(
            totalPages = edition?.pages ?: defaultEdition?.pages ?: 0,
            currentPage = userBookRead?.currentPage ?: 0,
        )
    }

    return ProgressSheetUiModel(
        bookTitle = title,
        medium = medium,
        progressPercent = userBookRead?.progress?.roundToInt() ?: 0,
        selectedTab = selectedTab,
    )
}
