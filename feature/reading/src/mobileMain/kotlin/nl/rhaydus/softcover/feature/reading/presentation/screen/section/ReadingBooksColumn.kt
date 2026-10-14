package nl.rhaydus.softcover.feature.reading.presentation.screen.section

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import nl.rhaydus.designsystem.component.rememberLazyItemMutationAnimator
import nl.rhaydus.softcover.core.component.bookcard.BookCard
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.bookcard.bookCardHandOver
import nl.rhaydus.softcover.core.component.bookcard.lazyKey
import nl.rhaydus.softcover.core.component.bookcard.rememberBookCardEntryCoordinator
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.reading.presentation.action.ReadingAction
import nl.rhaydus.softcover.feature.reading.presentation.screen.MarkAsReadController
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReadingBooksColumn(
    state: ReadingScreenUiState,
    runAction: (ReadingAction) -> Unit,
    onBookClick: (Book) -> Unit,
    controller: MarkAsReadController,
    listState: LazyListState,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    header: @Composable () -> Unit,
) {
    val animator = rememberLazyItemMutationAnimator(
        keys = remember(state.visibleAlsoReading) {
            state.visibleAlsoReading.cards.filterNot(BookCardUiModel::isLoading).map { it.key.lazyKey }
        },
    )

    val entry = rememberBookCardEntryCoordinator(
        cards = state.visibleAlsoReading.cards,
        key = "reading:rest",
    )

    val onCardEvent = rememberReadingCardEventHandler(
        alsoReading = state.visibleAlsoReading,
        runAction = runAction,
        onBookClick = onBookClick,
    )

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
    ) {
        item(key = "header") {
            header()
        }

        item(key = "featured") {
            ReadingFeaturedSection(
                state = state,
                runAction = runAction,
                onBookClick = onBookClick,
                controller = controller,
            )
        }

        if (state.visibleAlsoReading.isEmpty.not()) {
            item(key = "also-reading-label") {
                AlsoReadingHeader(model = state.visibleAlsoReadingHeader)
            }

            itemsIndexed(
                state.visibleAlsoReading.cards,
                key = { _, card -> card.key.lazyKey },
            ) { index, card ->
                val bookId = (card.key as? BookCardKey.Book)?.bookId ?: return@itemsIndexed

                BookCard(
                    model = card,
                    onEvent = onCardEvent,
                    modifier = Modifier.bookCardHandOver(
                        scope = this,
                        card = card,
                        animator = animator,
                        coordinator = entry,
                        index = index,
                    )
                        .then(controller.slideModifier(bookId)),
                )
            }
        }

        if (state.onHold.isEmpty.not()) {
            item(key = "on-hold") {
                OnHoldSection(
                    header = state.onHoldHeader,
                    onHold = state.onHold,
                    runAction = runAction,
                    onBookClick = onBookClick,
                )
            }
        }
    }
}
