package nl.rhaydus.softcover.feature.book_detail.presentation.action

import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.domain.model.DeadlineUnit
import nl.rhaydus.softcover.core.presentation.error.onApiFailure
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

internal class OnDeadlinePickedAction(
    private val date: LocalDate,
) : BookDetailAction {
    override suspend fun execute(
        dependencies: BookDetailDependencies,
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
    ) {
        val book = scope.currentState.book ?: return
        val edition = book.currentEdition ?: run {
            scope.setState { it.copy(showDeadlinePicker = false) }
            return
        }
        val isAudiobook = edition.isAudiobook

        val total = if (isAudiobook) edition.audioSeconds ?: 0 else edition.pages ?: 0
        val current = if (isAudiobook) {
            book.userBookRead?.currentSeconds ?: 0
        } else {
            book.userBookRead?.currentPage ?: 0
        }
        val unit = if (isAudiobook) DeadlineUnit.SECONDS else DeadlineUnit.PAGES

        dependencies.setBookDeadlineUseCase(
            bookId = book.id,
            deadlineDate = date,
            current = current,
            total = total,
            unit = unit,
        ).onApiFailure()

        scope.setState { it.copy(showDeadlinePicker = false) }
    }
}
