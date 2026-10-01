package nl.rhaydus.softcover.core.uibinding.progress

import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.progress.ProgressSheetMedium
import nl.rhaydus.softcover.core.component.progress.ProgressSheetTab
import nl.rhaydus.softcover.core.component.progress.ProgressSheetUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel
import nl.rhaydus.softcover.core.domain.model.Book
import kotlin.math.roundToInt

/**
 * Maps a [Book] onto what the progress sheet renders.
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
        medium = medium,
        progressPercent = userBookRead?.progress?.roundToInt() ?: 0,
        selectedTab = selectedTab,
        scaffold = SheetScaffoldUiModel(
            header = SheetHeaderUiModel(
                section = SectionHeaderUiModel.Section(
                    eyebrow = "Reading progress",
                    headline = title,
                ),
            ),
        ),
    )
}
