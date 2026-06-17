package nl.rhaydus.softcover.feature.explore.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.modifier.noRippleClickable
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.designsystem.modifier.pressScaleClickable
import nl.rhaydus.designsystem.modifier.shimmer
import nl.rhaydus.softcover.core.designsystem.presentation.component.AdaptiveModalSheet
import nl.rhaydus.softcover.core.designsystem.presentation.component.EditionImage
import nl.rhaydus.softcover.core.designsystem.presentation.component.EditorialSectionHeader
import nl.rhaydus.softcover.core.designsystem.presentation.component.LocalModalSheetDismiss
import nl.rhaydus.softcover.core.designsystem.presentation.component.SoftcoverButton
import nl.rhaydus.softcover.core.designsystem.presentation.component.UnreleasedBadge
import nl.rhaydus.softcover.core.designsystem.presentation.component.mutationAnimated
import nl.rhaydus.softcover.core.designsystem.presentation.component.rememberLazyItemMutationAnimator
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.model.ButtonStyle
import nl.rhaydus.softcover.core.designsystem.presentation.model.SoftcoverIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.prefetch.prefetchBookDetailOnPress
import nl.rhaydus.softcover.core.designsystem.presentation.theme.RatingGold
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.designsystem.presentation.transition.bookCoverTransitionKey
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.explore.presentation.action.ExploreAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnAddBookToLibraryClickAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnDismissContinueSeriesAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnDismissContinueSeriesBookAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnQueryChangeAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnRemoveAllSearchQueriesClickedAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnRemoveBookFromLibraryClickAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnRemoveSearchQueryClickedAction
import nl.rhaydus.ui.common.formatDecimalNumber
import kotlin.time.Duration.Companion.seconds

internal const val TRENDING_SKELETON_COUNT = 4
internal const val CONTINUE_SERIES_SKELETON_COUNT = 4

// Distinct shared-element surfaces so the same book showing up in both rows
// registers two unique keys in the SharedTransitionScope.
internal const val SURFACE_TRENDING = "explore-trending"
internal const val SURFACE_UP_NEXT = "explore-up-next"

/**
 * A trending-book cell: cover (with optional unreleased badge), 2-line title, author, and rating.
 * Width is caller-controlled via [modifier] — the mobile carousel fixes it (`Modifier.width(…)`), the
 * desktop discovery grid fills the cell (`Modifier.fillMaxWidth()`). Hover/cursor affordances are
 * baked in and inert on touch, so the mobile rendering is unchanged.
 */
