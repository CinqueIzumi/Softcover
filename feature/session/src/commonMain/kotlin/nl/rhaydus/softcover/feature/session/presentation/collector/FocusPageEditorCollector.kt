package nl.rhaydus.softcover.feature.session.presentation.collector

import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.feature.session.presentation.event.FocusModeEvent
import nl.rhaydus.softcover.feature.session.presentation.mapper.toFocusPageEditor
import nl.rhaydus.softcover.feature.session.presentation.screenmodel.FocusModeDependencies
import nl.rhaydus.softcover.feature.session.presentation.state.FocusModeUiState
import nl.rhaydus.softcover.feature.session.presentation.state.LocalFocusModeVariables
import nl.rhaydus.toad.ActionScope

/**
 * Seeds the page draft from the stored page once, then keeps `pageEditor` in step with the active
 * session and the draft. Later changes to the stored page do not overwrite what the reader has typed.
 */
internal class FocusPageEditorCollector : FocusModeCollector {
    override suspend fun onLaunch(
        scope: ActionScope<FocusModeUiState, FocusModeEvent, LocalFocusModeVariables>,
        dependencies: FocusModeDependencies,
    ) {
        val activeSession = dependencies.activeSessionController.activeSession.filterNotNull()

        val initialPage = activeSession.firstOrNull()?.book?.userBookRead?.currentPage ?: 0

        scope.setState { it.copy(pageDraft = TextFieldValue(text = initialPage.toString())) }

        combine(
            activeSession,
            scope.state.map { it.pageDraft.text }.distinctUntilChanged(),
        ) { active, draft -> active.book.toFocusPageEditor(draft = draft) }
            .collectLatest { editor -> scope.setState { it.copy(pageEditor = editor) } }
    }
}
