package nl.rhaydus.softcover.feature.book_detail.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalUriHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import nl.rhaydus.designsystem.haptics.rememberHaptics
import nl.rhaydus.designsystem.util.ObserveAsEvents
import nl.rhaydus.softcover.core.component.verdict.VerdictSheetContext
import nl.rhaydus.softcover.core.presentation.connectivity.rememberIsOnline
import nl.rhaydus.softcover.core.presentation.model.BookInitialCover
import nl.rhaydus.softcover.core.presentation.navigation.LocalBookDetailOverlayNavigator
import nl.rhaydus.softcover.core.presentation.navigation.LocalBookDetailPaneCloseHandler
import nl.rhaydus.softcover.core.presentation.navigation.LocalCreateListPresenter
import nl.rhaydus.softcover.core.presentation.navigation.TransientNavArg
import nl.rhaydus.softcover.feature.book_detail.presentation.action.BookDetailAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.FetchBookReviewsAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.InitializeBookWithIdAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnDismissChooseListsSheetAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnOpenVerdictSheetAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnShowChooseListsSheetAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnToggleListMembershipAction
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookMarkedAsReadEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.event.OpenExternalLinkEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.event.RefreshDetailBookEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailScreenScreenModel
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import org.koin.core.parameter.parametersOf

class BookDetailScreen(
    val id: Int,
    @TransientNavArg private val initialCover: BookInitialCover? = null,
    private val transitionSurface: String? = null,
) : Screen {
    // The key must include the book id: Voyager's ScreenModelStore is keyed by the screen key, so a
    // class-constant key makes every book share one BookDetailScreenScreenModel. That surfaces in the
    // two-pane (each book is a fresh nested navigator) as "every selection shows the first book."
    override val key: ScreenKey = "book-detail-$id"

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        val createListPresenter = LocalCreateListPresenter.current

        // In the expanded two-pane layout the detail rides in a pane, not the nav stack — back must
        // clear the pane (handler provided by the shell) rather than pop. Null on the pushed path.
        val paneCloseHandler = LocalBookDetailPaneCloseHandler.current
        val onNavigateBack: () -> Unit = paneCloseHandler ?: { navigator.pop() }

        // The cover viewer is a pushed full-screen Screen, so in the two-pane layout it must push onto
        // the root navigator to avoid being cropped into the detail pane; the detail's own navigator on
        // the pushed path.
        val overlayNavigator = LocalBookDetailOverlayNavigator.current ?: navigator

        val screenModel: BookDetailScreenScreenModel =
            koinScreenModel<BookDetailScreenScreenModel> {
                parametersOf(
                    id,
                    initialCover,
                    transitionSurface,
                )
            }

        val state: BookDetailUiState by screenModel.state.collectAsStateWithLifecycle()

        val haptics = rememberHaptics()

        val uriHandler = LocalUriHandler.current

        var celebrationKey by remember { mutableIntStateOf(0) }

        ObserveAsEvents(flow = screenModel.events) {
            when (it) {
                is RefreshDetailBookEvent -> {
                    screenModel.runAction(
                        action = InitializeBookWithIdAction(id = id),
                    )

                    screenModel.runAction(
                        action = FetchBookReviewsAction(bookId = id),
                    )
                }

                is BookMarkedAsReadEvent -> {
                    haptics.commit()
                    celebrationKey++

                    screenModel.runAction(
                        action = OnOpenVerdictSheetAction(context = VerdictSheetContext.FINISHED),
                    )
                }

                is OpenExternalLinkEvent -> {
                    uriHandler.openUri(uri = it.url)
                }
            }
        }

        val isOnline = rememberIsOnline()

        BookDetailScreenLayout(
            state = state,
            runAction = screenModel::runAction,
            onNavigateBack = onNavigateBack,
            onCoverClick = {
                // Reads the model the collector already mapped rather than mapping here: this lambda
                // only runs on tap, but it is lexically inside a composable and captures composition
                // state, which is exactly the shape R9 forbids.
                val cover = state.fullScreenCover ?: return@BookDetailScreenLayout

                overlayNavigator.push(FullScreenCoverScreen(cover = cover))
            },
            onCreateNewListClick = {
                // Same detour-and-return as Library's bulk path: the reader reached for a new list
                // *while* choosing lists for this book, so creating it puts the book on the new list
                // and brings the chooser back with it ticked. Unlike the bulk path this can reuse the
                // ordinary toggle action, which works off the book and the id alone and so doesn't
                // care that the new list has yet to surface in screen state.
                createListPresenter?.open(
                    onListCreated = { listId, _ ->
                        screenModel.runAction(
                            OnToggleListMembershipAction(
                                listId = listId,
                                isMember = false,
                            ),
                        )

                        screenModel.runAction(OnShowChooseListsSheetAction())
                    },
                )

                screenModel.runAction(OnDismissChooseListsSheetAction())
            },
            isOnline = isOnline,
            celebrationKey = celebrationKey,
        )
    }
}

/**
 * The Book Detail screen body. Desktop (`jvmMain`) and mobile (`mobileMain`) each provide a bespoke
 * `actual`: the shared `ScreenModel` / state / actions / overlays wire up identically in
 * [BookDetailScreen.Content], and only the rendered layout branches — mobile is a single scrolling
 * column, desktop is a fixed identity sidebar beside a scrolling content column. `expect` cannot carry
 * default argument values, so every parameter is supplied explicitly at the single call site above.
 * The screen's id and morph surface are **not** parameters here: the cover's shared-element key is
 * resolved by `CoverModelsCollector` and arrives on `BookDetailUiState.heroCover` (R7/R9).
 */
@Composable
internal expect fun BookDetailScreenLayout(
    state: BookDetailUiState,
    runAction: (BookDetailAction) -> Unit,
    onNavigateBack: () -> Unit,
    onCoverClick: () -> Unit,
    onCreateNewListClick: () -> Unit,
    isOnline: Boolean,
    celebrationKey: Int,
)