@Composable
internal fun TrendingCard(
    book: Book,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .prefetchBookDetailOnPress(book.id)
            .pointerHandCursor()
            .pressScaleClickable(onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            EditionImage(
                edition = book.currentEdition,
                defaultEdition = book.defaultEdition,
                isLoading = false,
                fallbackCoverUrl = book.coverUrl,
                modifier = Modifier.fillMaxWidth(),
                elevation = 6.dp,
                cornerRadius = 6.dp,
                sharedTransitionKey = bookCoverTransitionKey(
                    editionId = book.currentEdition?.id,
                    bookId = book.id,
                    surface = SURFACE_TRENDING,
                ),
            )

            if (book.isUnreleased) {
                book.effectiveReleaseDate?.let { date ->
                    UnreleasedBadge(
                        releaseDate = date,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(all = 6.dp),
                    )
                }
            }
        }

        Text(
            text = book.title,
            style = MaterialTheme.editorialTypography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            minLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        Text(
            text = book.authorString,
            style = MaterialTheme.editorialTypography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.height(16.dp),
        ) {
            if (book.rating != 0.0) {
                val starIcon = SoftcoverIconResource.Drawable(
                    icon = SoftcoverIcon.StarFilled,
                    contentDescription = "",
                )

                Icon(
                    painter = starIcon.getIconPainter(),
                    contentDescription = starIcon.contentDescription,
                    tint = RatingGold,
                    modifier = Modifier.size(14.dp),
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = formatDecimalNumber(
                        book.rating,
                        fractionDigits = 1,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
internal fun TrendingCardSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(6.dp))
                .shimmer(isLoading = true),
        )

        Box(
            modifier = Modifier
                .height(16.dp)
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(4.dp))
                .shimmer(isLoading = true),
        )

        Box(
            modifier = Modifier
                .height(12.dp)
                .fillMaxWidth(0.6f)
                .clip(RoundedCornerShape(4.dp))
                .shimmer(isLoading = true),
        )
    }
}

/**
 * An "up next in your series" cell: cover with a corner overflow menu (opens the dismiss sheet),
 * series eyebrow, 2-line title, and series position. Width is caller-controlled via [modifier], as
 * with [TrendingCard].
 */
@Composable
internal fun SeriesCard(
    book: Book,
    onClick: () -> Unit,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .prefetchBookDetailOnPress(book.id)
            .pointerHandCursor()
            .pressScaleClickable(onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            EditionImage(
                edition = book.currentEdition,
                defaultEdition = book.defaultEdition,
                isLoading = false,
                fallbackCoverUrl = book.coverUrl,
                modifier = Modifier.fillMaxWidth(),
                elevation = 4.dp,
                cornerRadius = 6.dp,
                sharedTransitionKey = bookCoverTransitionKey(
                    editionId = book.currentEdition?.id,
                    bookId = book.id,
                    surface = SURFACE_UP_NEXT,
                ),
            )

            IconButton(
                onClick = onMenuClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(2.dp)
                    .size(15.dp)
                    .pointerHandCursor()
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                        shape = CircleShape,
                    ),
            ) {
                val moreVertIcon = SoftcoverIconResource.Drawable(
                    icon = SoftcoverIcon.MoreVert,
                    contentDescription = "More options",
                )

                Icon(
                    painter = moreVertIcon.getIconPainter(),
                    contentDescription = moreVertIcon.contentDescription,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(14.dp),
                )
            }

            if (book.isUnreleased) {
                book.effectiveReleaseDate?.let { date ->
                    UnreleasedBadge(
                        releaseDate = date,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(all = 6.dp),
                    )
                }
            }
        }

        book.bookSeries?.let { series ->
            Text(
                text = series.name,
                style = MaterialTheme.editorialTypography.eyebrowSmall,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Text(
            text = book.title,
            style = MaterialTheme.editorialTypography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            minLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        Text(
            text = book.positionInSeriesDisplay?.let { "Book #$it" }.orEmpty(),
            style = MaterialTheme.editorialTypography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            minLines = 1,
            maxLines = 1,
        )
    }
}

@Composable
internal fun SeriesCardSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(6.dp))
                .shimmer(isLoading = true),
        )

        Box(
            modifier = Modifier
                .height(12.dp)
                .fillMaxWidth(0.7f)
                .clip(RoundedCornerShape(4.dp))
                .shimmer(isLoading = true),
        )

        Box(
            modifier = Modifier
                .height(14.dp)
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(4.dp))
                .shimmer(isLoading = true),
        )

        Box(
            modifier = Modifier
                .height(12.dp)
                .fillMaxWidth(0.4f)
                .clip(RoundedCornerShape(4.dp))
                .shimmer(isLoading = true),
        )
    }
}

/**
 * The shared dismiss-options sheet host for an "up next" series book. Wires the two dismiss actions
 * through [runAction] so both the mobile carousel and the desktop grid open an identical sheet, and
 * closes through [LocalModalSheetDismiss] so each form animates out the way it should. [onDismiss]
 * clears the host's selected book once the sheet has closed.
 */
@Composable
internal fun ContinueSeriesMenuSheet(
    book: Book,
    runAction: (ExploreAction) -> Unit,
    onDismiss: () -> Unit,
) {
    AdaptiveModalSheet(onDismissRequest = onDismiss) {
        val dismiss = LocalModalSheetDismiss.current

        ContinueSeriesDismissSheet(
            book = book,
            onDismissBookClick = {
                runAction(
                    OnDismissContinueSeriesBookAction(
                        bookId = book.id,
                        bookTitle = book.title,
                    ),
                )

                dismiss()
            },
            onDismissSeriesClick = {
                val series = book.bookSeries ?: return@ContinueSeriesDismissSheet

                runAction(
                    OnDismissContinueSeriesAction(
                        seriesId = series.id,
                        seriesName = series.name,
                    ),
                )

                dismiss()
            },
        )
    }
}

@Composable
private fun ContinueSeriesDismissSheet(
    book: Book,
    onDismissBookClick: () -> Unit,
    onDismissSeriesClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(state = rememberScrollState())
            .imePadding()
            .padding(bottom = 24.dp),
    ) {
        EditorialSectionHeader(
            eyebrow = book.bookSeries?.name ?: "Up next",
            headline = book.title,
            modifier = Modifier.padding(horizontal = 24.dp),
        )

        Spacer(modifier = Modifier.height(24.dp))

        DismissSheetOption(
            label = "I've already read this one",
            description = "Hide this book from \"Up next in your series\"",
            onClick = onDismissBookClick,
        )

        book.bookSeries?.let {
            DismissSheetOption(
                label = "Stop recommending this series",
                description = "Hide every book from this series",
                onClick = onDismissSeriesClick,
            )
        }
    }
}

