package nl.rhaydus.softcover.feature.settings.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import nl.rhaydus.softcover.feature.settings.presentation.action.CurrencySettingsAction
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.CurrencySettingsScreenModel
import nl.rhaydus.softcover.feature.settings.presentation.state.CurrencySettingsUiState

class CurrencySettingsScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        val screenModel = koinScreenModel<CurrencySettingsScreenModel>()

        val state by screenModel.state.collectAsStateWithLifecycle()

        CurrencySettingsScreenLayout(
            state = state,
            runAction = screenModel::runAction,
            onNavigateBack = navigator::pop,
        )
    }
}

@Composable
internal expect fun CurrencySettingsScreenLayout(
    state: CurrencySettingsUiState,
    runAction: (CurrencySettingsAction) -> Unit,
    onNavigateBack: () -> Unit,
)
