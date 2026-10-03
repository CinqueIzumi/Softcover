package nl.rhaydus.softcover.feature.explore.presentation.screen.section

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import nl.rhaydus.softcover.core.component.bookcard.BookCardEvent
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.presentation.prefetch.LocalBookDetailPrefetcher

/**
 * One handler for a whole rail; its cards route their events here by key. A click reports [surface] to
 * [onBookClick], a press prefetches the book's detail, and the overflow button reports to [onMenuClick].
 */
@Composable
internal fun rememberRailCardEventHandler(
    books: List<Book>,
    surface: String,
    onBookClick: (Book, String?) -> Unit,
    onMenuClick: (Book) -> Unit = {},
): (BookCardEvent) -> Unit {
    val prefetcher = LocalBookDetailPrefetcher.current
    val currentBooks by rememberUpdatedState(books)
    val currentOnBookClick by rememberUpdatedState(onBookClick)
    val currentOnMenuClick by rememberUpdatedState(onMenuClick)

    return remember(prefetcher, surface) {
        { event ->
            val key = event.key

            if (key is BookCardKey.Book) {
                when (event) {
                    is BookCardEvent.Clicked -> currentBooks
                        .firstOrNull { it.id == key.bookId }
                        ?.let {
                            currentOnBookClick(
                                it,
                                surface,
                            )
                        }

                    is BookCardEvent.MenuClicked -> currentBooks
                        .firstOrNull { it.id == key.bookId }
                        ?.let { currentOnMenuClick(it) }

                    is BookCardEvent.PressStarted -> prefetcher?.prefetch(key.bookId)

                    is BookCardEvent.LongClicked,
                    is BookCardEvent.TrailingClicked,
                    is BookCardEvent.ShakeEnded,
                        -> Unit
                }
            }
        }
    }
}
