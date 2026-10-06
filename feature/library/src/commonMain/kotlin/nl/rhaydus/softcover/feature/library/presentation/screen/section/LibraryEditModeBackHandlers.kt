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
    val selectionBackState = rememberNavigationEventState(NavigationEventInfo.None)

    NavigationBackHandler(
        state = selectionBackState,
        isBackEnabled = selectionMode,
        onBackCancelled = {},
        onBackCompleted = { runAction(OnExitSelectionModeAction()) },
    )

    val rearrangeBackState = rememberNavigationEventState(NavigationEventInfo.None)

    NavigationBackHandler(
        state = rearrangeBackState,
        isBackEnabled = isRearranging,
        onBackCancelled = {},
        onBackCompleted = { runAction(OnExitRearrangeModeAction()) },
    )
}
