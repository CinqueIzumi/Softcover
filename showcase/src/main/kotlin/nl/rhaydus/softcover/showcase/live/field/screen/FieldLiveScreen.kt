package nl.rhaydus.softcover.showcase.live.field.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import nl.rhaydus.designsystem.util.ObserveAsEvents
import nl.rhaydus.softcover.showcase.live.field.event.FieldLiveEvent
import nl.rhaydus.softcover.showcase.live.field.screenmodel.FieldLiveScreenModel

internal class FieldLiveScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        val screenModel = koinScreenModel<FieldLiveScreenModel>()

        val state by screenModel.state.collectAsStateWithLifecycle()

        ObserveAsEvents(flow = screenModel.events) {
            when (it) {
                FieldLiveEvent.NavigateBack -> navigator.pop()
            }
        }

        FieldLiveLayout(
            state = state,
            runAction = screenModel::runAction,
        )
    }
}
