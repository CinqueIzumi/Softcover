package nl.rhaydus.softcover.showcase.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import kotlinx.coroutines.Dispatchers
import nl.rhaydus.softcover.showcase.screenmodel.ShowcaseScreenModel

internal class ShowcaseScreen : Screen {
    @Composable
    override fun Content() {
        val screenModel = rememberScreenModel {
            ShowcaseScreenModel(mainDispatcher = Dispatchers.Main.immediate)
        }

        val state by screenModel.state.collectAsStateWithLifecycle()

        ShowcaseRoot(
            state = state,
            runAction = screenModel::runAction,
        )
    }
}
