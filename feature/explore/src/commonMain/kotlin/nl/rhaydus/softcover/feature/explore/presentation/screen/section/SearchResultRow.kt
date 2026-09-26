package nl.rhaydus.softcover.feature.explore.presentation.screen.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import nl.rhaydus.common.formatDecimalNumber
import nl.rhaydus.common.formatGroupedNumber
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.designsystem.modifier.pressScaleClickable
import nl.rhaydus.softcover.core.component.cover.Cover
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.RatingGold
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.presentation.prefetch.prefetchBookDetailOnPress
import nl.rhaydus.softcover.feature.explore.presentation.action.ExploreAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnAddBookToLibraryClickAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnRemoveBookFromLibraryClickAction

/**
 * A search-result row: cover, category eyebrow + title + byline + metadata, and the add/remove-from-
 * library bookmark toggle. Shared between the mobile single-column list and the desktop multi-column
 * results grid. Hover/cursor are baked in and inert on touch.
 */
@Composable
internal fun SearchResultRow(
    book: Book,
    cover: CoverUiModel?,
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
            )
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Cover(
           model = cover,
            modifier = Modifier.width(54.dp),
        )

        Column(modifier = Modifier.weight(1f)) {
            // Series only — a book with no series shows no eyebrow here (explore-3a feedback: a
            // fallback tag like "classics"/"dark" in the series slot read as noise, not signal).
            val category = book.seriesText

            if (category != null) {
                Text(
                    text = category.uppercase(),
                    style = MaterialTheme.editorialTypography.eyebrowSmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(modifier = Modifier.height(4.dp))
            }

            Text(
                text = book.title,
                style = MaterialTheme.editorialTypography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "By ${book.authorString}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                val metaParts = listOfNotNull(
                    book.releaseYear.takeIf { it != -1 }?.toString(),
                    "${formatGroupedNumber(book.usersCount)} readers",
                )

                Text(
                    text = metaParts.joinToString(separator = " · "),
                    style = MaterialTheme.editorialTypography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                if (book.rating != 0.0) {
                    Text(
                        text = " · ",
                        style = MaterialTheme.editorialTypography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    val starIcon = drawableIconResource(
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
                        style = MaterialTheme.editorialTypography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        val addedToLibrary = book.userBook != null

        BookmarkToggle(
            checked = addedToLibrary,
            onCheckedChange = { newValue ->
                when (newValue) {
                    true -> runAction(OnAddBookToLibraryClickAction(book = book))
                    false -> runAction(OnRemoveBookFromLibraryClickAction(book = book))
                }
            },
        )
    }
}

/** The 44×44, radius-12 bookmark-to-library toggle (explore-3a §4 "Search results"). */
@Composable
private fun BookmarkToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Surface(
        onClick = { onCheckedChange(checked.not()) },
        modifier = Modifier
            .size(44.dp)
            .pointerHandCursor(),
        shape = RoundedCornerShape(12.dp),
        color = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = if (checked) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            val iconResource = when {
                checked -> SoftcoverIcon.BookmarkAdded
                else -> SoftcoverIcon.BookmarkAdd
            }

            val contentDescription = when {
                checked -> "Remove from library"
                else -> "Add to library"
            }

            val bookmarkIcon = drawableIconResource(
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
