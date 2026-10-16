package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

import kotlinx.datetime.LocalDateTime
import nl.rhaydus.common.currentLocalDateTime
import nl.rhaydus.softcover.core.component.control.ToggleRowUiModel
import nl.rhaydus.softcover.core.component.progress.ProgressTimeUnit
import nl.rhaydus.softcover.core.component.progress.progressTimeField
import nl.rhaydus.softcover.core.component.quote.QuotePositionInputUiModel
import nl.rhaydus.softcover.core.component.quote.QuotePositionTab
import nl.rhaydus.softcover.core.component.quote.QuoteSheetUiModel
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.QuotePosition
import nl.rhaydus.softcover.core.domain.model.QuoteProgressAdvance
import nl.rhaydus.softcover.core.domain.model.UserBookRead
import nl.rhaydus.softcover.core.domain.model.progressAdvanceFor
import nl.rhaydus.softcover.core.uibinding.progress.toWhenReadUiModel
import nl.rhaydus.softcover.feature.book_detail.presentation.state.QuoteDraft

private const val SECONDS_PER_HOUR = 3600
private const val SECONDS_PER_MINUTE = 60

/** The page count a Page position is measured against; the default edition stands in for a missing one. */
internal val Book.quotePageTotal: Int?
    get() = (currentEdition?.pages ?: defaultEdition?.pages)?.takeIf { it > 0 }

/** The audiobook length a Time position is measured against; unlike pages, it never falls back. */
internal val Book.quoteAudioTotal: Int?
    get() = currentEdition?.audioSeconds?.takeIf { it > 0 }

/** The position units the book offers, primary unit first; a book with no total offers percentage alone. */
internal val Book.quotePositionTabs: List<QuotePositionTab>
    get() = when {
        currentEdition?.isAudiobook == true -> {
            if (quoteAudioTotal != null) {
                listOf(
                    QuotePositionTab.TIME,
                    QuotePositionTab.PERCENTAGE,
                )
            } else {
                listOf(QuotePositionTab.PERCENTAGE)
            }
        }

        quotePageTotal != null -> listOf(
            QuotePositionTab.PAGE,
            QuotePositionTab.PERCENTAGE,
        )

        else -> listOf(QuotePositionTab.PERCENTAGE)
    }

/** The draft's tab, or the primary unit when the book no longer offers it. */
internal fun Book.resolvedQuoteTab(draft: QuoteDraft): QuotePositionTab =
    draft.tab.takeIf { it in quotePositionTabs } ?: quotePositionTabs.first()

/** `null` while the position is blank, so a quote without one is still valid. */
internal fun QuoteDraft.toPosition(book: Book): QuotePosition? = when (book.resolvedQuoteTab(this)) {
    QuotePositionTab.PAGE -> {
        val total = book.quotePageTotal
        val page = positionText.toIntOrNull()?.takeIf { it > 0 }

        if (total == null || page == null) {
            null
        } else {
            QuotePosition.pages(
                page = page,
                totalPages = total,
            )
        }
    }

    QuotePositionTab.PERCENTAGE -> positionText.toIntOrNull()
        ?.takeIf { it > 0 }
        ?.let { QuotePosition.Percent(percent = it.toDouble()) }

    QuotePositionTab.TIME -> {
        val total = book.quoteAudioTotal
        val entered = enteredSeconds.takeIf { it > 0 }
        if (total == null || entered == null) {
            null
        } else {
            QuotePosition.seconds(
                seconds = entered,
                totalSeconds = total,
            )
        }
    }
}

