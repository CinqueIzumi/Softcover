package nl.rhaydus.softcover.feature.explore.presentation.screen.section

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.collections.immutable.ImmutableMap
import nl.rhaydus.softcover.core.component.bookcard.BookCardEvent
import nl.rhaydus.softcover.core.component.bookcard.BookCardSet
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.presentation.prefetch.rememberPrefetchingBookCardEventHandler
import nl.rhaydus.softcover.feature.explore.presentation.action.ExploreAction
import nl.rhaydus.softcover.feature.explore.presentation.model.ContinueSeriesMenuSheetUiModel
import nl.rhaydus.softcover.feature.explore.presentation.screen.SURFACE_UP_NEXT

/**
 * Hosts the "Up next in your series" cards of the mobile carousel and the desktop grid: [content]
 * lays the cards out and routes their events to the handler it receives. A tap opens the book through
 * [onBookClick]; the overflow button opens the book's entry of [menuSheets], which runs the picked
 * option through [runAction].
 */
@Composable
internal fun UpNextCardsHost(
    set: BookCardSet<Book>,
    menuSheets: ImmutableMap<Int, ContinueSeriesMenuSheetUiModel>,
    onBookClick: (Book, String?) -> Unit,
    runAction: (ExploreAction) -> Unit,
    content: @Composable (onCardEvent: (BookCardEvent) -> Unit) -> Unit,
) {
    var sheetBook by remember { mutableStateOf<Book?>(null) }

    val onCardEvent = rememberPrefetchingBookCardEventHandler(
        set = set,
    ) { event, book ->
        when (event) {
            is BookCardEvent.Clicked -> onBookClick(
                book,
                SURFACE_UP_NEXT,
            )

            is BookCardEvent.MenuClicked -> sheetBook = book

            is BookCardEvent.LongClicked,
            is BookCardEvent.PressStarted,
            is BookCardEvent.TrailingClicked,
            is BookCardEvent.ShakeEnded,
                -> Unit
        }
    }

    content(onCardEvent)

    sheetBook?.let { book ->
        menuSheets[book.id]?.let { menuSheet ->
            ContinueSeriesMenuSheet(
                model = menuSheet,
                runAction = runAction,
                onDismiss = { sheetBook = null },
            )
        }
    }
}
