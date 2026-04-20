package nl.rhaydus.softcover.feature.book_detail.presentation.action

import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import timber.log.Timber
import java.time.LocalDate

class OnDeadlinePickedAction(
    private val date: LocalDate,
) : BookDetailAction {
    override suspend fun execute(
        dependencies: BookDetailDependencies,
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
    ) {
        val book = scope.currentState.book ?: return
        val totalPages = book.currentEdition.pages ?: 0
        val currentPage = book.userBookRead?.currentPage ?: 0

        dependencies.setBookDeadlineUseCase(
            bookId = book.id,
            deadlineDate = date,
            currentPage = currentPage,
            totalPages = totalPages,
        ).onFailure {
            Timber.e("-=- $it")
        }

        scope.setState { it.copy(showDeadlinePicker = false) }
    }
}
