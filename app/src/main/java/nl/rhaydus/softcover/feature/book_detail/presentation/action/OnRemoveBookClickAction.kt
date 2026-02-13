package nl.rhaydus.softcover.feature.book_detail.presentation.action

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.book_detail.presentation.event.RefreshDetailBookEvent
import timber.log.Timber

class OnRemoveBookClickAction(val book: Book) :
    nl.rhaydus.softcover.feature.book_detail.presentation.action.BookDetailAction {
    override suspend fun execute(
        dependencies: nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies,
        scope: ActionScope<nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState, nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent, nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables>,
    ) {
        dependencies.removeBookFromLibraryUseCase(book = book).onFailure {
            Timber.e("-=- $it")
        }.onSuccess {
            scope.sendEvent(_root_ide_package_.nl.rhaydus.softcover.feature.updated_book_detail.presentation.event.RefreshDetailBookEvent())
        }
    }
}