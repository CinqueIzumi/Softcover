package nl.rhaydus.softcover.feature.reading.presentation.action

import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.uibinding.richtext.toReviewDocument
import nl.rhaydus.softcover.core.uibinding.verdict.draftReview
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.toad.ActionScope

internal data class OnSaveVerdictAction(
    private val book: Book,
) : ReadingAction {
    override suspend fun execute(
        dependencies: ReadingScreenDependencies,
        scope: ActionScope<ReadingScreenUiState, ReadingScreenEvent, ReadingLocalVariables>,
    ) {
        val draft = scope.currentState.verdictSheet ?: return

        dependencies.saveBookVerdictUseCase(
            book = book,
            rating = draft.rating,
            review = draft.draftReview().toReviewDocument(),
            hasSpoilers = draft.spoilers.checked,
        )
            .onSuccess {
                scope.sendEvent(ReadingScreenEvent.VerdictSavedEvent())
            }
            .onFailure { error ->
                AppLog.e("$error")

                scope.setState { it.copy(failedMutationBookIds = it.failedMutationBookIds + book.id) }
            }
    }
}
