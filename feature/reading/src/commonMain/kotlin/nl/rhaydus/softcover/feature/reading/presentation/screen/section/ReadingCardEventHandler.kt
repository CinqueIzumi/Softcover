package nl.rhaydus.softcover.feature.reading.presentation.screen.section

import androidx.compose.runtime.Composable
import nl.rhaydus.softcover.core.component.bookcard.BookCardEvent
import nl.rhaydus.softcover.core.component.bookcard.BookCardSet
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.presentation.prefetch.rememberPrefetchingBookCardEventHandler
import nl.rhaydus.softcover.feature.reading.presentation.action.OnClearMutationFailureAction
import nl.rhaydus.softcover.feature.reading.presentation.action.OnShowProgressSheetClickAction
import nl.rhaydus.softcover.feature.reading.presentation.action.ReadingAction

@Composable
internal fun rememberReadingCardEventHandler(
    alsoReading: BookCardSet<Book>,
    runAction: (ReadingAction) -> Unit,
    onBookClick: (Book) -> Unit,
): (BookCardEvent) -> Unit = rememberPrefetchingBookCardEventHandler(
    set = alsoReading,
) { event, book ->
    when (event) {
        is BookCardEvent.Clicked -> onBookClick(book)

        is BookCardEvent.TrailingClicked -> runAction(OnShowProgressSheetClickAction(book = book))

        is BookCardEvent.ShakeEnded -> runAction(OnClearMutationFailureAction(bookId = book.id))

        is BookCardEvent.LongClicked,
        is BookCardEvent.PressStarted,
        is BookCardEvent.MenuClicked,
            -> Unit
    }
}
