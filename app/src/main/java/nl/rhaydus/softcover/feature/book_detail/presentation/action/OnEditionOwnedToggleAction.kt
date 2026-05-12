package nl.rhaydus.softcover.feature.book_detail.presentation.action

import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import timber.log.Timber

class OnEditionOwnedToggleAction(
    private val edition: BookEdition,
) : BookDetailAction {
    override suspend fun execute(
        dependencies: BookDetailDependencies,
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
    ) {
        scope.currentLocalVariables.editionMutationJobs[edition.id]?.cancel()

        val job = dependencies.launch {
            dependencies.setEditionAsOwnedUseCase(
                edition = edition,
                owned = edition.owned.not(),
            ).onFailure { error ->
                Timber.e("-=- $error")

                scope.setState {
                    it.copy(failedMutationEditionIds = it.failedMutationEditionIds + edition.id)
                }
            }
        }

        scope.setLocalVariables {
            it.copy(editionMutationJobs = it.editionMutationJobs + (edition.id to job))
        }
    }
}
