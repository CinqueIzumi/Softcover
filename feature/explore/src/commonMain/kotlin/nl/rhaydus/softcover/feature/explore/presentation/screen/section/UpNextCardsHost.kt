package nl.rhaydus.softcover.feature.explore.presentation.screen.section

import androidx.compose.runtime.Composable
import nl.rhaydus.softcover.core.component.bookcard.BookCardEvent
import nl.rhaydus.softcover.core.component.bookcard.BookCardSet
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.presentation.prefetch.rememberPrefetchingBookCardEventHandler
import nl.rhaydus.softcover.feature.explore.presentation.action.ExploreAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnContinueSeriesMenuOpenAction
import nl.rhaydus.softcover.feature.explore.presentation.screen.SURFACE_UP_NEXT

/**
 * Hosts the "Up next in your series" cards of the mobile carousel and the desktop grid: [content]
 * lays the cards out and routes their events to the handler it receives. A tap opens the book through
 * [onBookClick]; the overflow button opens the book's menu sheet, hosted by the screen overlays.
 */
@Composable
internal fun UpNextCardsHost(
    set: BookCardSet<Book>,
    onBookClick: (Book, String?) -> Unit,
    runAction: (ExploreAction) -> Unit,
    content: @Composable (onCardEvent: (BookCardEvent) -> Unit) -> Unit,
) {
    val onCardEvent = rememberPrefetchingBookCardEventHandler(
        set = set,
    ) { event, book ->
        when (event) {
            is BookCardEvent.Clicked -> onBookClick(
                book,
                SURFACE_UP_NEXT,
            )

            is BookCardEvent.MenuClicked -> {
                runAction(OnContinueSeriesMenuOpenAction(bookId = book.id))
            }

            is BookCardEvent.LongClicked,
            is BookCardEvent.PressStarted,
            is BookCardEvent.TrailingClicked,
            is BookCardEvent.ShakeEnded,
                -> Unit
        }
    }

    content(onCardEvent)
}
