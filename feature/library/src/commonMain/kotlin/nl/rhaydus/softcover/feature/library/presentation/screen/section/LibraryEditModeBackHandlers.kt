package nl.rhaydus.softcover.feature.library.presentation.screen.section

import androidx.compose.runtime.Composable
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import nl.rhaydus.softcover.feature.library.presentation.action.LibraryAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnExitRearrangeModeAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnExitSelectionModeAction

@Composable
internal fun LibraryEditModeBackHandlers(
    selectionMode: Boolean,
    isRearranging: Boolean,
    runAction: (LibraryAction) -> Unit,
) {
    if (selectionMode) {
        val selectionBackState = rememberNavigationEventState(NavigationEventInfo.None)

        NavigationBackHandler(
            state = selectionBackState,
            onBackCancelled = {},
            onBackCompleted = { runAction(OnExitSelectionModeAction()) },
        )
    }

    if (isRearranging) {
        val rearrangeBackState = rememberNavigationEventState(NavigationEventInfo.None)

        NavigationBackHandler(
            state = rearrangeBackState,
            onBackCancelled = {},
            onBackCompleted = { runAction(OnExitRearrangeModeAction()) },
        )
    }
}
