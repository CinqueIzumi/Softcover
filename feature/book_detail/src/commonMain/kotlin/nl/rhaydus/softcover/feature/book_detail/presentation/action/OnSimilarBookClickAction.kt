package nl.rhaydus.softcover.feature.book_detail.presentation.action

import nl.rhaydus.softcover.core.presentation.model.BookInitialCover
import nl.rhaydus.softcover.core.presentation.navigation.ScreenDestination
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.similarBooksTransitionSurface
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

internal class OnSimilarBookClickAction(
    val bookId: Int,
) : BookDetailAction {
    override suspend fun execute(
        dependencies: BookDetailDependencies,
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
    ) {
        val locals = scope.currentLocalVariables

        val sourceId = locals.similarBooksSourceId ?: return
        val book = locals.similarBooks.firstOrNull { it.id == bookId } ?: return

        scope.sendEvent(
            BookDetailEvent.OpenSimilarBookEvent(
                destination = ScreenDestination.BookDetail(
                    id = book.id,
                    initialCover = BookInitialCover.fromBook(book),
                    transitionSurface = similarBooksTransitionSurface(sourceBookId = sourceId),
                ),
            ),
        )
    }
}
