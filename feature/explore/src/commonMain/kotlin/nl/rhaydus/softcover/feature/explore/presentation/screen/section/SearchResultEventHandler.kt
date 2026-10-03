package nl.rhaydus.softcover.feature.explore.presentation.screen.section

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import nl.rhaydus.softcover.core.component.bookcard.BookCardEvent
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.presentation.prefetch.LocalBookDetailPrefetcher
import nl.rhaydus.softcover.feature.explore.presentation.action.ExploreAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnAddBookToLibraryClickAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnRemoveBookFromLibraryClickAction

@Composable
internal fun rememberSearchResultEventHandler(
    books: List<Book>,
    onBookClick: (Book, String?) -> Unit,
    runAction: (ExploreAction) -> Unit,
): (BookCardEvent) -> Unit {
    val prefetcher = LocalBookDetailPrefetcher.current
    val currentBooks by rememberUpdatedState(books)
    val currentOnBookClick by rememberUpdatedState(onBookClick)
    val currentRunAction by rememberUpdatedState(runAction)

    return remember(prefetcher) {
        { event ->
            val key = event.key

            if (key is BookCardKey.Book) {
                val book = currentBooks.firstOrNull { it.id == key.bookId }

                when (event) {
                    is BookCardEvent.Clicked -> book?.let {
                        currentOnBookClick(
                            it,
                            null,
                        )
                    }

                    is BookCardEvent.PressStarted -> prefetcher?.prefetch(key.bookId)

                    is BookCardEvent.TrailingClicked -> book?.let {
                        currentRunAction(
                            if (it.userBook != null) {
                                OnRemoveBookFromLibraryClickAction(book = it)
                            } else {
                                OnAddBookToLibraryClickAction(book = it)
                            },
                        )
                    }

                    is BookCardEvent.LongClicked,
                    is BookCardEvent.MenuClicked,
                    is BookCardEvent.ShakeEnded,
                        -> Unit
                }
            }
        }
    }
}
