package nl.rhaydus.softcover.feature.session.presentation.action

import androidx.compose.ui.text.TextRange
import nl.rhaydus.softcover.feature.session.presentation.event.FocusModeEvent
import nl.rhaydus.softcover.feature.session.presentation.screenmodel.FocusModeDependencies
import nl.rhaydus.softcover.feature.session.presentation.state.FocusModeUiState
import nl.rhaydus.softcover.feature.session.presentation.state.LocalFocusModeVariables
import nl.rhaydus.toad.ActionScope

/**
 * Commits the draft to the active session when focus leaves the field, and only if it differs from
 * the stored page.
 */
internal data object OnPageFocusLostAction : FocusModeAction {
    override suspend fun execute(
        dependencies: FocusModeDependencies,
        scope: ActionScope<FocusModeUiState, FocusModeEvent, LocalFocusModeVariables>,
    ) {
        scope.setLocalVariables { it.copy(isFirstFocusEvent = true) }

        scope.setState { it.copy(pageDraft = it.pageDraft.copy(selection = TextRange.Zero)) }

        val storedPage = dependencies.activeSessionController.activeSession.value
            ?.book
            ?.userBookRead
            ?.currentPage
            ?: 0

        scope.currentState.pageDraft.text.toIntOrNull()
            ?.takeIf { it != storedPage }
            ?.let { page ->
                dependencies.activeSessionController.updatePage(newPage = page)

                scope.sendEvent(FocusModeEvent.PageCommittedEvent())
            }
    }
}
