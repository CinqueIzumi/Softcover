package nl.rhaydus.softcover.feature.explore.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.component.RhaydusButton
import nl.rhaydus.designsystem.modifier.hoverHighlight
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.designsystem.modifier.pressScale
import nl.rhaydus.softcover.core.designsystem.presentation.component.EditionImage
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.modifier.quoteGlyphSway
import nl.rhaydus.softcover.core.designsystem.presentation.theme.LocalDarkTheme
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.feature.explore.domain.model.DismissedSeries
import nl.rhaydus.softcover.feature.explore.domain.model.DismissedSeriesBook
import nl.rhaydus.softcover.feature.explore.presentation.action.HiddenSuggestionsAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnUnblockBookAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnUnblockSeriesAction
import nl.rhaydus.softcover.feature.explore.presentation.state.HiddenSuggestionsUiState

// Cover widths for the two row leads. The book cover matches the redline's single-cover measure; the
// series stack's front face is a touch narrower than that so the two offset under-layers (below) still
// read as a stack rather than crowding into the text column.
private val BOOK_COVER_WIDTH = 34.dp
private val SERIES_COVER_WIDTH = 30.dp
private val SERIES_COVER_HEIGHT = 45.dp
private val SERIES_STACK_LAYER_OFFSET = 6.dp

