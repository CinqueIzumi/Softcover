package nl.rhaydus.softcover.feature.lists.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import nl.rhaydus.softcover.core.designsystem.presentation.util.ObserveAsEvents
import nl.rhaydus.softcover.core.designsystem.presentation.util.SnackBarManager
import nl.rhaydus.softcover.feature.lists.presentation.action.CreateListAction
import nl.rhaydus.softcover.feature.lists.presentation.event.ListCreatedEvent
import nl.rhaydus.softcover.feature.lists.presentation.event.ListCreationFailedEvent
import nl.rhaydus.softcover.feature.lists.presentation.event.ListNameTakenEvent
import nl.rhaydus.softcover.feature.lists.presentation.screenmodel.CreateListScreenModel
import nl.rhaydus.softcover.feature.lists.presentation.state.CreateListUiState

class CreateListScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        val screenModel = koinScreenModel<CreateListScreenModel>()

        val state by screenModel.state.collectAsStateWithLifecycle()

        ObserveAsEvents(flow = screenModel.events) { event ->
            when (event) {
                is ListCreatedEvent -> {
                    SnackBarManager.showSnackbar(title = "List “${event.name}” created")

                    navigator.pop()
                }

                is ListNameTakenEvent -> SnackBarManager.showSnackbar(
                    title = "You already have a list called “${event.name}”. Pick another name.",
                )

                is ListCreationFailedEvent -> SnackBarManager.showSnackbar(
                    title = "Could not create list. Try again.",
                )
            }
        }

        CreateListScreenLayout(
            state = state,
            runAction = screenModel::runAction,
            onNavigateBack = navigator::pop,
        )
    }
}

// The mobile actual keeps today's Scaffold + SoftcoverTopBar; the desktop actual is a centered
// modal-style panel with a static back strip. Both render the shared [CreateListForm]. No default
// arguments — they are not allowed on an expect declaration, so every argument is supplied explicitly
// at the single call site above.
@Composable
internal expect fun CreateListScreenLayout(
    state: CreateListUiState,
    runAction: (CreateListAction) -> Unit,
    onNavigateBack: () -> Unit,
)
