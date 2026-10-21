package nl.rhaydus.softcover.showcase.live.share.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import nl.rhaydus.designsystem.util.ObserveAsEvents
import nl.rhaydus.softcover.showcase.live.share.event.ShareLiveEvent
import nl.rhaydus.softcover.showcase.live.share.screenmodel.ShareLiveScreenModel

internal class ShareLiveScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        val screenModel = koinScreenModel<ShareLiveScreenModel>()

        val state by screenModel.state.collectAsStateWithLifecycle()

        ObserveAsEvents(flow = screenModel.events) {
            when (it) {
                ShareLiveEvent.NavigateBack -> navigator.pop()
            }
        }

        ShareLiveLayout(
            state = state,
            runAction = screenModel::runAction,
        )
    }
}
