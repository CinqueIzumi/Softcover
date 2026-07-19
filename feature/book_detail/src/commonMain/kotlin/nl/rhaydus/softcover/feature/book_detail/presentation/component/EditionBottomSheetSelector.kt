package nl.rhaydus.softcover.feature.book_detail.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.rhaydus.common.toHoursMinutesSeconds
import nl.rhaydus.designsystem.component.AdaptiveModalSheet
import nl.rhaydus.designsystem.component.RhaydusButton
import nl.rhaydus.designsystem.editorial.component.EditorialSearchField
import nl.rhaydus.designsystem.model.ButtonSize
import nl.rhaydus.designsystem.model.ButtonStyle
import nl.rhaydus.designsystem.modifier.conditional
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.designsystem.modifier.pressScaleClickable
import nl.rhaydus.designsystem.theme.StandardPreview
import nl.rhaydus.softcover.core.designsystem.presentation.component.EditionImage
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.preview.PreviewData
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.ReadingFormat

@Composable
internal fun EditionBottomSheetSelector(
    bookTitle: String,
    currentEdition: BookEdition,
    defaultEdition: BookEdition?,
    editions: List<BookEdition>,
    isLoading: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onDismissRequest: () -> Unit,
    onConfirmClick: (BookEdition) -> Unit,
) {
    AdaptiveModalSheet(onDismissRequest = onDismissRequest) {
        EditionBottomSheetContent(
            bookTitle = bookTitle,
            currentEdition = currentEdition,
            defaultEdition = defaultEdition,
            editions = editions,
            isLoading = isLoading,
            searchQuery = searchQuery,
            onSearchQueryChange = onSearchQueryChange,
            onConfirmClick = onConfirmClick,
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun EditionBottomSheetContent(
    bookTitle: String,
    currentEdition: BookEdition,
    defaultEdition: BookEdition?,
    editions: List<BookEdition>,
    isLoading: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onConfirmClick: (BookEdition) -> Unit,
) {
    var selectedEdition by remember {
        mutableStateOf(currentEdition)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .imePadding(),
    ) {
        ChangeEditionHeader(
            bookTitle = bookTitle,
            currentEdition = currentEdition,
            defaultEdition = defaultEdition,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
        )

        Spacer(modifier = Modifier.height(20.dp))

        EditorialSearchField(
            query = searchQuery,
            onQueryChange = onSearchQueryChange,
            onClearClick = { onSearchQueryChange("") },
            searchIcon = drawableIconResource(
                icon = SoftcoverIcon.Search,
                contentDescription = "Search",
            ),
            clearIcon = drawableIconResource(
                icon = SoftcoverIcon.Close,
                contentDescription = "Clear search",
            ),
            placeholder = "Search by ISBN or publisher",
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "${editions.size} EDITIONS",
            style = MaterialTheme.editorialTypography.eyebrowSmall.copy(letterSpacing = 1.6.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 24.dp),
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (isLoading && editions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                CircularWavyProgressIndicator()
            }
        } else if (editions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "No editions match your search",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.editorialTypography.body,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(editions, key = { it.id }) { edition ->
                    EditionItem(
                        edition = edition,
                        currentEdition = currentEdition,
                        selected = edition.id == selectedEdition.id,
                        onEditionClick = { selectedEdition = edition },
                        defaultEdition = defaultEdition ?: edition,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        RhaydusButton(
            label = "Track this edition",
            style = ButtonStyle.FILLED,
            size = ButtonSize.M,
            enabled = isLoading.not() && selectedEdition != currentEdition,
            onClick = { onConfirmClick(selectedEdition) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * The canonical opening — accent bar, eyebrow, italic headline, italic description — composed locally
 * rather than through `EditorialSectionHeader`, which has no trailing slot for the book's mini jacket.
 * Mirrors the Choose-lists sheet's / Tag-editor sheet's header anatomy.
 */
@Composable
private fun ChangeEditionHeader(
    bookTitle: String,
    currentEdition: BookEdition,
    defaultEdition: BookEdition?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(width = 32.dp, height = 4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.primary),
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "EDITIONS",
                    style = MaterialTheme.editorialTypography.eyebrow,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Change edition",
                style = MaterialTheme.editorialTypography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = editionsDescription(
                    bookTitle = bookTitle,
                    author = currentEdition.authorString,
                ),
                style = MaterialTheme.editorialTypography.body,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        EditionImage(
            edition = currentEdition,
            defaultEdition = defaultEdition,
            isLoading = false,
            coverlessTitle = bookTitle,
            cornerRadius = 4.dp,
            elevation = 4.dp,
            shadowColor = Color.Black.copy(alpha = 0.5f),
            modifier = Modifier.width(56.dp),
        )
    }
}

private fun editionsDescription(
    bookTitle: String,
    author: String,
): String = if (author.isBlank()) bookTitle else "$bookTitle · $author"

@Composable
private fun EditionItem(
    edition: BookEdition,
    currentEdition: BookEdition,
    defaultEdition: BookEdition,
    selected: Boolean,
    onEditionClick: () -> Unit,
) {
    val cardShape = RoundedCornerShape(12.dp)
    val isTracked = edition.id == currentEdition.id
    val emphasized = selected || edition.owned

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = cardShape)
            .conditional(
                condition = emphasized,
                ifTrue = {
                    Modifier.border(
                        width = 1.5.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = cardShape,
                    )
                },
            )
            .background(
                color = if (selected) {
                    MaterialTheme.colorScheme.surfaceContainerLow
                } else {
                    MaterialTheme.colorScheme.surfaceContainer
                },
            )
            .pointerHandCursor()
            .pressScaleClickable(onClick = onEditionClick)
            .padding(all = 13.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
        ) {
            EditionImage(
                edition = edition,
                modifier = Modifier.width(44.dp),
                isLoading = false,
                defaultEdition = defaultEdition,
                coverlessTitle = edition.title,
            )

            Spacer(modifier = Modifier.width(13.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val formatIcon = drawableIconResource(
                        icon = edition.readingFormat.formatGlyph(),
                        contentDescription = "",
                    )

                    Icon(
                        painter = formatIcon.getIconPainter(),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(17.dp),
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = edition.formatLabel(),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    when {
                        selected -> SelectedCheckBadge()
                        isTracked -> TrackingNowChip()
                    }
                }

                val meta = edition.metaLine()

                if (meta.isNotBlank()) {
                    Spacer(modifier = Modifier.height(5.dp))

                    Text(
                        text = meta,
                        style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                edition.isbnWhisper()?.let { isbn ->
                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = isbn,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    )
                }

                if (edition.owned) {
                    Spacer(modifier = Modifier.height(9.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val bagIcon = drawableIconResource(
                            icon = SoftcoverIcon.ShoppingBag,
                            contentDescription = "",
                        )

                        Icon(
                            painter = bagIcon.getIconPainter(),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(15.dp),
                        )

                        Spacer(modifier = Modifier.width(5.dp))

                        Text(
                            text = "You own this edition",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectedCheckBadge() {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center,
    ) {
        val icon = drawableIconResource(
            icon = SoftcoverIcon.Check,
            contentDescription = "Selected edition",
        )

        Icon(
            painter = icon.getIconPainter(),
            contentDescription = icon.contentDescription,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(15.dp),
        )
    }
}

@Composable
private fun TrackingNowChip() {
    Text(
        text = "TRACKING NOW",
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.6.sp,
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(horizontal = 9.dp, vertical = 4.dp),
    )
}

/** Format icon: the differentiator that leads each card — `readingFormat` maps to a single glyph. */
private fun ReadingFormat?.formatGlyph(): SoftcoverIcon = when (this) {
    ReadingFormat.Audio -> SoftcoverIcon.Headset
    ReadingFormat.Ebook -> SoftcoverIcon.LibraryBooks
    ReadingFormat.Physical, ReadingFormat.Both, null -> SoftcoverIcon.MenuBook
}

private fun BookEdition.formatLabel(): String =
    format.takeIf { it.isNotBlank() } ?: readingFormat?.label ?: "Edition"

/**
 * The tabular differences line: publisher · release year · pages, with pages swapped for a duration
 * ("{h}h {m}m", via the foundation `toHoursMinutesSeconds()`) on an audiobook. Unknown parts are
 * dropped rather than shown as a placeholder.
 */
private fun BookEdition.metaLine(): String {
    val parts = buildList {
        publisher?.takeIf { it.isNotBlank() }?.let(::add)
        releaseYear.takeIf { it != -1 }?.let { add(it.toString()) }

        if (isAudiobook) {
            audioSeconds?.let { seconds ->
                val duration = seconds.toHoursMinutesSeconds()
                add("${duration.hours}h ${duration.minutes}m")
            }
        } else {
            pages?.let { add("$it pp") }
        }
    }

    return parts.joinToString(" · ")
}

private fun BookEdition.isbnWhisper(): String? {
    val isbn = isbn13?.takeIf { it.isNotBlank() } ?: isbn10?.takeIf { it.isNotBlank() }

    return isbn?.let { "ISBN $it" }
}

@StandardPreview
@Composable
private fun EditionBottomSheetContentPreview() {
    val baseEdition = PreviewData.baseEdition.copy(title = "King Sorrow")

    val editions = listOf(
        baseEdition.copy(
            id = 10,
            format = "Audiobook",
            readingFormat = ReadingFormat.Audio,
            publisher = "Harper Audio",
            releaseYear = 2025,
            pages = null,
            audioSeconds = 87120,
            isbn13 = "9780063339214",
            isbn10 = null,
            owned = true,
        ),
        baseEdition.copy(
            id = 20,
            format = "Hardcover",
            readingFormat = ReadingFormat.Physical,
            publisher = "William Morrow",
            releaseYear = 2025,
            pages = 896,
            audioSeconds = null,
            isbn13 = "9780062200600",
            isbn10 = null,
            owned = false,
        ),
        baseEdition.copy(
            id = 30,
            format = "Trade paperback",
            readingFormat = ReadingFormat.Physical,
            publisher = "William Morrow",
            releaseYear = 2026,
            pages = 912,
            audioSeconds = null,
            isbn13 = "9780063345123",
            isbn10 = null,
            owned = false,
        ),
        baseEdition.copy(
            id = 40,
            format = "E-book",
            readingFormat = ReadingFormat.Ebook,
            publisher = "William Morrow",
            releaseYear = 2025,
            pages = 900,
            audioSeconds = null,
            isbn13 = "9780062200624",
            isbn10 = null,
            owned = false,
        ),
        baseEdition.copy(
            id = 50,
            format = "Hardcover · UK",
            readingFormat = ReadingFormat.Physical,
            publisher = "Gollancz",
            releaseYear = 2025,
            pages = 880,
            audioSeconds = null,
            isbn13 = "9781473230129",
            isbn10 = null,
            owned = false,
        ),
    )

    SoftcoverTheme {
        EditionBottomSheetContent(
            bookTitle = "King Sorrow",
            currentEdition = editions[1],
            defaultEdition = editions[1],
            editions = editions,
            isLoading = false,
            searchQuery = "",
            onSearchQueryChange = {},
            onConfirmClick = {},
        )
    }
}
