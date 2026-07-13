package nl.rhaydus.softcover.feature.explore.presentation.screen

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.rhaydus.designsystem.component.RhaydusButton
import nl.rhaydus.designsystem.editorial.component.EditorialSectionHeader
import nl.rhaydus.designsystem.model.ButtonSize
import nl.rhaydus.designsystem.model.ButtonStyle
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.softcover.core.designsystem.presentation.component.EditionImage
import nl.rhaydus.softcover.core.designsystem.presentation.modifier.quoteGlyphSway
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.feature.explore.domain.model.DismissedSeries
import nl.rhaydus.softcover.feature.explore.domain.model.DismissedSeriesBook
import nl.rhaydus.softcover.feature.explore.presentation.action.HiddenSuggestionsAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnUnblockBookAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnUnblockSeriesAction
import nl.rhaydus.softcover.feature.explore.presentation.state.HiddenSuggestionsUiState

/**
 * The Hidden-suggestions body, shared by the mobile [HiddenSuggestionsScreen] page and the desktop
 * standalone layout. Two labelled sections ("Hidden books" / "Hidden series") each list their rows
 * with a per-item unblock affordance; a section is omitted entirely when its list is empty, and an
 * overall editorial empty state replaces both when nothing at all is hidden
 * ([HiddenSuggestionsUiState.isEmpty]). The caller supplies the scroll / width [modifier].
 */
@Composable
internal fun HiddenSuggestionsContent(
    state: HiddenSuggestionsUiState,
    runAction: (HiddenSuggestionsAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Spacer(modifier = Modifier.height(8.dp))

        if (state.isEmpty) {
            HiddenSuggestionsEmptyState()
        } else {
            if (state.hiddenBooks.isNotEmpty()) {
                HiddenBooksSection(
                    books = state.hiddenBooks,
                    runAction = runAction,
                )

                Spacer(modifier = Modifier.height(32.dp))
            }

            if (state.hiddenSeries.isNotEmpty()) {
                HiddenSeriesSection(
                    series = state.hiddenSeries,
                    runAction = runAction,
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun HiddenBooksSection(
    books: List<DismissedSeriesBook>,
    runAction: (HiddenSuggestionsAction) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        EditorialSectionHeader(
            eyebrow = "Hidden books",
            headline = "Books you've hidden",
            description = "Removed from \"Up next in your series\". Unblock to bring a title back.",
        )

        Spacer(modifier = Modifier.height(16.dp))

        books.forEachIndexed { index, book ->
            if (index > 0) {
                Spacer(modifier = Modifier.height(12.dp))
            }

            HiddenBookRow(
                book = book,
                onUnblock = {
                    runAction(OnUnblockBookAction(book = book))
                },
            )
        }
    }
}

@Composable
private fun HiddenSeriesSection(
    series: List<DismissedSeries>,
    runAction: (HiddenSuggestionsAction) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        EditorialSectionHeader(
            eyebrow = "Hidden series",
            headline = "Series you've hidden",
            description = "Removed from \"Up next in your series\". Unblock to resume suggestions.",
        )

        Spacer(modifier = Modifier.height(16.dp))

        series.forEachIndexed { index, entry ->
            if (index > 0) {
                Spacer(modifier = Modifier.height(12.dp))
            }

            HiddenSeriesRow(
                series = entry,
                onUnblock = {
                    runAction(
                        OnUnblockSeriesAction(
                            seriesId = entry.seriesId,
                            seriesName = entry.seriesName,
                            coverUrl = entry.coverUrl,
                        ),
                    )
                },
            )
        }
    }
}

@Composable
private fun HiddenBookRow(
    book: DismissedSeriesBook,
    onUnblock: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(20.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = 12.dp,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            EditionImage(
                edition = null,
                defaultEdition = null,
                isLoading = false,
                fallbackCoverUrl = book.coverUrl,
                coverlessTitle = book.title ?: "Hidden book",
                cornerRadius = 6.dp,
                modifier = Modifier.width(48.dp),
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = book.title ?: "Hidden book",
                    style = MaterialTheme.editorialTypography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = book.authorText
                        ?: book.seriesName?.let { "From $it" }
                        ?: "Hidden from Up next in your series",
                    style = MaterialTheme.editorialTypography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            RhaydusButton(
                label = "Unblock",
                style = ButtonStyle.TEXT,
                size = ButtonSize.S,
                onClick = onUnblock,
                modifier = Modifier.pointerHandCursor(),
            )
        }
    }
}

@Composable
private fun HiddenSeriesRow(
    series: DismissedSeries,
    onUnblock: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(20.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = 12.dp,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            EditionImage(
                edition = null,
                defaultEdition = null,
                isLoading = false,
                fallbackCoverUrl = series.coverUrl,
                coverlessTitle = series.seriesName ?: "Hidden series",
                cornerRadius = 6.dp,
                modifier = Modifier.width(48.dp),
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = series.seriesName ?: "Hidden series",
                    style = MaterialTheme.editorialTypography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Series",
                    style = MaterialTheme.editorialTypography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            RhaydusButton(
                label = "Unblock",
                style = ButtonStyle.TEXT,
                size = ButtonSize.S,
                onClick = onUnblock,
                modifier = Modifier.pointerHandCursor(),
            )
        }
    }
}

/**
 * The "nothing hidden at all" state — the same low-alpha, swaying quote-glyph flourish used by the
 * empty Reading screen (DS §5 editorial quote, empty-state variant), with copy tailored to explain
 * what this screen is for rather than a bare "nothing here" panel.
 */
@Composable
private fun HiddenSuggestionsEmptyState() {
    val quoteAlpha = if (isSystemInDarkTheme()) 0.15f else 0.3f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "“",
            style = MaterialTheme.editorialTypography.quoteGlyph.copy(fontSize = 120.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = quoteAlpha),
            modifier = Modifier.quoteGlyphSway(),
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Nothing hidden",
            style = MaterialTheme.editorialTypography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Books and series you hide from \"Up next in your series\" appear here, " +
                "so you can bring any of them back whenever you like.",
            style = MaterialTheme.editorialTypography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
