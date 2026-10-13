package nl.rhaydus.softcover.core.uibinding.progress

import kotlinx.datetime.LocalDateTime
import nl.rhaydus.common.currentLocalDateTime
import nl.rhaydus.common.toHoursMinutesSeconds
import nl.rhaydus.softcover.core.component.progress.ProgressSheetMedium
import nl.rhaydus.softcover.core.component.progress.ProgressSheetTab
import nl.rhaydus.softcover.core.component.progress.ProgressSheetUiModel
import nl.rhaydus.softcover.core.component.progress.ProgressTimeUnit
import nl.rhaydus.softcover.core.component.progress.progressTimeField
import nl.rhaydus.softcover.core.domain.model.Book
import kotlin.math.roundToInt

/**
 * Maps a [Book] onto what the progress sheet renders.
 *
 * [selectedTab] is the reader's stored last-used unit; [timeDraft] is their uncommitted time entry, or
 * `null` to seed the fields from the book's position. [readAt] is the reader's backdate, or `null` for
 * "just now", shown relative to [now]. A stored `PAGE` on an audiobook is not a valid
 * tab, so [ProgressSheetUiModel.tabsFor] falls back to the medium's primary unit.
 *
 * **Two resolution asymmetries are deliberate.** The page
 * total falls back to the book's default edition when the current one has no page count, but the
 * audio total does *not* — it reads the current edition only. And the medium is chosen from the
 * current edition alone, so a print edition shelved against a book whose default is an audiobook
 * still logs pages. Adding a fallback to either would silently change which tabs a book offers.
 */
fun Book.toProgressSheetUiModel(
    selectedTab: ProgressSheetTab,
    timeDraft: ProgressTimeDraft? = null,
    readAt: LocalDateTime? = null,
    now: LocalDateTime = currentLocalDateTime(),
): ProgressSheetUiModel {
    val edition = currentEdition

    val medium = if (edition?.isAudiobook == true) {
        val draft = ProgressTimeDraft.resolve(
            stored = timeDraft,
            currentSeconds = progressCurrentSeconds,
        )

        val totalSeconds = edition.audioSeconds ?: 0

        val enteredMinutes = draft.minutes.toIntOrNull() ?: 0

        val enteredSeconds = (draft.hours.toIntOrNull() ?: 0) * 3600 +
            enteredMinutes * 60 +
            (draft.seconds.toIntOrNull() ?: 0)

        ProgressSheetMedium.Timed(
            totalSeconds = totalSeconds,
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
            suffix = if (totalSeconds > 0) "of ${totalSeconds.toClockText()}" else null,
            fraction = if (totalSeconds > 0) enteredSeconds.toFloat() / totalSeconds else 0f,
            canDecreaseMinutes = enteredMinutes > 0,
            canIncreaseMinutes = enteredMinutes < ProgressTimeUnit.MINUTES.maximum,
        )
    } else {
        val totalPages = edition?.pages ?: defaultEdition?.pages ?: 0

        ProgressSheetMedium.Paged(
            totalPages = totalPages,
            currentPage = userBookRead?.currentPage ?: 0,
            suffix = if (totalPages > 0) "of $totalPages pages" else null,
        )
    }

    return ProgressSheetUiModel(
        medium = medium,
        progressPercent = userBookRead?.progress?.roundToInt() ?: 0,
        tabs = ProgressSheetUiModel.tabsFor(
            medium = medium,
            selectedTab = selectedTab,
        ),
        whenRead = readAt.toWhenReadUiModel(now = now),
        scaffold = ProgressSheetUiModel.scaffoldFor(bookTitle = title),
    )
}

private fun Int.toClockText(): String {
    val hms = toHoursMinutesSeconds()

    val hours = hms.hours.toString().padStart(
        length = 2,
        padChar = '0',
    )

    val minutes = hms.minutes.toString().padStart(
        length = 2,
        padChar = '0',
    )

    val seconds = hms.seconds.toString().padStart(
        length = 2,
        padChar = '0',
    )

    return "$hours:$minutes:$seconds"
}

/** Where the book's listening position currently stands, the seed for a fresh [ProgressTimeDraft]. */
val Book.progressCurrentSeconds: Int
    get() = userBookRead?.currentSeconds ?: 0
