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
import nl.rhaydus.softcover.core.designsystem.presentation.component.rememberIsOnline
import nl.rhaydus.softcover.core.designsystem.presentation.model.BookInitialCover
import nl.rhaydus.softcover.core.designsystem.presentation.navigation.LocalBookDetailOverlayNavigator
import nl.rhaydus.softcover.core.designsystem.presentation.navigation.LocalBookDetailPaneCloseHandler
import nl.rhaydus.softcover.core.designsystem.presentation.navigation.LocalCreateListPresenter
import nl.rhaydus.softcover.core.designsystem.presentation.navigation.TransientNavArg
import nl.rhaydus.softcover.feature.book_detail.presentation.action.BookDetailAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.FetchBookReviewsAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.InitializeBookWithIdAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnDismissChooseListsSheetAction
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
            koinScreenModel<BookDetailScreenScreenModel> { parametersOf(
                id,
                initialCover,
            ) }

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
                val book = state.book ?: return@BookDetailScreenLayout

                overlayNavigator.push(
                    FullScreenCoverScreen(
                        edition = state.displayedEdition,
                        defaultEdition = book.defaultEdition,
                        fallbackCoverUrl = book.coverUrl,
                    ),
                )
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
            bookId = id,
            transitionSurface = transitionSurface,
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
 * [bookId] and [transitionSurface] flow through from the screen's constructor (the cover's shared-
 * element key needs them before the book itself has loaded).
 */
@Composable
internal expect fun BookDetailScreenLayout(
    state: BookDetailUiState,
    runAction: (BookDetailAction) -> Unit,
    onNavigateBack: () -> Unit,
    onCoverClick: () -> Unit,
    onCreateNewListClick: () -> Unit,
    isOnline: Boolean,
    bookId: Int,
    transitionSurface: String?,
    celebrationKey: Int,
)
