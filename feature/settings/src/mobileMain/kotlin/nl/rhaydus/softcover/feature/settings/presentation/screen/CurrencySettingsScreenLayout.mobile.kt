package nl.rhaydus.softcover.feature.settings.presentation.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import nl.rhaydus.designsystem.layout.cappedContentWidth
import nl.rhaydus.designsystem.theme.Spacing
import nl.rhaydus.designsystem.theme.StandardPreview
import nl.rhaydus.softcover.core.component.topbar.TopBar
import nl.rhaydus.softcover.core.component.topbar.TopBarEvent
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.feature.settings.presentation.action.CurrencySettingsAction
import nl.rhaydus.softcover.feature.settings.presentation.state.CurrencySettingsUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal actual fun CurrencySettingsScreenLayout(
    state: CurrencySettingsUiState,
    runAction: (CurrencySettingsAction) -> Unit,
    onNavigateBack: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.testTag(SettingsTestTags.CURRENCY_PAGE),
        topBar = {
            TopBar(
                model = state.topBar,
                onEvent = { event ->
                    when (event) {
                        TopBarEvent.BackClicked -> onNavigateBack()
                    }
                },
            )
        },
    ) { innerPadding ->
        CurrencySettingsContent(
            state = state,
            runAction = runAction,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .cappedContentWidth()
                .padding(horizontal = Spacing.gutter),
        )
    }
}

@StandardPreview
@Composable
private fun CurrencySettingsPreview() {
    SoftcoverTheme {
        CurrencySettingsScreenLayout(
            state = CurrencySettingsUiState(),
            runAction = {},
            onNavigateBack = {},
        )
    }
}
