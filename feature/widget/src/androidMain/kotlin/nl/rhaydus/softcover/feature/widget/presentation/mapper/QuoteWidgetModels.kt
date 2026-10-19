package nl.rhaydus.softcover.feature.widget.presentation.mapper

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.transformLatest
import nl.rhaydus.softcover.core.domain.model.Quote
import nl.rhaydus.softcover.core.domain.model.ThemeConfiguration
import nl.rhaydus.softcover.core.uibinding.quote.toPositionLabel
import nl.rhaydus.softcover.feature.widget.domain.model.QuoteFetchTracker
import nl.rhaydus.softcover.feature.widget.domain.model.QuoteSelection
import nl.rhaydus.softcover.feature.widget.domain.util.epochDayChanges
import nl.rhaydus.softcover.feature.widget.domain.util.quoteKey
import nl.rhaydus.softcover.feature.widget.domain.util.rankForDay
import nl.rhaydus.softcover.feature.widget.domain.util.resolveShown
import nl.rhaydus.softcover.feature.widget.presentation.model.QuoteWidgetUiModel

private class QuoteInputs(
    val isSignedIn: Boolean,
    val themeConfiguration: ThemeConfiguration,
    val quotes: List<Quote>,
    val fetchState: QuoteFetchTracker.State?,
    val selection: QuoteSelection?,
    val today: Long,
)

private const val QUOTE_OF_THE_DAY_EYEBROW = "QUOTE OF THE DAY"

/**
 * [today] re-emits at local midnight, so a widget left on screen re-ranks without another source emitting.
 *
 * An empty list shows the loader while [fetchState] says a fetch is running, so
 * [QuoteWidgetUiModel.Content.FetchFailed] means a fetch failed with nothing cached.
 *
 * Emits each model twice for a shown quote: first with its text and no artwork, then with the stored artwork
 * [android.net.Uri]s, so the text never waits on the cover download or rendering.
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal fun quoteWidgetModels(
    isSignedIn: Flow<Boolean>,
    themeConfiguration: Flow<ThemeConfiguration>,
    quotes: Flow<List<Quote>>,
    selection: Flow<QuoteSelection?>,
    fetchState: Flow<QuoteFetchTracker.State?>,
    artworkUris: suspend (Quote) -> QuoteWidgetUiModel.Artwork? = { null },
    today: Flow<Long> = epochDayChanges(),
): Flow<QuoteWidgetUiModel> = combine(
    isSignedIn,
    themeConfiguration,
    quotes.combine(
        fetchState,
        ::Pair,
    ),
    selection,
    today,
) { signedIn, theme, (allQuotes, state), selected, epochDay ->
    QuoteInputs(
        isSignedIn = signedIn,
        themeConfiguration = theme,
        quotes = allQuotes,
        fetchState = state,
        selection = selected,
        today = epochDay,
    )
}.transformLatest { inputs ->
    val ranking = rankForDay(
        quotes = inputs.quotes,
        epochDay = inputs.today,
    )

    val shown = if (inputs.isSignedIn) {
        resolveShown(
            ranking = ranking,
            selectedQuoteId = inputs.selection?.quoteId,
            selectedDay = inputs.selection?.epochDay,
            today = inputs.today,
        )
    } else {
        null
    }

    val content = toQuoteContent(
        inputs = inputs,
        ranking = ranking,
        shown = shown,
    )

    emit(
        QuoteWidgetUiModel(
            themeConfiguration = inputs.themeConfiguration,
            content = content,
        ),
    )

    if (shown != null && content is QuoteWidgetUiModel.Content.Shown) {
        emit(
            QuoteWidgetUiModel(
                themeConfiguration = inputs.themeConfiguration,
                content = content.copy(
                    artwork = artworkUris(shown),
                    imagesPending = false,
                ),
            ),
        )
    }
}

private fun toQuoteContent(
    inputs: QuoteInputs,
    ranking: List<Quote>,
    shown: Quote?,
): QuoteWidgetUiModel.Content {
    if (inputs.isSignedIn.not()) return QuoteWidgetUiModel.Content.SignedOut

    if (shown == null) {
        return emptyContent(
            hasQuotes = inputs.quotes.isNotEmpty(),
            fetchState = inputs.fetchState,
        )
    }

    val rank = ranking.indexOfFirst { quoteKey(it) == quoteKey(shown) }

    return QuoteWidgetUiModel.Content.Shown(
        quoteText = shown.text,
        byline = listOfNotNull(
            shown.editionTitle?.takeIf { it.isNotBlank() } ?: shown.bookTitle?.takeIf { it.isNotBlank() },
            shown.position?.toPositionLabel(),
        ).joinToString(separator = " · ").takeIf { it.isNotEmpty() },
        bookId = shown.bookId,
        eyebrow = if (rank == 0) QUOTE_OF_THE_DAY_EYEBROW else "QUOTES · ${rank + 1}/${ranking.size}",
        hasArrows = ranking.size > 1,
        artwork = null,
        imagesPending = true,
    )
}

private fun emptyContent(
    hasQuotes: Boolean,
    fetchState: QuoteFetchTracker.State?,
): QuoteWidgetUiModel.Content = when {
    hasQuotes -> QuoteWidgetUiModel.Content.Empty
    fetchState == QuoteFetchTracker.State.Running -> QuoteWidgetUiModel.Content.Loading
    fetchState == QuoteFetchTracker.State.Failed -> QuoteWidgetUiModel.Content.FetchFailed
    else -> QuoteWidgetUiModel.Content.Empty
}
