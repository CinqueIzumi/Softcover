package nl.rhaydus.softcover.feature.explore.presentation.screen.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableMap
import nl.rhaydus.designsystem.component.rememberStaggeredEntryCoordinator
import nl.rhaydus.designsystem.component.staggeredEntry
import nl.rhaydus.designsystem.util.SkeletonCrossfade
import nl.rhaydus.softcover.core.component.bookcard.BookCard
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetRowGroupUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.explore.presentation.action.ExploreAction
import nl.rhaydus.softcover.feature.explore.presentation.screen.SURFACE_BECAUSE_YOU_READ

private val becauseYouReadListState = LazyListState()

private val BECAUSE_YOU_READ_CARD_WIDTH = 118.dp

@Composable
internal fun BecauseYouReadSection(
    genre: String?,
    header: SectionHeaderUiModel?,
    genreOptions: List<String>,
    genreSheet: SheetScaffoldUiModel,
    genreGroup: SheetRowGroupUiModel<String?>,
    books: List<Book>,
    cards: ImmutableMap<Int, BookCardUiModel>,
    isLoading: Boolean,
    onBookClick: (Book, String?) -> Unit,
    runAction: (ExploreAction) -> Unit,
) {
    // Collapses only once the fetch is done and there is nothing to show; any loading state keeps the
    // header and rail footprint reserved.
    if (isLoading.not() && (genre == null || books.isEmpty())) return

    // Hoisted above the crossfade - see TrendingSection's comment for why.
    val entry = rememberStaggeredEntryCoordinator(key = "explore:because_you_read")

    val onCardEvent = rememberRailCardEventHandler(
        books = books,
        surface = SURFACE_BECAUSE_YOU_READ,
        onBookClick = onBookClick,
    )

    // The cards are derived off the books, so they land a frame after them.
    val cardsReady = books.all { it.id in cards }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionHeader(
            model = header ?: BECAUSE_YOU_READ_HEADER_STAND_IN,
            isLoading = isLoading,
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

        SkeletonCrossfade(
            isLoading = isLoading || cardsReady.not(),
            label = "BecauseYouReadRail",
        ) { loading ->
            if (loading) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(3) {
                        BookCard(
                            model = BECAUSE_YOU_READ_STAND_IN_CARD,
                            onEvent = {},
                            isLoading = true,
                            modifier = Modifier.width(BECAUSE_YOU_READ_CARD_WIDTH),
                        )
                    }
                }
            } else {
                LazyRow(
                    state = becauseYouReadListState,
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    itemsIndexed(books, key = { _, book -> book.id }) { index, book ->
                        cards[book.id]?.let { card ->
                            BookCard(
                                model = card,
                                onEvent = onCardEvent,
                                modifier = Modifier
                                    .width(BECAUSE_YOU_READ_CARD_WIDTH)
                                    .staggeredEntry(coordinator = entry, index = index),
                            )
                        }
                    }
                }
            }
        }
    }
}
