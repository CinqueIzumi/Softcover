package nl.rhaydus.softcover.feature.session.presentation.action

import androidx.compose.ui.text.input.TextFieldValue
import nl.rhaydus.softcover.feature.session.presentation.event.FocusModeEvent
import nl.rhaydus.softcover.feature.session.presentation.mapper.pageCount
import nl.rhaydus.softcover.feature.session.presentation.screenmodel.FocusModeDependencies
import nl.rhaydus.softcover.feature.session.presentation.state.FocusModeUiState
import nl.rhaydus.softcover.feature.session.presentation.state.LocalFocusModeVariables
import nl.rhaydus.toad.ActionScope

/**
 * Applies the field's raw edit to the draft: a number above the page count is rejected and leaves the
 * draft as it was, a non-numeric edit clears the text, and the first callback after focus returns only
 * restores the field's selection.
 */
internal class OnPageDraftChangedAction(
    private val newValue: TextFieldValue,
) : FocusModeAction {
    override suspend fun execute(
        dependencies: FocusModeDependencies,
        scope: ActionScope<FocusModeUiState, FocusModeEvent, LocalFocusModeVariables>,
    ) {
        val totalPages = dependencies.activeSessionController.activeSession.value?.book?.pageCount() ?: return

        var draft = scope.currentState.pageDraft

        if (scope.currentLocalVariables.isFirstFocusEvent) {
            scope.setLocalVariables { it.copy(isFirstFocusEvent = false) }
        } else {
            draft = draft.copy(selection = newValue.selection)
        }

        if (newValue.text == draft.text) {
            scope.setState { it.copy(pageDraft = draft) }

            return
        }

        if (newValue.text.isEmpty()) {
            scope.setState { it.copy(pageDraft = newValue) }

            return
        }

        val newNumber = newValue.text.toIntOrNull()

        if (newNumber == null) {
            scope.setState {
                it.copy(
                    pageDraft = draft.copy(
                        text = "",
                        selection = newValue.selection,
                    ),
                )
            }

            return
        }

        if (newNumber > totalPages) {
            scope.setState { it.copy(pageDraftRejected = true) }

            return
        }

        scope.setState { it.copy(pageDraft = newValue) }
    }
}