private val ROW_VERTICAL_PADDING = 13.dp
private val RESTORE_ICON_SIZE = 18.dp

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
    Column(modifier = modifier) {
        if (state.isEmpty) {
            HiddenSuggestionsEmptyState()
        } else {
            Text(
                text = "Books and series you’ve muted stay out of your " +
                    "“Up next in your series” suggestions. Restore any of them here.",
                style = MaterialTheme.editorialTypography.body,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (state.hiddenBooks.isNotEmpty()) {
                HiddenBooksSection(
                    books = state.hiddenBooks,
                    runAction = runAction,
                )

                Spacer(modifier = Modifier.height(40.dp))
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
        HiddenSuggestionsGroupHeader(
            eyebrow = "Hidden books",
            count = books.size,
            subline = "Set aside — tap Restore to shelve them again.",
        )

        Spacer(modifier = Modifier.height(8.dp))

        books.forEach { book ->
            HiddenBookRow(
                book = book,
                onRestore = {
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
        HiddenSuggestionsGroupHeader(
            eyebrow = "Hidden series",
            count = series.size,
            subline = "A muted series hides every book in it.",
        )

        Spacer(modifier = Modifier.height(8.dp))

        series.forEach { entry ->
            HiddenSeriesRow(
                series = entry,
                onRestore = {
                    runAction(
                        OnUnblockSeriesAction(
                            seriesId = entry.seriesId,
                            seriesName = entry.seriesName,
                            coverUrl = entry.coverUrl,
                            authorText = entry.authorText,
                            bookCount = entry.bookCount,
                        ),
                    )
                },
            )
        }
    }
}

/**
 * The compact header opening each group within the page: the DS §2.3 inline 20×1 hairline bar +
 * `eyebrowSmall` (the same anatomy book detail's `InlineAccentLabel` uses for a compact in-card label),
 * extended with a demoted tabular-figure [count] trailing the eyebrow and a one-line italic [subline]
 * standing in for a headline (the screen's own title lives in the top bar). "Hidden books" / "Hidden
 * series" are the only
 * two consumers today, so this stays screen-local; promote it to a shared component only once a second
 * screen needs the same "nested group inside an already-opened page" header shape.
 */
@Composable
private fun HiddenSuggestionsGroupHeader(
    eyebrow: String,
    count: Int,
    subline: String,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .height(1.dp)
                    .width(20.dp)
                    .background(MaterialTheme.colorScheme.primary),
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = eyebrow.uppercase(),
                style = MaterialTheme.editorialTypography.eyebrowSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
            )

            Text(
                text = count.toString(),
                style = MaterialTheme.editorialTypography.eyebrowSmall.copy(fontFeatureSettings = "tnum"),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = subline,
            style = MaterialTheme.editorialTypography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * A hidden book: single 2:3 cover, Inter title (roman, never italic), an ALL-CAPS byline, trailing
 * [RestoreAffordance]. Hairline-separated (never a boxed card) — a top `outlineVariant` rule sits above
 * every row, and the whole row is the tap target for [onRestore] (press-scale + native ripple on every
 * platform, an additional `surfaceContainer` hover wash on desktop).
 */
@Composable
private fun HiddenBookRow(
    book: DismissedSeriesBook,
    onRestore: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }

    // Guards blank (not just null) — Book.authorString upstream is a non-null String that can resolve
    // to "", which would otherwise pass the null checks below and render a stray "BY " with nothing
    // after it.
    val authorText = book.authorText?.takeIf { it.isNotBlank() }
    val seriesName = book.seriesName?.takeIf { it.isNotBlank() }

    val byline = when {
        authorText != null -> "By $authorText"
        seriesName != null -> seriesName
        else -> null
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Surface(
            onClick = onRestore,
            modifier = Modifier
                .fillMaxWidth()
                .pointerHandCursor()
                .pressScale(interactionSource)
                .hoverHighlight(
                    interactionSource = interactionSource,
                    color = MaterialTheme.colorScheme.surfaceContainer,
                ),
            color = Color.Transparent,
            shape = RectangleShape,
            interactionSource = interactionSource,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = ROW_VERTICAL_PADDING),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                EditionImage(
                    edition = null,
                    defaultEdition = null,
                    isLoading = false,
                    fallbackCoverUrl = book.coverUrl,
                    coverlessTitle = book.title ?: "Hidden book",
                    cornerRadius = 4.dp,
                    modifier = Modifier.width(BOOK_COVER_WIDTH),
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = book.title ?: "Hidden book",
                        style = MaterialTheme.editorialTypography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    // Always rendered (even with an empty string when neither fact is known) so the
                    // row reserves the same height as every other row — an empty Text still occupies
                    // one eyebrowSmall line, per the style's own line height.
                    Text(
                        text = byline?.uppercase().orEmpty(),
                        style = MaterialTheme.editorialTypography.eyebrowSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                RestoreAffordance()
            }
        }
    }
}

/**
 * A hidden series: same row rhythm as [HiddenBookRow], but led by a [SeriesCoverStack] instead of a
 * single cover — so a series is never mistaken for a book — and an italic "author · N books" scope
 * caption ([seriesCaption]) in place of the byline.
 */
@Composable
private fun HiddenSeriesRow(
    series: DismissedSeries,
    onRestore: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }

    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Surface(
            onClick = onRestore,
            modifier = Modifier
                .fillMaxWidth()
                .pointerHandCursor()
                .pressScale(interactionSource)
                .hoverHighlight(
                    interactionSource = interactionSource,
                    color = MaterialTheme.colorScheme.surfaceContainer,
                ),
            color = Color.Transparent,
            shape = RectangleShape,
            interactionSource = interactionSource,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = ROW_VERTICAL_PADDING),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SeriesCoverStack(
                    coverUrl = series.coverUrl,
                    coverlessTitle = series.seriesName ?: "Hidden series",
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = series.seriesName ?: "Hidden series",
                        style = MaterialTheme.editorialTypography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = seriesCaption(
                            authorText = series.authorText,
                            bookCount = series.bookCount,
                        ),
                        style = MaterialTheme.editorialTypography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                RestoreAffordance()
            }
        }
    }
}

/**
 * Degrades the series scope caption gracefully as its two facts arrive independently — never renders a
 * bare "null" when one or both are unknown.
 */
private fun seriesCaption(
    authorText: String?,
    bookCount: Int?,
): String {
    // Guards blank the same way HiddenBookRow's byline does — an upstream "" author string must not
    // pass this check and render a bare " · N books" with nothing before the separator.
    val author = authorText?.takeIf { it.isNotBlank() }
    val countPart = bookCount?.let { if (it == 1) "1 book" else "$it books" }

    return when {
        author != null && countPart != null -> "$author · $countPart"
        author != null -> author
        countPart != null -> countPart
        else -> "Series"
    }
}

/**
 * The three-cover silhouette that leads a hidden-series row: the real [EditionImage] front face, over
 * two offset, stepped-alpha under-layers in `surfaceContainerHigh` (DS token map — the cover-stack
 * under-layer tone) — so a series reads as a stack at a glance, distinct from a single book cover.
 * Screen-local for the same reason as [HiddenSuggestionsGroupHeader]; promote only once a second screen
 * needs to represent a series as a stack of covers.
 */
@Composable
private fun SeriesCoverStack(
    coverUrl: String?,
    coverlessTitle: String,
) {
    Box(
        modifier = Modifier
            .width(SERIES_COVER_WIDTH + SERIES_STACK_LAYER_OFFSET * 2)
            .height(SERIES_COVER_HEIGHT),
    ) {
        SeriesCoverStackLayer(
            alpha = 0.4f,
            modifier = Modifier.offset(x = SERIES_STACK_LAYER_OFFSET * 2),
        )

        SeriesCoverStackLayer(
            alpha = 0.7f,
            modifier = Modifier.offset(x = SERIES_STACK_LAYER_OFFSET),
        )

        EditionImage(
            edition = null,
            defaultEdition = null,
            isLoading = false,
            fallbackCoverUrl = coverUrl,
            coverlessTitle = coverlessTitle,
            cornerRadius = 3.dp,
            modifier = Modifier.width(SERIES_COVER_WIDTH),
        )
    }
}

@Composable
private fun SeriesCoverStackLayer(
    alpha: Float,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .width(SERIES_COVER_WIDTH)
            .height(SERIES_COVER_HEIGHT)
            // Matches the front cover's own `cornerRadius = 3.dp` (below) explicitly, rather than
            // riding the theme's `extraSmall` shape token, so the two can never silently diverge.
            .clip(RoundedCornerShape(3.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = alpha)),
    )
}

/**
 * The warm, reversible trailing affordance every row ends with: a return-arrow glyph + "Restore" label,
 * both `primary`. Deliberately not a [RhaydusButton] — its `TEXT` style suppresses the leading icon —
 * so this is a small hand-composed row instead; the click itself lives on the row's own [Surface]
 * (above), so this composable is purely the visual label.
 */
@Composable
private fun RestoreAffordance() {
    val restoreIcon = drawableIconResource(
        icon = SoftcoverIcon.History,
        contentDescription = "Restore",
    )

    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = restoreIcon.getIconPainter(),
            contentDescription = restoreIcon.contentDescription,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(RESTORE_ICON_SIZE),
        )

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = "Restore",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.primary,
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
        Text(
            text = "“",
            style = MaterialTheme.editorialTypography.quoteGlyph,
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
            text = "Books and series you mute from “Up next in your series” land here, " +
                "so you can bring any of them back whenever you like.",
            style = MaterialTheme.editorialTypography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
