package nl.rhaydus.softcover.core.presentation.prefetch

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import nl.rhaydus.softcover.core.component.bookcard.BookCardEvent
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.core.component.bookcard.BookCardSet
import nl.rhaydus.softcover.core.component.bookcard.rememberBookCardEventHandler

/**
 * [rememberBookCardEventHandler] that also prefetches a book's detail when a press starts on a
 * [BookCardKey.Book] card, whether or not the key resolves. [prefetchEnabled] lets a surface suppress
 * it, for instance while selecting.
 */
@Composable
fun <T : Any> rememberPrefetchingBookCardEventHandler(
    set: BookCardSet<T>,
    prefetchEnabled: Boolean = true,
    onEvent: (event: BookCardEvent, payload: T) -> Unit,
): (BookCardEvent) -> Unit {
    val prefetcher = LocalBookDetailPrefetcher.current
    val currentPrefetchEnabled by rememberUpdatedState(prefetchEnabled)
    val resolving = rememberBookCardEventHandler(
        set = set,
        onEvent = onEvent,
    )

    return remember(prefetcher, resolving) {
        { event ->
            val key = event.key

            if (event is BookCardEvent.PressStarted && key is BookCardKey.Book && currentPrefetchEnabled) {
                prefetcher?.prefetch(key.bookId)
            }

            resolving(event)
        }
    }
}
