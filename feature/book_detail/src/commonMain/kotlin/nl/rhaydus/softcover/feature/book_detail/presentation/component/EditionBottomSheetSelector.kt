package nl.rhaydus.softcover.feature.book_detail.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.rhaydus.common.toHoursMinutesSeconds
import nl.rhaydus.designsystem.editorial.component.EditorialSearchField
import nl.rhaydus.designsystem.modifier.conditional
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.designsystem.modifier.pressScaleClickable
import nl.rhaydus.designsystem.theme.StandardPreview
import nl.rhaydus.softcover.core.component.badge.Badge
import nl.rhaydus.softcover.core.component.badge.BadgeSize
import nl.rhaydus.softcover.core.component.badge.BadgeTone
import nl.rhaydus.softcover.core.component.badge.BadgeUiModel
import nl.rhaydus.softcover.core.component.cover.Cover
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.component.selection.SelectionMark
import nl.rhaydus.softcover.core.component.selection.SelectionMarkBackdrop
import nl.rhaydus.softcover.core.component.selection.SelectionMarkUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetScaffold
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldEvent
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.ReadingFormat
import nl.rhaydus.softcover.core.domain.preview.PreviewData
import nl.rhaydus.softcover.feature.book_detail.presentation.model.EditionTrailingUiModel

@Composable
internal fun EditionBottomSheetSelector(
    model: SheetScaffoldUiModel,
    editions: List<BookEdition>,
    editionCovers: Map<Int, CoverUiModel>,
    editionTrailingMarks: Map<Int, EditionTrailingUiModel>,
    selectedEditionId: Int?,
    isLoading: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onEditionSelected: (Int) -> Unit,
    onDismissRequest: () -> Unit,
    onConfirmClick: () -> Unit,
) {
    SheetScaffold(
        model = model,
        onEvent = { event ->
            when (event) {
                SheetScaffoldEvent.PrimaryClicked -> onConfirmClick()

                SheetScaffoldEvent.Dismissed -> onDismissRequest()

                SheetScaffoldEvent.TopActionClicked,
                SheetScaffoldEvent.SecondaryClicked,
                SheetScaffoldEvent.HeaderActionClicked,
                -> Unit
            }
        },
    ) {
        EditionBottomSheetContent(
            editions = editions,
            editionCovers = editionCovers,
            editionTrailingMarks = editionTrailingMarks,
            selectedEditionId = selectedEditionId,
            isLoading = isLoading,
            searchQuery = searchQuery,
            onSearchQueryChange = onSearchQueryChange,
            onEditionSelected = onEditionSelected,
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ColumnScope.EditionBottomSheetContent(
    editions: List<BookEdition>,
    editionCovers: Map<Int, CoverUiModel>,
    editionTrailingMarks: Map<Int, EditionTrailingUiModel>,
    selectedEditionId: Int?,
    isLoading: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onEditionSelected: (Int) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
    ) {
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
                    .weight(1f),
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
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(editions, key = { it.id }) { edition ->
                    EditionItem(
                        edition = edition,
                        selected = edition.id == selectedEditionId,
                        trailing = editionTrailingMarks[edition.id],
                        onEditionClick = { onEditionSelected(edition.id) },
                        cover = editionCovers[edition.id],
                    )
                }
            }
        }
    }
}

@Composable
private fun EditionItem(
    edition: BookEdition,
    cover: CoverUiModel?,
    selected: Boolean,
    trailing: EditionTrailingUiModel?,
    onEditionClick: () -> Unit,
) {
    val cardShape = RoundedCornerShape(12.dp)
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
            Cover(
                model = cover,
                modifier = Modifier.width(44.dp),
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

                    when (trailing) {
                        is EditionTrailingUiModel.Selected -> SelectionMark(model = trailing.mark)
                        is EditionTrailingUiModel.Tracking -> Badge(model = trailing.badge)
                        null -> Unit
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

    val editionCovers = editions.associate { edition ->
        edition.id to CoverUiModel(
            source = null,
            coverlessTitle = edition.title,
            variant = CoverVariant.EditionListRow,
        )
    }

    val editionTrailingMarks = mapOf(
        editions[1].id to EditionTrailingUiModel.Tracking(
            badge = BadgeUiModel(
                label = "TRACKING NOW",
                tone = BadgeTone.Neutral,
                size = BadgeSize.Regular,
            ),
        ),
        editions[2].id to EditionTrailingUiModel.Selected(
            mark = SelectionMarkUiModel(
                selected = true,
                backdrop = SelectionMarkBackdrop.Surface,
            ),
        ),
    )

    SoftcoverTheme {
        Column {
            EditionBottomSheetContent(
                editions = editions,
                editionCovers = editionCovers,
                editionTrailingMarks = editionTrailingMarks,
                selectedEditionId = editions[2].id,
                isLoading = false,
                searchQuery = "",
                onSearchQueryChange = {},
                onEditionSelected = {},
            )
        }
    }
}
