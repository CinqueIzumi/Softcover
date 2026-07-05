package nl.rhaydus.softcover.feature.settings.presentation.screen

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
import nl.rhaydus.softcover.feature.settings.presentation.action.SettingsAction
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState

/**
 * Standalone desktop Appearance page — a fallback for a direct push (the primary desktop entry is the
 * Settings master–detail pane). Static back bar over the shared [AppearanceSettingsContent], capped to
 * the reading measure, with a persistent desktop scrollbar.
 */
@Composable
internal actual fun AppearanceSettingsScreenLayout(
    state: SettingsScreenUiState,
    runAction: (SettingsAction) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val scrollState = rememberScrollState()

    Column(modifier = Modifier.fillMaxSize()) {
        DesktopSettingsBackBar(
            title = "Appearance",
            onNavigateBack = onNavigateBack,
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            AppearanceSettingsContent(
                state = state,
                runAction = runAction,
                showBottomBarToggle = false,
                showUiScaleControl = true,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .cappedContentWidth()
                    .padding(
                        horizontal = 32.dp,
                        vertical = 16.dp,
                    ),
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
