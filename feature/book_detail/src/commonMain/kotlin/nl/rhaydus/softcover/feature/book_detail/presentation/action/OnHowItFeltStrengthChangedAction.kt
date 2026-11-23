package nl.rhaydus.softcover.feature.book_detail.presentation.action

import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.withStrength
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

internal data class OnHowItFeltStrengthChangedAction(
    val key: String,
    val strength: Int,
) : BookDetailAction {
    override suspend fun execute(
        dependencies: BookDetailDependencies,
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
    ) {
        scope.setState { state ->
            state.copy(
                howItFeltDraft = state.howItFeltDraft?.withStrength(
                    key = key,
                    strength = strength,
                ),
            )
        }
    }
}
