package nl.rhaydus.softcover.feature.book_detail.presentation.action

import nl.rhaydus.common.currentLocalDateTime
import nl.rhaydus.softcover.core.uibinding.progress.shiftedBy
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

internal class OnWhenReadShiftedAction(
    private val minutes: Int,
) : BookDetailAction {
    override suspend fun execute(
        dependencies: BookDetailDependencies,
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
    ) {
        val now = currentLocalDateTime()

        scope.setState {
            it.copy(
                readAt = it.readAt.shiftedBy(
                    minutes = minutes,
                    now = now,
                ),
            )
        }
    }
}
