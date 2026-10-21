package nl.rhaydus.softcover.showcase.live.dialog.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import nl.rhaydus.designsystem.util.ObserveAsEvents
import nl.rhaydus.softcover.showcase.live.dialog.event.DialogLiveEvent
import nl.rhaydus.softcover.showcase.live.dialog.screenmodel.DialogLiveScreenModel

internal class DialogLiveScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        val screenModel = koinScreenModel<DialogLiveScreenModel>()

        val state by screenModel.state.collectAsStateWithLifecycle()

        ObserveAsEvents(flow = screenModel.events) {
            when (it) {
                DialogLiveEvent.NavigateBack -> navigator.pop()
            }
        }

        DialogLiveLayout(
            state = state,
            runAction = screenModel::runAction,
        )
    }
}
