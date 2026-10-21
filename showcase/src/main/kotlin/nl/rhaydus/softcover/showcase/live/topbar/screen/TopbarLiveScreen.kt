package nl.rhaydus.softcover.showcase.live.topbar.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import nl.rhaydus.designsystem.util.ObserveAsEvents
import nl.rhaydus.softcover.showcase.live.topbar.event.TopbarLiveEvent
import nl.rhaydus.softcover.showcase.live.topbar.screenmodel.TopbarLiveScreenModel

internal class TopbarLiveScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        val screenModel = koinScreenModel<TopbarLiveScreenModel>()

        val state by screenModel.state.collectAsStateWithLifecycle()

        ObserveAsEvents(flow = screenModel.events) {
            when (it) {
                TopbarLiveEvent.NavigateBack -> navigator.pop()
            }
        }

        TopbarLiveLayout(
            state = state,
            runAction = screenModel::runAction,
        )
    }
}
