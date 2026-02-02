package nl.rhaydus.softcover.feature.settings.presentation.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import nl.rhaydus.softcover.core.presentation.component.SoftcoverButton
import nl.rhaydus.softcover.core.presentation.component.SoftcoverTopBar
import nl.rhaydus.softcover.core.presentation.model.ButtonSize
import nl.rhaydus.softcover.core.presentation.model.ButtonStyle
import nl.rhaydus.softcover.core.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.presentation.theme.StandardPreview
import nl.rhaydus.softcover.feature.settings.presentation.action.OnLogOutClickAction
import nl.rhaydus.softcover.feature.settings.presentation.action.SettingsAction
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState
import nl.rhaydus.softcover.feature.settings.presentation.viewmodel.SettingsScreenViewModel

// TODO: Maybe display some user info here?
object SettingsScreen : Screen {
    @Composable
    override fun Content() {
        val viewModel = koinScreenModel<SettingsScreenViewModel>()

        val state by viewModel.state.collectAsStateWithLifecycle()

        Screen(
            state = state,
            runAction = viewModel::runAction,
        )
    }

    @Composable
    fun Screen(
        state: SettingsScreenUiState,
        runAction: (SettingsAction) -> Unit,
    ) {
        Scaffold(
            topBar = {
                SoftcoverTopBar(title = "Settings")
            },
            contentWindowInsets = WindowInsets.statusBars,
        ) {
            Column(
                modifier = Modifier
                    .padding(it)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
            ) {
                // TODO: Replace this with user profile information
                Text(text = "Hello user")

                Spacer(modifier = Modifier.height(4.dp))

                SoftcoverButton(
                    label = "Log out",
                    onClick = {
                        runAction(OnLogOutClickAction())
                    },
                    style = ButtonStyle.TONAL,
                    size = ButtonSize.M
                )
            }
        }
    }
}

@StandardPreview
@Composable
private fun SettingsScreenPreview() {
    SoftcoverTheme {
        SettingsScreen.Screen(
            runAction = {},
            state = SettingsScreenUiState()
        )
    }
}