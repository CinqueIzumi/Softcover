package nl.rhaydus.softcover.feature.lists.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import nl.rhaydus.designsystem.haptics.LocalHaptics
import nl.rhaydus.designsystem.util.ObserveAsEvents
import nl.rhaydus.designsystem.util.SnackBarManager
import nl.rhaydus.softcover.core.component.haptics.FeedbackEvent
import nl.rhaydus.softcover.core.component.haptics.play
import nl.rhaydus.softcover.core.component.sheet.SheetScaffold
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldEvent
import nl.rhaydus.softcover.core.component.sheet.modelOrNull
import nl.rhaydus.softcover.feature.lists.presentation.action.OnSubmitAction
import nl.rhaydus.softcover.feature.lists.presentation.event.CreateListEvent
import nl.rhaydus.softcover.feature.lists.presentation.screenmodel.CreateListScreenModel

/**
 * The create-list surface — a modal sheet (a title page, not a form), not a pushed
 * screen. [CreateListSheet] hosts this in its own nested [cafe.adriel.voyager.navigator.Navigator],
 * mounted exactly once by the app shell rather than once per call site: [koinScreenModel] needs a
 * `Screen` inside a navigator's store to scope against, and this class exists solely to provide one.
 * [SheetScaffold] renders through a [androidx.compose.ui.window.Dialog] / `ModalBottomSheet`, so
 * the shell's own content stays visible and composed behind the scrim exactly as the redesign intends
 * — the nested navigator is there only to scope [CreateListScreenModel], not to host a back-stack, and
 * is disposed the moment [CreateListSheet] leaves composition (mirroring
 * [nl.rhaydus.softcover.orchestration.presentation.BookDetailPaneHost]'s nested-navigator technique).
 * It holds no state of its own so the back stack survives process-death restore; its callbacks arrive
 * through [LocalCreateListCallbacks].
 * `internal` — nothing outside this module needs the `Screen` type itself, only [CreateListSheet].
 */
internal class CreateListScreen : Screen {
    @Composable
    override fun Content() {
        val callbacks = LocalCreateListCallbacks.current

        val onDismissRequest = callbacks.onDismissRequest

        val onListCreated = callbacks.onListCreated

        val screenModel = koinScreenModel<CreateListScreenModel>()

        val state by screenModel.state.collectAsStateWithLifecycle()

        val haptics = LocalHaptics.current

        val keyboardController = LocalSoftwareKeyboardController.current

        ObserveAsEvents(flow = screenModel.events) { event ->
            if (event is FeedbackEvent) haptics.play(event.feedback)

            when (event) {
                is CreateListEvent.ListCreatedEvent -> {
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
                }

                is CreateListEvent.ListCreationFailedEvent -> {
                    SnackBarManager.showSnackbar(
                        title = event.message ?: "Could not create list. Try again.",
                    )
                }
            }
        }

        state.sheet.modelOrNull?.let { scaffold ->
            SheetScaffold(
                model = scaffold,
                onEvent = { event ->
                    when (event) {
                        SheetScaffoldEvent.PrimaryClicked -> {
                            keyboardController?.hide()

                            screenModel.runAction(OnSubmitAction())
                        }

                        SheetScaffoldEvent.Dismissed -> onDismissRequest()

                        SheetScaffoldEvent.SecondaryClicked,
                        SheetScaffoldEvent.HeaderActionClicked -> Unit
                    }
                },
            ) {
                CreateListSheetContent(
                    state = state,
                    runAction = screenModel::runAction,
                )
            }
        }
    }
}