@Composable
private fun DismissSheetOption(
    label: String,
    description: String,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .pointerHandCursor()
            .clickable(onClick = onClick)
            .padding(
                horizontal = 24.dp,
                vertical = 16.dp,
            ),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.editorialTypography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Text(
            text = description,
            style = MaterialTheme.editorialTypography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * The recent-search history block: an eyebrow + "Clear all" row over a horizontally scrolling row of
 * removable query chips. Shared by both platforms (full-width on desktop, beneath the discovery grid).
 */
@Composable
internal fun RecentSearchesSection(
    queries: List<String>,
    runAction: (ExploreAction) -> Unit,
) {
    if (queries.isEmpty()) return

    Column(
        modifier = Modifier.padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "RECENT SEARCHES",
                style = MaterialTheme.editorialTypography.eyebrowSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            SoftcoverButton(
                label = "Clear all",
                onClick = { runAction(OnRemoveAllSearchQueriesClickedAction()) },
                style = ButtonStyle.TEXT,
            )
        }

        QueryChipsRow(
            queries = queries,
            runAction = runAction,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun QueryChipsRow(
    queries: List<String>,
    runAction: (ExploreAction) -> Unit,
) {
    val animator = rememberLazyItemMutationAnimator(keys = queries)

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(queries, key = { it }) { query ->
            FilterChip(
                modifier = Modifier.mutationAnimated(
                    scope = this,
                    animator = animator,
                    itemKey = query,
                ),
                selected = false,
                onClick = {
                    runAction(
                        OnQueryChangeAction(
                            newQuery = query,
                            searchDelay = 0.seconds,
                        ),
                    )
                },
                label = { Text(text = query) },
                trailingIcon = {
                    val closeIcon = SoftcoverIconResource.Drawable(
                        icon = SoftcoverIcon.Close,
                        contentDescription = "Remove query icon",
                    )

                    Icon(
                        painter = closeIcon.getIconPainter(),
                        contentDescription = closeIcon.contentDescription,
                        modifier = Modifier
                            .size(16.dp)
                            .noRippleClickable {
                                runAction(OnRemoveSearchQueryClickedAction(query = query))
                            },
                    )
                },
            )
        }
    }
}

/**
 * A search-result row: cover, series eyebrow + title + byline + metadata, and the add/remove-from-
 * library toggle. Shared between the mobile single-column list and the desktop multi-column results
 * grid. Hover/cursor are baked in and inert on touch.
 */
@Composable
internal fun SearchResultRow(
    book: Book,
    onBookClick: (Book, String?) -> Unit,
    runAction: (ExploreAction) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .prefetchBookDetailOnPress(book.id)
            .pointerHandCursor()
            .pressScaleClickable(
                onClick = {
                    onBookClick(
                        book,
                        null,
                    )
                },
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EditionImage(
            edition = book.currentEdition,
            modifier = Modifier.width(80.dp),
            isLoading = false,
            defaultEdition = book.defaultEdition,
            fallbackCoverUrl = book.coverUrl,
            sharedTransitionKey = bookCoverTransitionKey(
                editionId = book.currentEdition?.id,
                bookId = book.id,
            ),
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            book.seriesText?.let { seriesText ->
                Text(
                    text = seriesText,
                    style = MaterialTheme.editorialTypography.eyebrowSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Text(
                text = book.title,
                style = MaterialTheme.editorialTypography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Text(
                text = "By ${book.authorString}",
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            val strings = listOf(
                book.releaseYear.takeIf { it != -1 },
                book.usersCount.let { "$it readers" },
                book.rating.takeIf { it != 0.0 },
            ).mapNotNull { it?.toString() }

            val label = strings.joinToString(separator = " • ") { it }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                if (book.rating != 0.0) {
                    Spacer(modifier = Modifier.width(4.dp))

                    val starIcon = SoftcoverIconResource.Drawable(
                        icon = SoftcoverIcon.StarFilled,
                        contentDescription = "",
                    )

                    Icon(
                        painter = starIcon.getIconPainter(),
                        contentDescription = starIcon.contentDescription,
                        tint = RatingGold,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(4.dp))

        val addedToLibrary = book.userBook != null

        IconToggleButton(
            checked = addedToLibrary,
            onCheckedChange = { newValue: Boolean ->
                when (newValue) {
                    true -> runAction(OnAddBookToLibraryClickAction(book = book))
                    false -> runAction(OnRemoveBookFromLibraryClickAction(book = book))
                }
            },
            modifier = Modifier.pointerHandCursor(),
        ) {
            val iconResource = when {
                addedToLibrary -> SoftcoverIcon.BookmarkAdded
                else -> SoftcoverIcon.BookmarkAdd
            }

            val contentDescription = when {
                addedToLibrary -> "Remove from library icon"
                else -> "Add to library icon"
            }

            val bookmarkIcon = SoftcoverIconResource.Drawable(
                icon = iconResource,
                contentDescription = contentDescription,
            )

            Icon(
                painter = bookmarkIcon.getIconPainter(),
                contentDescription = bookmarkIcon.contentDescription,
            )
        }
    }
}
