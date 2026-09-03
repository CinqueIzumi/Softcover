package nl.rhaydus.softcover.feature.reading.presentation.action

import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.core.component.richtext.RichTextUiModel
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.uibinding.richtext.toReviewDocument
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.toad.ActionScope

internal data class OnSaveVerdictAction(
    private val book: Book,
    private val rating: Double?,
    private val review: RichTextUiModel,
    private val hasSpoilers: Boolean,
) : ReadingAction {
    override suspend fun execute(
        dependencies: ReadingScreenDependencies,
        scope: ActionScope<ReadingScreenUiState, ReadingScreenEvent, ReadingLocalVariables>,
    ) {
        scope.setState {
            it.copy(
                verdictPromptBook = null,
                verdictReview = null,
            )
        }

        dependencies.saveBookVerdictUseCase(
            book = book,
            rating = rating,
            review = review.toReviewDocument(),
            hasSpoilers = hasSpoilers,
        ).onFailure { error ->
            AppLog.e("$error")

            scope.setState { it.copy(failedMutationBookIds = it.failedMutationBookIds + book.id) }
        }
    }
}