internal fun Book.toQuoteSheetUiModel(
    draft: QuoteDraft,
    now: LocalDateTime = currentLocalDateTime(),
): QuoteSheetUiModel {
    val tab = resolvedQuoteTab(draft)

    return QuoteSheetUiModel(
        text = QuoteSheetUiModel.textField(text = draft.text),
        positionTabs = QuoteSheetUiModel.positionTabsFor(
            offered = quotePositionTabs,
            selected = tab,
        ),
        position = positionInputFor(
            tab = tab,
            draft = draft,
        ),
        progressAdvance = toProgressAdvanceRow(draft = draft),
        privacy = QuoteSheetUiModel.privacyFor(selected = draft.privacy.toQuotePrivacyOption()),
        whenRead = draft.readAt.toWhenReadUiModel(now = now),
        scaffold = QuoteSheetUiModel.scaffoldFor(
            bookTitle = title,
            canSave = draft.text.isNotBlank(),
        ),
    )
}

private val QuoteDraft.enteredSeconds: Int
    get() = (hours.toIntOrNull() ?: 0) * SECONDS_PER_HOUR +
        (minutes.toIntOrNull() ?: 0) * SECONDS_PER_MINUTE +
        (seconds.toIntOrNull() ?: 0)

private fun Book.positionInputFor(
    tab: QuotePositionTab,
    draft: QuoteDraft,
): QuotePositionInputUiModel = when (tab) {
    QuotePositionTab.PAGE -> QuotePositionInputUiModel.Page(
        field = QuoteSheetUiModel.positionField(
            tab = tab,
            text = draft.positionText,
        ),
        suffix = quotePageTotal?.let { "of $it pages" },
    )

    QuotePositionTab.PERCENTAGE -> QuotePositionInputUiModel.Percentage(
        field = QuoteSheetUiModel.positionField(
            tab = tab,
            text = draft.positionText,
        ),
    )

    QuotePositionTab.TIME -> QuotePositionInputUiModel.Time(
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
        suffix = quoteAudioTotal?.let { "of ${it.toClockText()}" },
    )
}

private fun Book.toProgressAdvanceRow(draft: QuoteDraft): ToggleRowUiModel<String>? {
    val position = draft.toPosition(book = this) ?: return null
    val advance = progressAdvanceFor(position) ?: return null
    val isAudiobook = currentEdition?.isAudiobook == true

    return ToggleRowUiModel(
        key = PROGRESS_ADVANCE_KEY,
        title = advance.title(),
        gloss = advance.gloss(
            isAudiobook = isAudiobook,
            read = currentRead,
        ),
        checked = draft.progressAdvanceEnabled,
    )
}

private const val PROGRESS_ADVANCE_KEY = "progress-advance"

private fun QuoteProgressAdvance.title(): String {
    val seconds = newSeconds

    return when {
        finishesBook -> "Mark as read"
        seconds != null -> "Update my progress to ${seconds.toClockText()}"
        else -> "Update my progress to page $newPage"
    }
}

private fun QuoteProgressAdvance.gloss(
    isAudiobook: Boolean,
    read: UserBookRead?,
): String = when {
    finishesBook && isAudiobook -> "That's the end of the audiobook."
    finishesBook -> "Page $newPage is the last page."
    isAudiobook -> "You're at ${(read?.currentSeconds ?: 0).toClockText()}."
    else -> "You're on page ${read?.currentPage ?: 0}."
}

/** "1:02:03". */
internal fun Int.toClockText(): String {
    val hours = this / SECONDS_PER_HOUR
    val minutes = this % SECONDS_PER_HOUR / SECONDS_PER_MINUTE
    val seconds = this % SECONDS_PER_MINUTE

    return "$hours:${minutes.twoDigits()}:${seconds.twoDigits()}"
}

private fun Int.twoDigits(): String = toString().padStart(
    length = 2,
    padChar = '0',
)

/** Digits only, cut to [maxLength] and capped at [maximum]; blank stays blank so the field can be cleared. */
internal fun sanitizedQuoteNumber(
    text: String,
    maximum: Int,
    maxLength: Int,
): String {
    val digits = text.filter { it.isDigit() }.take(maxLength)

    return digits.toIntOrNull()?.coerceAtMost(maximum)?.toString().orEmpty()
}
