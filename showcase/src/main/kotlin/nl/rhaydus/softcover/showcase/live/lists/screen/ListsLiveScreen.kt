package nl.rhaydus.softcover.showcase.live.lists.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import nl.rhaydus.designsystem.util.ObserveAsEvents
import nl.rhaydus.softcover.showcase.live.lists.event.ListsLiveEvent
import nl.rhaydus.softcover.showcase.live.lists.screenmodel.ListsLiveScreenModel

internal class ListsLiveScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        val screenModel = koinScreenModel<ListsLiveScreenModel>()

        val state by screenModel.state.collectAsStateWithLifecycle()

        ObserveAsEvents(flow = screenModel.events) {
            when (it) {
                ListsLiveEvent.NavigateBack -> navigator.pop()
            }
        }

        ListsLiveLayout(
            state = state,
            runAction = screenModel::runAction,
        )
    }
}
