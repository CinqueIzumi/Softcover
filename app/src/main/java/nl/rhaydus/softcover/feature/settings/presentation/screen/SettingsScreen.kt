package nl.rhaydus.softcover.feature.settings.presentation.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import nl.rhaydus.softcover.R
import nl.rhaydus.softcover.core.presentation.component.SoftcoverButton
import nl.rhaydus.softcover.core.presentation.component.SoftcoverTopBar
import nl.rhaydus.softcover.core.presentation.model.ButtonSize
import nl.rhaydus.softcover.core.presentation.model.ButtonStyle
import nl.rhaydus.softcover.core.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.presentation.theme.StandardPreview
import nl.rhaydus.softcover.feature.settings.presentation.action.OnLogOutClickAction
import nl.rhaydus.softcover.feature.settings.presentation.action.SettingsAction
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.SettingsScreenScreenModel
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState

// TODO: Maybe display some user info here?
object SettingsScreen : Screen {
    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<SettingsScreenScreenModel>()

        val state by screenModel.state.collectAsStateWithLifecycle()

        Screen(
            state = state,
            runAction = screenModel::runAction,
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
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
                    .padding(horizontal = 16.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
            ) {
                // TODO: Replace this with user profile information
                Image(
                    painter = painterResource(id = R.drawable.illu_wip),
                    contentDescription = "Work in progress illustration",
                )

                Text(
                    text = "This page is still in being set-up...",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(8.dp))

                SoftcoverButton(
                    label = "Log out",
                    onClick = {
                        runAction(OnLogOutClickAction())
                    },
                    style = ButtonStyle.TONAL,
                    size = ButtonSize.M,
                    modifier = Modifier.fillMaxWidth(),
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