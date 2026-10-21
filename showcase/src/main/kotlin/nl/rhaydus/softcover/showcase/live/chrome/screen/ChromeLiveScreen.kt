package nl.rhaydus.softcover.showcase.live.chrome.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import nl.rhaydus.designsystem.util.ObserveAsEvents
import nl.rhaydus.softcover.showcase.live.chrome.event.ChromeLiveEvent
import nl.rhaydus.softcover.showcase.live.chrome.screenmodel.ChromeLiveScreenModel

internal class ChromeLiveScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        val screenModel = koinScreenModel<ChromeLiveScreenModel>()

        val state by screenModel.state.collectAsStateWithLifecycle()

        ObserveAsEvents(flow = screenModel.events) {
            when (it) {
                ChromeLiveEvent.NavigateBack -> navigator.pop()
            }
        }

        ChromeLiveLayout(
            state = state,
            runAction = screenModel::runAction,
        )
    }
}
