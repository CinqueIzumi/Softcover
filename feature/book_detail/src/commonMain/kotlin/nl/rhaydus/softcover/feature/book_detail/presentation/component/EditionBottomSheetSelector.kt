package nl.rhaydus.softcover.feature.book_detail.presentation.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.designsystem.editorial.component.EditorialSearchField
import nl.rhaydus.designsystem.theme.StandardPreview
import nl.rhaydus.softcover.core.component.bookcard.BookCard
import nl.rhaydus.softcover.core.component.bookcard.BookCardEvent
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.bookcard.rememberBookCardEventHandler
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetScaffold
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldEvent
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.domain.model.ReadingFormat
import nl.rhaydus.softcover.core.domain.preview.PreviewData
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.toEditionCard

@Composable
internal fun EditionBottomSheetSelector(
    model: SheetScaffoldUiModel,
    editionCards: ImmutableList<BookCardUiModel>,
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

                SheetScaffoldEvent.SecondaryClicked,
                SheetScaffoldEvent.HeaderActionClicked -> Unit
            }
        },
    ) {
        val onCardEvent = rememberBookCardEventHandler(
            resolve = { key -> key as? BookCardKey.Edition },
        ) { event, edition ->
            if (event is BookCardEvent.Clicked) onEditionSelected(edition.editionId)
        }

        EditionBottomSheetContent(
            editionCards = editionCards,
            isLoading = isLoading,
            searchQuery = searchQuery,
            onSearchQueryChange = onSearchQueryChange,
            onEvent = onCardEvent,
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ColumnScope.EditionBottomSheetContent(
    editionCards: ImmutableList<BookCardUiModel>,
    isLoading: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onEvent: (BookCardEvent) -> Unit,
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

        SectionHeader(model = SectionHeaderUiModel.Label(eyebrow = "${editionCards.size} EDITIONS"))

        Spacer(modifier = Modifier.height(10.dp))

        if (isLoading && editionCards.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                CircularWavyProgressIndicator()
            }
        } else if (editionCards.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                SoftcoverText(
                    text = "No editions match your search",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.editorialTypography.body,
                )
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(editionCards, key = { it.key.toString() }) { card ->
                    BookCard(
                        model = card,
                        onEvent = onEvent,
                    )
                }
            }
        }
    }
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

    val editionCards = editions.mapIndexed { index, edition ->
        edition.toEditionCard(
            fallbackEdition = null,
            selected = index == 2,
            tracked = index == 1,
        )
    }.toImmutableList()

    SoftcoverTheme {
        Column {
            EditionBottomSheetContent(
                editionCards = editionCards,
                isLoading = false,
                searchQuery = "",
                onSearchQueryChange = {},
                onEvent = {},
            )
        }
    }
}
