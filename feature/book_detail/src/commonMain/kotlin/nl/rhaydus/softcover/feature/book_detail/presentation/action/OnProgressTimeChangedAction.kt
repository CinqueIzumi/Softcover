package nl.rhaydus.softcover.feature.book_detail.presentation.action

import nl.rhaydus.softcover.core.component.progress.ProgressTimeUnit
import nl.rhaydus.softcover.core.uibinding.progress.ProgressTimeDraft
import nl.rhaydus.softcover.core.uibinding.progress.progressCurrentSeconds
import nl.rhaydus.softcover.core.uibinding.progress.withTimeDraft
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

internal class OnProgressTimeChangedAction(
    private val unit: ProgressTimeUnit,
    private val text: String,
) : BookDetailAction {
    override suspend fun execute(
        dependencies: BookDetailDependencies,
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
    ) {
        val book = scope.currentState.book ?: return

        scope.setState { state ->
            val draft = ProgressTimeDraft
                .resolve(
                    stored = state.progressTimeDraft,
                    currentSeconds = book.progressCurrentSeconds,
                )
                .withText(
                    unit = unit,
                    text = text,
                )

            state.copy(
                progressTimeDraft = draft,
                progressSheet = state.progressSheet?.withTimeDraft(draft = draft),
            )
        }
    }
}
