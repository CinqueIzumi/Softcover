package nl.rhaydus.softcover.showcase.live.quote.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import nl.rhaydus.designsystem.util.ObserveAsEvents
import nl.rhaydus.softcover.showcase.live.quote.event.QuoteLiveEvent
import nl.rhaydus.softcover.showcase.live.quote.screenmodel.QuoteLiveScreenModel

internal class QuoteLiveScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        val screenModel = koinScreenModel<QuoteLiveScreenModel>()

        val state by screenModel.state.collectAsStateWithLifecycle()

        ObserveAsEvents(flow = screenModel.events) {
            when (it) {
                QuoteLiveEvent.NavigateBack -> navigator.pop()
            }
        }

        QuoteLiveLayout(
            state = state,
            runAction = screenModel::runAction,
        )
    }
}
