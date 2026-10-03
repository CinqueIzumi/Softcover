package nl.rhaydus.softcover.feature.explore.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.component.DesktopVerticalScrollbar
import nl.rhaydus.designsystem.layout.cappedContentWidth
import nl.rhaydus.designsystem.theme.StandardPreview
import nl.rhaydus.softcover.core.component.topbar.EditorialTopBar
import nl.rhaydus.softcover.core.component.topbar.EditorialTopBarEvent
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.feature.explore.presentation.action.HiddenSuggestionsAction
import nl.rhaydus.softcover.feature.explore.presentation.state.HiddenSuggestionsUiState

/**
 * Standalone desktop Hidden-suggestions page — a direct push from Settings (there is no master–
 * detail pane for it). An [EditorialTopBar] carrying the back control and the page title over the
 * shared [HiddenSuggestionsContent], capped to the reading measure, with a persistent desktop
 * scrollbar.
 */
@Composable
internal actual fun HiddenSuggestionsScreenLayout(
    state: HiddenSuggestionsUiState,
    runAction: (HiddenSuggestionsAction) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val scrollState = rememberScrollState()

    Column(modifier = Modifier.fillMaxSize()) {
        EditorialTopBar(
            model = state.editorialTopBar,
            onEvent = { event ->
                when (event) {
                    EditorialTopBarEvent.BackClicked -> onNavigateBack()
                }
            },
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            HiddenSuggestionsContent(
                state = state,
                runAction = runAction,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .cappedContentWidth()
                    .padding(horizontal = 32.dp),
            )

            DesktopVerticalScrollbar(
                scrollState = scrollState,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .padding(vertical = 4.dp),
            )
        }
    }
}

@StandardPreview
@Composable
private fun HiddenSuggestionsScreenDesktopPreview() {
    SoftcoverTheme {
        HiddenSuggestionsScreenLayout(
            state = HiddenSuggestionsUiState(),
            runAction = {},
            onNavigateBack = {},
        )
    }
}
