package nl.rhaydus.softcover.feature.book_detail.presentation.action

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import timber.log.Timber

class OnMarkBookAsWantToReadClickAction(val book: Book) :
    nl.rhaydus.softcover.feature.book_detail.presentation.action.BookDetailAction {
    override suspend fun execute(
        dependencies: nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies,
        scope: ActionScope<nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState, nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent, nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables>,
    ) {
        dependencies.markBookAsWantToReadUseCase(bookId = book.id).onFailure {
            Timber.e("-=- $it")
        }
    }
}