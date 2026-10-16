package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

import kotlin.math.roundToInt
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import nl.rhaydus.common.currentLocalDate
import nl.rhaydus.softcover.core.component.chip.ChipLeading
import nl.rhaydus.softcover.core.component.chip.ChipTone
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.quote.QuoteCardUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.domain.model.Quote
import nl.rhaydus.softcover.core.domain.model.QuotePosition
import nl.rhaydus.softcover.feature.book_detail.presentation.state.QuotesSectionUiModel

internal fun List<Quote>.toQuotesSection(
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
    today: LocalDate = currentLocalDate(),
): QuotesSectionUiModel = QuotesSectionUiModel(
    header = SectionHeaderUiModel.Label(eyebrow = "Your quotes"),
    quotes = map {
        it.toQuoteCardUiModel(
            timeZone = timeZone,
            today = today,
        )
    }.toImmutableList(),
    emptyMessage = "Quotes you save from this book show up here.".takeIf { isEmpty() },
    addChip = ChipUiModel(
        key = QUOTE_ADD_CHIP_KEY,
        label = "Add a quote",
        tone = ChipTone.Dashed,
        leading = ChipLeading.Icon(icon = SoftcoverIcon.Add),
    ),
)

internal fun Quote.toQuoteCardUiModel(
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
    today: LocalDate = currentLocalDate(),
): QuoteCardUiModel = QuoteCardUiModel(
    key = localId,
    text = text,
    byline = listOfNotNull(
        position?.toBylineLabel(),
        actionAt.toLocalDateTime(timeZone).date.toBylineLabel(today = today),
        privacy.toQuotePrivacyOption().label,
    ).joinToString(separator = " · "),
)

private const val QUOTE_ADD_CHIP_KEY = "quote-add"

private fun QuotePosition.toBylineLabel(): String = when (this) {
    is QuotePosition.Pages -> "p. $page"
    is QuotePosition.Percent -> "${percent.roundToInt()}%"
    is QuotePosition.Seconds -> seconds.toClockText()
}

private fun LocalDate.toBylineLabel(today: LocalDate): String {
    val formatter = LocalDate.Format {
        day(Padding.NONE)
        char(' ')
        monthName(MonthNames.ENGLISH_ABBREVIATED)
    }
    val base = formatter.format(this)

    return if (year == today.year) base else "$base $year"
}
