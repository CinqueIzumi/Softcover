package nl.rhaydus.softcover.feature.explore.presentation.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.layout.cappedContentWidth
import nl.rhaydus.designsystem.theme.StandardPreview
import nl.rhaydus.softcover.core.component.topbar.TopBar
import nl.rhaydus.softcover.core.component.topbar.TopBarEvent
import nl.rhaydus.softcover.core.component.topbar.TopBarNavigation
import nl.rhaydus.softcover.core.component.topbar.TopBarUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.feature.explore.presentation.action.HiddenSuggestionsAction
import nl.rhaydus.softcover.feature.explore.presentation.state.HiddenSuggestionsUiState

/** The bar is fixed for this screen, so the model is a constant rather than rebuilt per frame. */
private val HIDDEN_SUGGESTIONS_TOP_BAR = TopBarUiModel(
    title = "Hidden suggestions",
    navigation = TopBarNavigation.Back,
)

/**
 * Mobile Hidden-suggestions page — a pushed sub-screen of Settings. Uses the standard [TopBar]
 * carrying the "Hidden suggestions" title + back control, exactly like the sibling Appearance /
 * Library-tabs settings pages; the [HiddenSuggestionsContent] body opens with an intro line rather than
 * repeating the title.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal actual fun HiddenSuggestionsScreenLayout(
    state: HiddenSuggestionsUiState,
    runAction: (HiddenSuggestionsAction) -> Unit,
    onNavigateBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopBar(
                model = HIDDEN_SUGGESTIONS_TOP_BAR,
                onEvent = { event ->
                    when (event) {
                        TopBarEvent.BackClicked -> onNavigateBack()
                    }
                },
            )
        },
    ) { innerPadding ->
        HiddenSuggestionsContent(
            state = state,
            runAction = runAction,
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .cappedContentWidth()
                .padding(
                    horizontal = 24.dp,
                    vertical = 16.dp,
                ),
        )
    }
}

@StandardPreview
@Composable
private fun HiddenSuggestionsScreenPreview() {
    SoftcoverTheme {
        HiddenSuggestionsScreenLayout(
            state = HiddenSuggestionsUiState(),
            runAction = {},
            onNavigateBack = {},
        )
    }
}
