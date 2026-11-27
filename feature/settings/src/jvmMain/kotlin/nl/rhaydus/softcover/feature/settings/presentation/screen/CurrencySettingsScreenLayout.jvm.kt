package nl.rhaydus.softcover.feature.settings.presentation.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.designsystem.layout.cappedContentWidth
import nl.rhaydus.designsystem.theme.Spacing
import nl.rhaydus.softcover.core.component.topbar.EditorialTopBar
import nl.rhaydus.softcover.core.component.topbar.TopBarEvent
import nl.rhaydus.softcover.feature.settings.presentation.action.CurrencySettingsAction
import nl.rhaydus.softcover.feature.settings.presentation.state.CurrencySettingsUiState

@Composable
internal actual fun CurrencySettingsScreenLayout(
    state: CurrencySettingsUiState,
    runAction: (CurrencySettingsAction) -> Unit,
    onNavigateBack: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        EditorialTopBar(
            model = state.topBar,
            onEvent = { event ->
                when (event) {
                    TopBarEvent.BackClicked -> onNavigateBack()
                }
            },
        )

        CurrencySettingsContent(
            state = state,
            runAction = runAction,
            modifier = Modifier
                .weight(1f)
                .cappedContentWidth()
                .padding(horizontal = Spacing.gutter),
        )
    }
}
