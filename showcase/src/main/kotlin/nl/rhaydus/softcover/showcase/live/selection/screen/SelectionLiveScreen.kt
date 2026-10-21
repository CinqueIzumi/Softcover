package nl.rhaydus.softcover.showcase.live.selection.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import nl.rhaydus.designsystem.util.ObserveAsEvents
import nl.rhaydus.softcover.showcase.live.selection.event.SelectionLiveEvent
import nl.rhaydus.softcover.showcase.live.selection.screenmodel.SelectionLiveScreenModel

internal class SelectionLiveScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        val screenModel = koinScreenModel<SelectionLiveScreenModel>()

        val state by screenModel.state.collectAsStateWithLifecycle()

        ObserveAsEvents(flow = screenModel.events) {
            when (it) {
                SelectionLiveEvent.NavigateBack -> navigator.pop()
            }
        }

        SelectionLiveLayout(
            state = state,
            runAction = screenModel::runAction,
        )
    }
}
