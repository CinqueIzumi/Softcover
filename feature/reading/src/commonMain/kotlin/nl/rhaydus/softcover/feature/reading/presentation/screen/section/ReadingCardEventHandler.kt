package nl.rhaydus.softcover.feature.reading.presentation.screen.section

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import nl.rhaydus.softcover.core.component.bookcard.BookCardEvent
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.presentation.prefetch.LocalBookDetailPrefetcher

/**
 * One handler for a whole list; its cards route their events here by key. A press prefetches the book's
 * detail; [onTrailingClick] and [onShakeEnd] are for the rows that carry a trailing control and a failed
 * save.
 */
@Composable
internal fun rememberReadingCardEventHandler(
    books: List<Book>,
    onBookClick: (Book) -> Unit,
    onTrailingClick: (Book) -> Unit = {},
    onShakeEnd: (bookId: Int) -> Unit = {},
): (BookCardEvent) -> Unit {
    val prefetcher = LocalBookDetailPrefetcher.current
    val currentBooks by rememberUpdatedState(books)
    val currentOnBookClick by rememberUpdatedState(onBookClick)
    val currentOnTrailingClick by rememberUpdatedState(onTrailingClick)
    val currentOnShakeEnd by rememberUpdatedState(onShakeEnd)

    return remember(prefetcher) {
        { event ->
            val key = event.key

            if (key is BookCardKey.Book) {
                when (event) {
                    is BookCardEvent.Clicked -> currentBooks
                        .firstOrNull { it.id == key.bookId }
                        ?.let { currentOnBookClick(it) }

                    is BookCardEvent.TrailingClicked -> currentBooks
                        .firstOrNull { it.id == key.bookId }
                        ?.let { currentOnTrailingClick(it) }

                    is BookCardEvent.PressStarted -> prefetcher?.prefetch(key.bookId)

                    is BookCardEvent.ShakeEnded -> currentOnShakeEnd(key.bookId)

                    is BookCardEvent.LongClicked,
                    is BookCardEvent.MenuClicked,
                        -> Unit
                }
            }
        }
    }
}
