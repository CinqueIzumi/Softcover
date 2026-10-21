package nl.rhaydus.softcover.feature.library.presentation.screen.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.component.rememberLazyItemMutationAnimator
import nl.rhaydus.softcover.core.component.bookcard.BookCard
import nl.rhaydus.softcover.core.component.bookcard.BookCardEvent
import nl.rhaydus.softcover.core.component.bookcard.BookCardSet
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.bookcard.bookCardHandOver
import nl.rhaydus.softcover.core.component.bookcard.lazyKey
import nl.rhaydus.softcover.core.component.bookcard.rememberBookCardEntryCoordinator
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.state.SkeletonRevealGroup
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.presentation.prefetch.rememberPrefetchingBookCardEventHandler
import nl.rhaydus.softcover.feature.library.presentation.action.LibraryAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnAddRecommendedToWantToReadAction

/** @param horizontalInset The row's content padding, so cards scroll through it rather than clip at it. */
@Composable
internal fun RecommendedRail(
    header: SectionHeaderUiModel,
    set: BookCardSet<Book>,
    horizontalInset: Dp,
    onBookClick: (Book) -> Unit,
    runAction: (LibraryAction) -> Unit,
) {
    val onCardEvent = rememberPrefetchingBookCardEventHandler(set = set) { event, book ->
        when (event) {
            is BookCardEvent.Clicked -> onBookClick(book)

            is BookCardEvent.TrailingClicked -> runAction(OnAddRecommendedToWantToReadAction(book = book))

            else -> Unit
        }
    }

    val entry = rememberBookCardEntryCoordinator(
        cards = set.cards,
        key = "library:recommended",
    )

    val animator = rememberLazyItemMutationAnimator(
        keys = remember(set.cards) {
            set.cards.filterNot(BookCardUiModel::isLoading).map { it.key.lazyKey }
        },
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
        modifier = Modifier.padding(top = Spacing.xl),
    ) {
        SectionHeader(
            model = header,
            modifier = Modifier.padding(horizontal = horizontalInset),
        )

        SkeletonRevealGroup {
            LazyRow(
                contentPadding = PaddingValues(horizontal = horizontalInset),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                itemsIndexed(
                    set.cards,
                    key = { _, card -> card.key.lazyKey },
                ) { index, card ->
                    BookCard(
                        model = card,
                        onEvent = onCardEvent,
                        modifier = Modifier
                            .width(118.dp)
                            .bookCardHandOver(
                                scope = this,
                                card = card,
                                animator = animator,
                                coordinator = entry,
                                index = index,
                            ),
                    )
                }
            }
        }
    }
}
