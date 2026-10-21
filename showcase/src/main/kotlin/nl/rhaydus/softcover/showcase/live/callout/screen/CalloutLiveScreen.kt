package nl.rhaydus.softcover.showcase.live.callout.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import nl.rhaydus.designsystem.util.ObserveAsEvents
import nl.rhaydus.softcover.showcase.live.callout.event.CalloutLiveEvent
import nl.rhaydus.softcover.showcase.live.callout.screenmodel.CalloutLiveScreenModel

internal class CalloutLiveScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        val screenModel = koinScreenModel<CalloutLiveScreenModel>()

        val state by screenModel.state.collectAsStateWithLifecycle()

        ObserveAsEvents(flow = screenModel.events) {
            when (it) {
                CalloutLiveEvent.NavigateBack -> navigator.pop()
            }
        }

        CalloutLiveLayout(
            state = state,
            runAction = screenModel::runAction,
        )
    }
}
