package nl.rhaydus.softcover.feature.settings.presentation.action

import nl.rhaydus.softcover.core.presentation.error.toUserMessage
import nl.rhaydus.softcover.feature.settings.presentation.event.DocumentPageEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.DocumentPageDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.DocumentPageLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.DocumentPageUiState
import nl.rhaydus.toad.ActionScope

/**
 * Re-fetches the page's published document, bypassing the cache TTL; pull-to-refresh and the error slot's
 * retry both dispatch it. The sections are not set here: the write lands in the cache and
 * the content collector re-emits them, so there is one path by which the rendered sections change.
 */
internal class RefreshDocumentPageAction : DocumentPageAction {
    override suspend fun execute(
        dependencies: DocumentPageDependencies,
        scope: ActionScope<DocumentPageUiState, DocumentPageEvent, DocumentPageLocalVariables>,
    ) {
        if (scope.currentState.isRefreshing) return

        scope.setState {
            it.copy(
                isRefreshing = true,
                error = null,
            )
        }

        dependencies.source.refresh(force = true)
            .onFailure { error ->
                val message = error.toUserMessage() ?: scope.currentState.refreshFailureMessage

                scope.setState { it.copy(error = message) }
            }

        scope.setState { it.copy(isRefreshing = false) }
    }
}
