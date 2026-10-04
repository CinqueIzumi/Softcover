package nl.rhaydus.softcover.core.uibinding.progress

import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.progress.ProgressSheetMedium
import nl.rhaydus.softcover.core.component.progress.ProgressSheetTab
import nl.rhaydus.softcover.core.component.progress.ProgressSheetUiModel
import nl.rhaydus.softcover.core.component.progress.ProgressTimeUnit
import nl.rhaydus.softcover.core.component.progress.progressTimeField
import nl.rhaydus.softcover.core.component.sheet.SheetHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel
import nl.rhaydus.softcover.core.domain.model.Book
import kotlin.math.roundToInt

/**
 * Maps a [Book] onto what the progress sheet renders.
 *
 * [selectedTab] is the reader's stored last-used unit; [timeDraft] is their uncommitted time entry, or
 * `null` to seed the fields from the book's position. A stored `PAGE` on an audiobook is not a valid
 * tab, so [ProgressSheetUiModel.tabsFor] falls back to the medium's primary unit.
 *
 * **Two resolution asymmetries are deliberate and preserved from the pre-migration sheet.** The page
 * total falls back to the book's default edition when the current one has no page count, but the
 * audio total does *not* — it reads the current edition only. And the medium is chosen from the
 * current edition alone, so a print edition shelved against a book whose default is an audiobook
 * still logs pages. Adding a fallback to either would silently change which tabs a book offers.
 */
fun Book.toProgressSheetUiModel(
    selectedTab: ProgressSheetTab,
    timeDraft: ProgressTimeDraft? = null,
): ProgressSheetUiModel {
    val edition = currentEdition

    val medium = if (edition?.isAudiobook == true) {
        val draft = ProgressTimeDraft.resolve(
            stored = timeDraft,
            currentSeconds = progressCurrentSeconds,
        )

        ProgressSheetMedium.Timed(
            totalSeconds = edition.audioSeconds ?: 0,
            currentSeconds = progressCurrentSeconds,
            hours = progressTimeField(
                unit = ProgressTimeUnit.HOURS,
                text = draft.hours,
            ),
            minutes = progressTimeField(
                unit = ProgressTimeUnit.MINUTES,
                text = draft.minutes,
            ),
            seconds = progressTimeField(
                unit = ProgressTimeUnit.SECONDS,
                text = draft.seconds,
            ),
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
        tabs = ProgressSheetUiModel.tabsFor(
            medium = medium,
            selectedTab = selectedTab,
        ),
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

/** Where the book's listening position currently stands, the seed for a fresh [ProgressTimeDraft]. */
val Book.progressCurrentSeconds: Int
    get() = userBookRead?.currentSeconds ?: 0
