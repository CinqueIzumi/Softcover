package nl.rhaydus.softcover.feature.settings.presentation.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.designsystem.presentation.component.SoftcoverTopBar
import nl.rhaydus.softcover.core.designsystem.presentation.layout.editorialContentWidth
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.designsystem.presentation.theme.StandardPreview
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
        topBar = {
            SoftcoverTopBar(
                title = "Appearance",
                onNavigateBack = onNavigateBack,
            )
        },
    ) { innerPadding ->
        AppearanceSettingsContent(
            state = state,
            runAction = runAction,
            showBottomBarToggle = true,
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .editorialContentWidth()
                .padding(
                    horizontal = 24.dp,
                    vertical = 16.dp,
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
