package nl.rhaydus.softcover.feature.book_detail.presentation.action

import nl.rhaydus.softcover.core.designsystem.presentation.toad.ActionScope
import nl.rhaydus.softcover.core.domain.logging.AppLog
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState

/**
 * Sets whether [edition] is marked owned. [owned] is the **desired new state** — the caller decides
 * the toggle from the authoritative owned source (the local "Owned" list), so this action never
 * infers it from the edition's overlay flag (which isn't populated for off-shelf editions).
 */
internal class OnEditionOwnedToggleAction(
    private val edition: BookEdition,
    private val owned: Boolean,
) : BookDetailAction {
    override suspend fun execute(
        dependencies: BookDetailDependencies,
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
    ) {
        scope.currentLocalVariables.editionMutationJobs[edition.id]?.cancel()

        val job = dependencies.launch {
            dependencies.setEditionAsOwnedUseCase(
                edition = edition,
                owned = owned,
            ).onFailure { error ->
                AppLog.e("$error")

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
