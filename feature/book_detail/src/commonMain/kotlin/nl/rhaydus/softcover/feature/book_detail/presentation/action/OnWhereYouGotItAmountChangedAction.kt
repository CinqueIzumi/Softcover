package nl.rhaydus.softcover.feature.book_detail.presentation.action

import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.withAmountText
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

internal data class OnWhereYouGotItAmountChangedAction(val text: String) : BookDetailAction {
    override suspend fun execute(
        dependencies: BookDetailDependencies,
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
    ) {
        val currencies = dependencies.getCurrenciesUseCase()

        scope.setState { state ->
            state.copy(
                whereYouGotItDraft = state.whereYouGotItDraft?.withAmountText(
                    text = text,
                    currencies = currencies,
                ),
            )
        }
    }
}
