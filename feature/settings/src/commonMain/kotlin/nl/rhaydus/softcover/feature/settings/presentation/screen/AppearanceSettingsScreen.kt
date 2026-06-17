package nl.rhaydus.softcover.feature.settings.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import nl.rhaydus.softcover.feature.settings.presentation.action.SettingsAction
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.SettingsScreenScreenModel
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState

class AppearanceSettingsScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        val screenModel = koinScreenModel<SettingsScreenScreenModel>()

        val state by screenModel.state.collectAsStateWithLifecycle()

        AppearanceSettingsScreenLayout(
            state = state,
            onNavigateBack = navigator::pop,
            runAction = screenModel::runAction,
        )
    }
}

/**
 * The Appearance settings body. Desktop (`jvmMain`) and mobile (`mobileMain`) each provide a bespoke
 * `actual` around the shared [AppearanceSettingsContent]; only the chrome (top bar vs static header)
 * branches. On desktop this standalone page is a fallback — the primary entry is the Settings
 * master–detail pane. `expect` cannot carry default argument values, so every parameter is supplied
 * explicitly at the single call site above.
 */
@Composable
internal expect fun AppearanceSettingsScreenLayout(
    state: SettingsScreenUiState,
    runAction: (SettingsAction) -> Unit,
    onNavigateBack: () -> Unit,
)
