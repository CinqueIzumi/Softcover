package nl.rhaydus.softcover.feature.settings.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import nl.rhaydus.designsystem.util.ObserveAsEvents
import nl.rhaydus.softcover.feature.settings.presentation.action.OnWidgetsResumedAction
import nl.rhaydus.softcover.feature.settings.presentation.event.WidgetsSettingsEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.WidgetsSettingsScreenModel

class WidgetsSettingsScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        val screenModel = koinScreenModel<WidgetsSettingsScreenModel>()

        val state by screenModel.state.collectAsStateWithLifecycle()

        ObserveAsEvents(flow = screenModel.events) {
            when (it) {
                WidgetsSettingsEvent.NavigateBack -> navigator.pop()
            }
        }

        // Pinning support depends on the launcher, which can change while the app is in the background.
        LifecycleResumeEffect(Unit) {
            screenModel.runAction(OnWidgetsResumedAction())

            onPauseOrDispose { }
        }

        WidgetsSettingsScreenLayout(
            state = state,
            runAction = screenModel::runAction,
        )
    }
}
