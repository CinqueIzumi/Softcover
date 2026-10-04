package nl.rhaydus.softcover.feature.book_detail.presentation.action

import kotlinx.coroutines.flow.firstOrNull
import nl.rhaydus.softcover.core.component.dialog.DatePickerUiModel
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

internal class OnOpenDeadlinePickerAction : BookDetailAction {
    override suspend fun execute(
        dependencies: BookDetailDependencies,
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
    ) {
        val initialDate = scope.currentState.deadline?.deadlineDate
            ?: dependencies.observeCurrentDateUseCase().firstOrNull()
            ?: return

        scope.setState {
            it.copy(
                deadlinePicker = DatePickerUiModel(
                    initialDate = initialDate,
                ),
                fabMenuExpanded = false,
            )
        }
    }
}
