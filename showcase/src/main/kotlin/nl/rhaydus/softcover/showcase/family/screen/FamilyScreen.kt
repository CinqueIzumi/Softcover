package nl.rhaydus.softcover.showcase.family.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import nl.rhaydus.designsystem.util.ObserveAsEvents
import nl.rhaydus.softcover.core.component.gallery.GalleryFamily
import nl.rhaydus.softcover.showcase.family.event.FamilyEvent
import nl.rhaydus.softcover.showcase.family.screenmodel.FamilyScreenModel
import org.koin.core.parameter.parametersOf

internal class FamilyScreen(private val family: GalleryFamily) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        val screenModel = koinScreenModel<FamilyScreenModel> { parametersOf(family) }

        val state by screenModel.state.collectAsStateWithLifecycle()

        ObserveAsEvents(flow = screenModel.events) {
            when (it) {
                FamilyEvent.NavigateBack -> navigator.pop()
            }
        }

        FamilyScreenLayout(
            state = state,
            runAction = screenModel::runAction,
        )
    }
}
