package nl.rhaydus.softcover.feature.settings.presentation.screen

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.softcover.core.component.layout.DesktopScrollPage
import nl.rhaydus.softcover.core.component.topbar.EditorialTopBar
import nl.rhaydus.softcover.core.component.topbar.TopBarEvent
import nl.rhaydus.softcover.feature.settings.presentation.action.SettingsAction
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState

/**
 * Standalone desktop Appearance page, a fallback for a direct push; the primary desktop entry is the
 * Settings master–detail pane.
 */
@Composable
internal actual fun AppearanceSettingsScreenLayout(
    state: SettingsScreenUiState,
    runAction: (SettingsAction) -> Unit,
    onNavigateBack: () -> Unit,
) {
    DesktopScrollPage(
        topBar = {
            EditorialTopBar(
                model = state.appearanceTopBar,
                onEvent = { event ->
                    when (event) {
                        TopBarEvent.BackClicked -> onNavigateBack()
                    }
                },
            )
        },
    ) {
        AppearanceSettingsContent(
            state = state,
            runAction = runAction,
            showUiScaleControl = true,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
