package nl.rhaydus.softcover.feature.lists.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import nl.rhaydus.designsystem.component.AdaptiveModalSheet
import nl.rhaydus.designsystem.util.ObserveAsEvents
import nl.rhaydus.designsystem.util.SnackBarManager
import nl.rhaydus.softcover.feature.lists.presentation.event.ListCreatedEvent
import nl.rhaydus.softcover.feature.lists.presentation.event.ListCreationFailedEvent
import nl.rhaydus.softcover.feature.lists.presentation.event.ListNameTakenEvent
import nl.rhaydus.softcover.feature.lists.presentation.screenmodel.CreateListScreenModel

/**
 * The create-list surface — a modal sheet ("List Creation 1a": a title page, not a form), not a pushed
 * screen. [CreateListSheet] hosts this in its own nested [cafe.adriel.voyager.navigator.Navigator],
 * mounted exactly once by the app shell rather than once per call site: [koinScreenModel] needs a
 * `Screen` inside a navigator's store to scope against, and this class exists solely to provide one.
 * [AdaptiveModalSheet] renders through a [androidx.compose.ui.window.Dialog] / `ModalBottomSheet`, so
 * the shell's own content stays visible and composed behind the scrim exactly as the redesign intends
 * — the nested navigator is there only to scope [CreateListScreenModel], not to host a back-stack, and
 * is disposed the moment [CreateListSheet] leaves composition (mirroring
 * [nl.rhaydus.softcover.orchestration.presentation.BookDetailPaneHost]'s nested-navigator technique).
 * `internal` — nothing outside this module needs the `Screen` type itself, only [CreateListSheet].
 */
internal class CreateListScreen(
    private val onDismissRequest: () -> Unit,
    private val onListCreated: ((listId: Int, listName: String) -> Unit)?,
) : Screen {
    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<CreateListScreenModel>()

        val state by screenModel.state.collectAsStateWithLifecycle()

        ObserveAsEvents(flow = screenModel.events) { event ->
            when (event) {
                is ListCreatedEvent -> {
                    // A caller that opened this sheet mid-task (add-to-list) finishes that task with
                    // the new id and reports the outcome its own way — "Added 5 books to X" says more
                    // than "List X created", and two stacked snackbars for one tap says less. Only the
                    // standalone create path (no callback) confirms creation here.
                    if (onListCreated != null) {
                        onListCreated.invoke(
                            event.listId,
                            event.name,
                        )
                    } else {
                        SnackBarManager.showSnackbar(title = "List “${event.name}” created")
                    }

                    onDismissRequest()
                }

                is ListNameTakenEvent -> SnackBarManager.showSnackbar(
                    title = "You already have a list called “${event.name}”. Pick another name.",
                )

                is ListCreationFailedEvent -> SnackBarManager.showSnackbar(
                    title = "Could not create list. Try again.",
                )
            }
        }

        AdaptiveModalSheet(onDismissRequest = onDismissRequest) {
            CreateListSheetContent(
                state = state,
                runAction = screenModel::runAction,
            )
        }
    }
}
