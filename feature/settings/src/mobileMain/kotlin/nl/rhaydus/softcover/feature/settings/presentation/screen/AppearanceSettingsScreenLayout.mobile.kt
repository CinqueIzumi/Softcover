package nl.rhaydus.softcover.feature.settings.presentation.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import nl.rhaydus.designsystem.layout.cappedContentWidth
import nl.rhaydus.designsystem.theme.StandardPreview
import nl.rhaydus.softcover.core.component.topbar.TopBar
import nl.rhaydus.softcover.core.component.topbar.TopBarEvent
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.feature.settings.presentation.action.SettingsAction
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal actual fun AppearanceSettingsScreenLayout(
    state: SettingsScreenUiState,
    runAction: (SettingsAction) -> Unit,
    onNavigateBack: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.testTag(SettingsTestTags.APPEARANCE_PAGE),
        topBar = {
            TopBar(
                model = state.appearanceTopBar,
                onEvent = { event ->
                    when (event) {
                        TopBarEvent.BackClicked -> onNavigateBack()
                    }
                },
            )
        },
    ) { innerPadding ->
        AppearanceSettingsContent(
            state = state,
            runAction = runAction,
            showUiScaleControl = false,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .cappedContentWidth()
                .padding(
                    horizontal = Spacing.gutter,
                    vertical = Spacing.md,
                ),
        )
    }
}

@StandardPreview
@Composable
private fun AppearanceSettingsPreview() {
    SoftcoverTheme {
        AppearanceSettingsScreenLayout(
            state = SettingsScreenUiState(),
            runAction = {},
            onNavigateBack = {},
        )
    }
}
