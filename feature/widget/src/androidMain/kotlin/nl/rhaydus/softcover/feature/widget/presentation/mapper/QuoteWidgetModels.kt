package nl.rhaydus.softcover.feature.widget.presentation.mapper

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import nl.rhaydus.softcover.core.domain.model.Quote
import nl.rhaydus.softcover.core.domain.model.ThemeConfiguration
import nl.rhaydus.softcover.core.uibinding.quote.toPositionLabel
import nl.rhaydus.softcover.feature.widget.domain.model.QuoteSelection
import nl.rhaydus.softcover.feature.widget.domain.util.epochDayChanges
import nl.rhaydus.softcover.feature.widget.domain.util.quoteKey
import nl.rhaydus.softcover.feature.widget.domain.util.rankForDay
import nl.rhaydus.softcover.feature.widget.domain.util.resolveShown
import nl.rhaydus.softcover.feature.widget.presentation.model.QuoteWidgetUiModel

private const val QUOTE_OF_THE_DAY_EYEBROW = "QUOTE OF THE DAY"

/** [today] re-emits at local midnight, so a widget left on screen re-ranks without another source emitting. */
internal fun quoteWidgetModels(
    isSignedIn: Flow<Boolean>,
    themeConfiguration: Flow<ThemeConfiguration>,
    quotes: Flow<List<Quote>>,
    selection: Flow<QuoteSelection?>,
    today: Flow<Long> = epochDayChanges(),
): Flow<QuoteWidgetUiModel> = combine(
    isSignedIn,
    themeConfiguration,
    quotes,
    selection,
    today,
) { signedIn, theme, allQuotes, selected, epochDay ->
    QuoteWidgetUiModel(
        themeConfiguration = theme,
        content = toQuoteContent(
            isSignedIn = signedIn,
            quotes = allQuotes,
            selection = selected,
            today = epochDay,
        ),
    )
}

private fun toQuoteContent(
    isSignedIn: Boolean,
    quotes: List<Quote>,
    selection: QuoteSelection?,
    today: Long,
): QuoteWidgetUiModel.Content {
    if (isSignedIn.not()) return QuoteWidgetUiModel.Content.SignedOut

    val ranking = rankForDay(
        quotes = quotes,
        epochDay = today,
    )

    val shown = resolveShown(
        ranking = ranking,
        selectedQuoteId = selection?.quoteId,
        selectedDay = selection?.epochDay,
        today = today,
    ) ?: return QuoteWidgetUiModel.Content.Empty

    val rank = ranking.indexOfFirst { quoteKey(it) == quoteKey(shown) }

    return QuoteWidgetUiModel.Content.Shown(
        quoteText = shown.text,
        byline = listOfNotNull(
            shown.bookTitle?.takeIf { it.isNotBlank() },
            shown.position?.toPositionLabel(),
        ).joinToString(separator = " · ").takeIf { it.isNotEmpty() },
        bookId = shown.bookId,
        eyebrow = if (rank == 0) QUOTE_OF_THE_DAY_EYEBROW else "QUOTES · ${rank + 1}/${ranking.size}",
        hasArrows = ranking.size > 1,
    )
}
