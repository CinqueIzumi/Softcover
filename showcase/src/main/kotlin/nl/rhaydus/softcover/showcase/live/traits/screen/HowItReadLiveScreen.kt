package nl.rhaydus.softcover.showcase.live.traits.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import nl.rhaydus.designsystem.util.ObserveAsEvents
import nl.rhaydus.softcover.showcase.live.traits.event.HowItReadLiveEvent
import nl.rhaydus.softcover.showcase.live.traits.screenmodel.HowItReadLiveScreenModel

internal class HowItReadLiveScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        val screenModel = koinScreenModel<HowItReadLiveScreenModel>()

        val state by screenModel.state.collectAsStateWithLifecycle()

        ObserveAsEvents(flow = screenModel.events) {
            when (it) {
                HowItReadLiveEvent.NavigateBack -> navigator.pop()
            }
        }

        HowItReadLiveLayout(
            state = state,
            runAction = screenModel::runAction,
        )
    }
}
