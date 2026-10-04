package nl.rhaydus.softcover.feature.explore.presentation.screen.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.component.rememberStaggeredEntryCoordinator
import nl.rhaydus.designsystem.component.staggeredEntry
import nl.rhaydus.softcover.core.component.bookcard.BookCard
import nl.rhaydus.softcover.core.component.bookcard.BookCardEvent
import nl.rhaydus.softcover.core.component.bookcard.BookCardSet
import nl.rhaydus.softcover.core.component.bookcard.lazyKey
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetRowGroupUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.presentation.prefetch.rememberPrefetchingBookCardEventHandler
import nl.rhaydus.softcover.feature.explore.presentation.action.ExploreAction
import nl.rhaydus.softcover.feature.explore.presentation.screen.SURFACE_BECAUSE_YOU_READ

private val becauseYouReadListState = LazyListState()

@Composable
internal fun BecauseYouReadSection(
    genre: String?,
    header: SectionHeaderUiModel,
    genreOptions: List<String>,
    genreSheet: SheetScaffoldUiModel,
    genreGroup: SheetRowGroupUiModel<String?>,
    set: BookCardSet<Book>,
    isLoading: Boolean,
    onBookClick: (Book, String?) -> Unit,
    runAction: (ExploreAction) -> Unit,
) {
    // Collapses only once the fetch is done and there is nothing to show; any loading state keeps the
    // header and rail footprint reserved.
    if (isLoading.not() && (genre == null || set.isEmpty)) return

    // Stamped on mount - see TrendingSection's comment for why.
    val entry = rememberStaggeredEntryCoordinator(key = "explore:because_you_read")

    val onCardEvent = rememberPrefetchingBookCardEventHandler(
        set = set,
    ) { event, book ->
        if (event is BookCardEvent.Clicked) {
            onBookClick(
                book,
                SURFACE_BECAUSE_YOU_READ,
            )
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionHeader(
            model = header,
            modifier = Modifier.padding(horizontal = 24.dp),
        )

        BecauseYouReadGenreControl(
            genre = genre ?: "Fantasy",
            options = genreOptions,
            sheet = genreSheet,
            genreGroup = genreGroup,
            runAction = runAction,
            isLoading = isLoading,
            modifier = Modifier.padding(horizontal = 24.dp),
        )

        LazyRow(
            state = becauseYouReadListState,
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            itemsIndexed(set.cards, key = { _, card -> card.key.lazyKey }) { index, card ->
                BookCard(
                    model = card,
                    onEvent = onCardEvent,
                    modifier = Modifier
                        .width(118.dp)
                        .let {
                            if (card.isLoading) {
                                it
                            } else {
                                it.staggeredEntry(
                                    coordinator = entry,
                                    index = index,
                                )
                            }
                        },
                )
            }
        }
    }
}
