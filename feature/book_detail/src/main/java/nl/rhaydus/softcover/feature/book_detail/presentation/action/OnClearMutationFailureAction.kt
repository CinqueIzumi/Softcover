package nl.rhaydus.softcover.feature.book_detail.presentation.action

import nl.rhaydus.softcover.core.designsystem.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState

class OnClearMutationFailureAction(
    private val bookId: Int? = null,
    private val editionId: Int? = null,
) : BookDetailAction {
    init {
        require(value = bookId != null || editionId != null) {
            "OnClearMutationFailureAction requires either a bookId or an editionId."
        }
    }

    override suspend fun execute(
        dependencies: BookDetailDependencies,
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
    ) {
        scope.setState {
            it.copy(
                failedMutationBookIds = if (bookId != null) {
                    it.failedMutationBookIds - bookId
                } else {
                    it.failedMutationBookIds
                },
                failedMutationEditionIds = if (editionId != null) {
                    it.failedMutationEditionIds - editionId
                } else {
                    it.failedMutationEditionIds
                },
            )
        }
    }
}
