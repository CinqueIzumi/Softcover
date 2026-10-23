package nl.rhaydus.softcover.showcase.live.snackbar.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import nl.rhaydus.designsystem.util.ObserveAsEvents
import nl.rhaydus.softcover.showcase.live.snackbar.event.SnackbarLiveEvent
import nl.rhaydus.softcover.showcase.live.snackbar.screenmodel.SnackbarLiveScreenModel

internal class SnackbarLiveScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        val screenModel = koinScreenModel<SnackbarLiveScreenModel>()

        val state by screenModel.state.collectAsStateWithLifecycle()

        ObserveAsEvents(flow = screenModel.events) {
            when (it) {
                SnackbarLiveEvent.NavigateBack -> navigator.pop()
            }
        }

        SnackbarLiveLayout(
            state = state,
            runAction = screenModel::runAction,
        )
    }
}
