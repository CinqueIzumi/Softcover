package nl.rhaydus.softcover.feature.settings.presentation.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import nl.rhaydus.softcover.core.presentation.component.SoftcoverTopBar
import nl.rhaydus.softcover.core.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.presentation.theme.StandardPreview
import nl.rhaydus.softcover.feature.settings.presentation.action.OnFloatingBarToggledAction
import nl.rhaydus.softcover.feature.settings.presentation.action.SettingsAction
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.SettingsScreenScreenModel
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState

class AppearanceSettingsScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        val screenModel = koinScreenModel<SettingsScreenScreenModel>()

        val state by screenModel.state.collectAsStateWithLifecycle()

        Screen(
            state = state,
            onNavigateBack = navigator::pop,
            runAction = screenModel::runAction,
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Screen(
        state: SettingsScreenUiState,
        runAction: (SettingsAction) -> Unit,
        onNavigateBack: () -> Unit,
    ) {
        Scaffold(
            topBar = {
                SoftcoverTopBar(
                    title = "Appearance settings",
                    onNavigateBack = onNavigateBack,
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier.padding(innerPadding)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Use floating bottom bar")

                        Text(
                            text = "When turned off, a normal docked bottom bar will be used instead of the experimantal floating bottom bar.",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Switch(
                        checked = state.useFloatingBarChecked,
                        onCheckedChange = {
                            runAction(OnFloatingBarToggledAction(newValue = it))
                        }
                    )
                }
            }
        }
    }
}

@StandardPreview
@Composable
private fun AppearanceSettingsPreview() {
    SoftcoverTheme {
        AppearanceSettingsScreen().Screen(
            onNavigateBack = {},
            runAction = {},
            state = SettingsScreenUiState()
        )
    }
}