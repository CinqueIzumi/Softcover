package nl.rhaydus.softcover.feature.book_detail.presentation.action

import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.state.ShareCardSize
import nl.rhaydus.toad.ActionScope

internal class OnShareSizeToggledAction : BookDetailAction {
    override suspend fun execute(
        dependencies: BookDetailDependencies,
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
    ) {
        scope.setState { state ->
            state.copy(
                chosenShareSize = when (state.chosenShareSize) {
                    ShareCardSize.STORY -> ShareCardSize.FULL_TEXT
                    ShareCardSize.FULL_TEXT -> ShareCardSize.STORY
                },
            )
        }
    }
}
