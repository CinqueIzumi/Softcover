package nl.rhaydus.softcover.showcase.live.chip.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import nl.rhaydus.designsystem.util.ObserveAsEvents
import nl.rhaydus.softcover.showcase.live.chip.event.ChipLiveEvent
import nl.rhaydus.softcover.showcase.live.chip.screenmodel.ChipLiveScreenModel

internal class ChipLiveScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        val screenModel = koinScreenModel<ChipLiveScreenModel>()

        val state by screenModel.state.collectAsStateWithLifecycle()

        ObserveAsEvents(flow = screenModel.events) {
            when (it) {
                ChipLiveEvent.NavigateBack -> navigator.pop()
            }
        }

        ChipLiveLayout(
            state = state,
            runAction = screenModel::runAction,
        )
    }
}
