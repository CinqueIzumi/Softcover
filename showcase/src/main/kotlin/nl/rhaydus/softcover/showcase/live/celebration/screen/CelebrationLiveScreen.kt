package nl.rhaydus.softcover.showcase.live.celebration.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import nl.rhaydus.designsystem.util.ObserveAsEvents
import nl.rhaydus.softcover.showcase.live.celebration.event.CelebrationLiveEvent
import nl.rhaydus.softcover.showcase.live.celebration.screenmodel.CelebrationLiveScreenModel

internal class CelebrationLiveScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        val screenModel = koinScreenModel<CelebrationLiveScreenModel>()

        val state by screenModel.state.collectAsStateWithLifecycle()

        ObserveAsEvents(flow = screenModel.events) {
            when (it) {
                CelebrationLiveEvent.NavigateBack -> navigator.pop()
            }
        }

        CelebrationLiveLayout(
            state = state,
            runAction = screenModel::runAction,
        )
    }
}
