package nl.rhaydus.softcover.feature.book_detail.presentation.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import nl.rhaydus.designsystem.editorial.component.EditorialSearchField
import nl.rhaydus.designsystem.theme.StandardPreview
import nl.rhaydus.softcover.core.component.bookcard.BookCard
import nl.rhaydus.softcover.core.component.bookcard.BookCardEvent
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.core.component.bookcard.lazyKey
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.component.sheet.LazySheetScaffold
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldEvent
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.feature.book_detail.presentation.state.EditionSheetEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.state.EditionSheetUiModel

@Composable
internal fun EditionBottomSheetSelector(
    model: EditionSheetUiModel,
    onEvent: (EditionSheetEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazySheetScaffold(
        model = model.scaffold,
        onEvent = { event ->
            when (event) {
                SheetScaffoldEvent.PrimaryClicked -> onEvent(EditionSheetEvent.Confirmed)

                SheetScaffoldEvent.Dismissed -> onEvent(EditionSheetEvent.Dismissed)

                SheetScaffoldEvent.SecondaryClicked,
                SheetScaffoldEvent.HeaderActionClicked -> Unit
            }
        },
        modifier = modifier,
        fillHeight = true,
        pinned = {
            EditionSearchHeader(
                model = model,
                onEvent = onEvent,
            )
        },
    ) {
        editionItems(
            model = model,
            onEvent = onEvent,
        )
    }
}

@Composable
private fun EditionSearchHeader(
    model: EditionSheetUiModel,
    onEvent: (EditionSheetEvent) -> Unit,
) {
    EditorialSearchField(
        query = model.searchQuery,
        onQueryChange = { onEvent(EditionSheetEvent.SearchChanged(query = it)) },
        onClearClick = { onEvent(EditionSheetEvent.SearchChanged(query = "")) },
        searchIcon = drawableIconResource(
            icon = SoftcoverIcon.Search,
            contentDescription = "Search",
        ),
        clearIcon = drawableIconResource(
            icon = SoftcoverIcon.Close,
            contentDescription = "Clear search",
        ),
        placeholder = model.searchPlaceholder,
    )

    Spacer(modifier = Modifier.height(Spacing.md))

    SectionHeader(model = model.countHeader)

    Spacer(modifier = Modifier.height(Spacing.sm))
}

private fun LazyListScope.editionItems(
    model: EditionSheetUiModel,
    onEvent: (EditionSheetEvent) -> Unit,
) {
    if (model.cards.isEmpty()) {
        item {
            Box(
                modifier = Modifier.fillParentMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                SoftcoverText(
                    text = model.emptyMessage,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.editorialTypography.body,
                )
            }
        }
    } else {
        items(
            count = model.cards.size,
            key = { model.cards[it].key.lazyKey },
        ) { index ->
            BookCard(
                model = model.cards[index],
                onEvent = { event ->
                    val key = event.key

                    if (event is BookCardEvent.Clicked && key is BookCardKey.Edition) {
                        onEvent(EditionSheetEvent.EditionSelected(editionId = key.editionId))
                    }
                },
            )
        }
    }
}

@StandardPreview
@Composable
private fun EditionBottomSheetContentPreview() {
    val model = editionSheetPreviewModel()

    SoftcoverTheme {
        Column {
            EditionSearchHeader(
                model = model,
                onEvent = {},
            )

            LazyColumn {
                editionItems(
                    model = model,
                    onEvent = {},
                )
            }
        }
    }
}
