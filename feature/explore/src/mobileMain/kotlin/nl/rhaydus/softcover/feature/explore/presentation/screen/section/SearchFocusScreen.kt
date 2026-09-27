package nl.rhaydus.softcover.feature.explore.presentation.screen.section

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.layout.rememberBottomBarPadding
import nl.rhaydus.softcover.feature.explore.presentation.action.ExploreAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnSearchDismissedAction
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState

private val focusScrollState = ScrollState(initial = 0)

@Composable
internal fun SearchFocusScreen(
    state: ExploreScreenUiState,
    runAction: (ExploreAction) -> Unit,
    contentPadding: PaddingValues,
) {
    Box(
        // Feedback item 11: tapping outside the focused field dismisses the focus surface. Only the
        // state is dismissed here - the top bar owns the platform focus and the keyboard, and lets
        // go of both off the back of this action (see `SearchTopBar`). Clearing focus from
        // this side as well is what used to leave the two out of step.
        modifier = Modifier
            .padding(contentPadding)
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { runAction(OnSearchDismissedAction) }
            },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(focusScrollState)
                .padding(top = 18.dp),
        ) {
            SearchFocusContent(
                queries = state.previousSearchQueries,
                moods = state.moodTags,
                runAction = runAction,
            )

            Spacer(modifier = Modifier.height(rememberBottomBarPadding()))
        }
    }
}
