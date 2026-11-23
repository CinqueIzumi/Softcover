package nl.rhaydus.softcover.feature.book_detail.presentation.action

import kotlinx.coroutines.flow.firstOrNull
import nl.rhaydus.common.runCatchingCancellable
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.toHowItFeltDraft
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

internal class OnOpenHowItFeltSheetAction : BookDetailAction {
    override suspend fun execute(
        dependencies: BookDetailDependencies,
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
    ) {
        if (scope.currentState.book == null) return

        val moods = runCatchingCancellable {
            dependencies.observeBookMoodsUseCase(bookId = scope.currentState.bookId).firstOrNull()
        }.getOrNull() ?: return

        scope.setState { it.copy(howItFeltDraft = moods.toHowItFeltDraft()) }
    }
}
