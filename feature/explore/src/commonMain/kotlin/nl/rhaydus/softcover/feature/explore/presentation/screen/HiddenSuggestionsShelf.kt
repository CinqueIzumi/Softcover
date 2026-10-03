package nl.rhaydus.softcover.feature.explore.presentation.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import nl.rhaydus.softcover.core.component.bookcard.BookCard
import nl.rhaydus.softcover.core.component.bookcard.BookCardEvent
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.bookcard.lazyKey
import nl.rhaydus.softcover.core.component.bookcard.rememberBookCardEventHandler
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.modifier.quoteGlyphSway
import nl.rhaydus.softcover.core.designsystem.presentation.theme.LocalDarkTheme
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.feature.explore.presentation.action.HiddenSuggestionsAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnUnblockBookAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnUnblockSeriesAction
import nl.rhaydus.softcover.feature.explore.presentation.model.HiddenGroupHeaderUiModel
import nl.rhaydus.softcover.feature.explore.presentation.screen.section.HIDDEN_BOOK_STAND_IN_CARD
import nl.rhaydus.softcover.feature.explore.presentation.screen.section.HIDDEN_SERIES_STAND_IN_CARD
import nl.rhaydus.softcover.feature.explore.presentation.state.HiddenSuggestionsUiState

/**
 * The Hidden-suggestions body, shared by the mobile [HiddenSuggestionsScreen] page and the desktop
 * standalone layout. An intro line opens the surface (the screen's title lives in the top bar, matching
 * the sibling Appearance / Library-tabs settings pages), then two hairline-separated groups — "Hidden
 * books" / "Hidden series" — each list their rows with a per-item warm "Restore" affordance; a group is
 * omitted entirely when its list is empty, and an overall editorial empty state replaces everything when
 * nothing at all is hidden ([HiddenSuggestionsUiState.isEmpty]). The caller supplies the scroll / width
 * [modifier].
 */
@Composable
internal fun HiddenSuggestionsContent(
    state: HiddenSuggestionsUiState,
    runAction: (HiddenSuggestionsAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val unblockActions = remember(state.hiddenBooks, state.hiddenSeries) {
        buildMap<BookCardKey, HiddenSuggestionsAction> {
            state.hiddenBooks.forEach { book ->
                put(
                    BookCardKey.Book(book.bookId),
                    OnUnblockBookAction(book = book),
                )
            }

            state.hiddenSeries.forEach { series ->
                put(
                    BookCardKey.Series(series.seriesId),
                    OnUnblockSeriesAction(
                        seriesId = series.seriesId,
                        seriesName = series.seriesName,
                        coverUrl = series.coverUrl,
                        authorText = series.authorText,
                        bookCount = series.bookCount,
                    ),
                )
            }
        }
    }

    val onEvent = rememberBookCardEventHandler(
        resolve = unblockActions::get,
    ) { event, action ->
        if (event is BookCardEvent.Clicked) {
            runAction(action)
        }
    }

    Column(modifier = modifier) {
        if (state.isEmpty) {
            HiddenSuggestionsEmptyState()
        } else {
            SoftcoverText(
                text = "Books and series you’ve muted stay out of your " +
                    "“Up next in your series” suggestions. Restore any of them here.",
                style = MaterialTheme.editorialTypography.body,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (state.hiddenBooksLoaded.not() || state.hiddenBooks.isNotEmpty()) {
                HiddenBooksSection(
                    header = state.hiddenBooksGroupHeader,
                    cards = state.hiddenBookCards,
                    isLoading = state.hiddenBooksLoaded.not(),
                    onEvent = onEvent,
                )

                Spacer(modifier = Modifier.height(40.dp))
            }

            if (state.hiddenSeriesLoaded.not() || state.hiddenSeries.isNotEmpty()) {
                HiddenSeriesSection(
                    header = state.hiddenSeriesGroupHeader,
                    cards = state.hiddenSeriesCards,
                    isLoading = state.hiddenSeriesLoaded.not(),
                    onEvent = onEvent,
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun HiddenBooksSection(
    header: HiddenGroupHeaderUiModel,
    cards: ImmutableList<BookCardUiModel>?,
    isLoading: Boolean,
    onEvent: (BookCardEvent) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        HiddenSuggestionsGroupHeader(
            model = header,
            isLoading = isLoading,
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (cards == null) {
            repeat(2) {
                BookCard(
                    model = HIDDEN_BOOK_STAND_IN_CARD,
                    onEvent = {},
                )
            }
        } else {
            cards.forEach { card ->
                key(card.key.lazyKey) {
                    BookCard(
                        model = card,
                        onEvent = onEvent,
                    )
                }
            }
        }
    }
}

@Composable
private fun HiddenSeriesSection(
    header: HiddenGroupHeaderUiModel,
    cards: ImmutableList<BookCardUiModel>?,
    isLoading: Boolean,
    onEvent: (BookCardEvent) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        HiddenSuggestionsGroupHeader(
            model = header,
            isLoading = isLoading,
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (cards == null) {
            repeat(2) {
                BookCard(
                    model = HIDDEN_SERIES_STAND_IN_CARD,
                    onEvent = {},
                )
            }
        } else {
            cards.forEach { card ->
                key(card.key.lazyKey) {
                    BookCard(
                        model = card,
                        onEvent = onEvent,
                    )
                }
            }
        }
    }
}

/**
 * The compact header opening each group within the page: [SectionHeader]'s [SectionHeaderUiModel.Inline]
 * register, with a demoted tabular-figure count trailing the eyebrow and a one-line italic subline
 * standing in for a headline (the screen's own title lives in the top bar). "Hidden books" / "Hidden
 * series" are the only two consumers today, so this stays screen-local; promote it to a shared component
 * only once a second screen needs the same "nested group inside an already-opened page" header shape.
 */
@Composable
private fun HiddenSuggestionsGroupHeader(
    model: HiddenGroupHeaderUiModel,
    isLoading: Boolean,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(model = model.header) {
            SoftcoverText(
                text = model.count,
                isLoading = isLoading,
                standIn = "00",
                style = MaterialTheme.editorialTypography.eyebrowSmall.copy(fontFeatureSettings = "tnum"),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        SoftcoverText(
            text = model.subline,
            style = MaterialTheme.editorialTypography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * The "nothing hidden at all" state — the same low-alpha, swaying quote-glyph flourish used by the
 * empty Reading screen (DS §5 editorial quote, empty-state variant), with copy tailored to explain
 * what this screen is for rather than a bare "nothing here" panel.
 */
@Composable
private fun HiddenSuggestionsEmptyState() {
    val quoteAlpha = if (LocalDarkTheme.current) 0.15f else 0.3f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SoftcoverText(
            text = "“",
            style = MaterialTheme.editorialTypography.quoteGlyph,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = quoteAlpha),
            modifier = Modifier.quoteGlyphSway(),
        )

        Spacer(modifier = Modifier.height(8.dp))

        SoftcoverText(
            text = "Nothing hidden",
            style = MaterialTheme.editorialTypography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(12.dp))

        SoftcoverText(
            text = "Books and series you mute from “Up next in your series” land here, " +
                "so you can bring any of them back whenever you like.",
            style = MaterialTheme.editorialTypography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
