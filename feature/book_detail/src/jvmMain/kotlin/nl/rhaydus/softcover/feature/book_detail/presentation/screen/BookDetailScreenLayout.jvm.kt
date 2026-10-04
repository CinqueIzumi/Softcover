package nl.rhaydus.softcover.feature.book_detail.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.Flow
import nl.rhaydus.designsystem.component.DesktopVerticalScrollbar
import nl.rhaydus.designsystem.modifier.shakeOnError
import nl.rhaydus.softcover.core.component.callout.Callout
import nl.rhaydus.softcover.core.component.divider.VerticalDivider
import nl.rhaydus.softcover.core.component.state.OfflineEmptyStatePlaceholder
import nl.rhaydus.softcover.core.component.topbar.EditorialTopBar
import nl.rhaydus.softcover.core.component.topbar.EditorialTopBarEvent
import nl.rhaydus.softcover.core.designsystem.presentation.layout.bottomChromePadding
import nl.rhaydus.softcover.feature.book_detail.presentation.action.BookDetailAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnClearMutationFailureAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnLensSelectedAction
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.bookDetailActionFor
import nl.rhaydus.softcover.feature.book_detail.presentation.screen.section.BookDetailOverlays
import nl.rhaydus.softcover.feature.book_detail.presentation.screen.section.BookOverflowMenu
import nl.rhaydus.softcover.feature.book_detail.presentation.screen.section.GeneralBookInfoSection
import nl.rhaydus.softcover.feature.book_detail.presentation.screen.section.LensContent
import nl.rhaydus.softcover.feature.book_detail.presentation.screen.section.LensToggle
import nl.rhaydus.softcover.feature.book_detail.presentation.screen.section.ShelveControlCard
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState

/**
 * Desktop Book Detail. On a wide surface (a full-screen pushed detail) it is a fixed identity sidebar
 * (cover, title, series, rating hero, and the shelve-control card) beside a scrolling narrative column
 * — a sticky lens toggle atop the Yours / The Book lens content (status/progress, your rating/tags/
 * review on Yours; about, tags, find-it, community reviews on The Book). In the narrower expanded
 * two-pane detail slot it collapses to a single scrolling column carrying the same sidebar content
 * inline above the narrative column. A static top strip holds the back/close control and the overflow
 * menu; there is no scroll-collapsing top bar. Every section, and the full set of modal overlays
 * ([BookDetailOverlays]), is shared shelf code — only the arrangement is desktop-specific. The whole
 * surface paints an opaque [Surface] background so a pushed detail never lets the screen beneath it
 * bleed through during the navigation transition.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
internal actual fun BookDetailScreenLayout(
    state: BookDetailUiState,
    runAction: (BookDetailAction) -> Unit,
    onNavigateBack: () -> Unit,
    onCoverClick: () -> Unit,
    onCreateNewListClick: () -> Unit,
    isOnline: Boolean,
    celebrationKey: Int,
    shareCaptureEvents: Flow<BookDetailEvent.ShareCaptureEvent>,
) {
    val showOfflinePlaceholder =
        isOnline.not() && state.book == null && state.loadingBookDetails.not()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            EditorialTopBar(
                model = state.editorialTopBar,
                onEvent = { event ->
                    when (event) {
                        EditorialTopBarEvent.BackClicked -> onNavigateBack()
                    }
                },
                actions = {
                    BookOverflowMenu(
                        state = state,
                        runAction = runAction,
                        isOnline = isOnline,
                    )
                },
            )

            if (showOfflinePlaceholder) {
                state.offlineEmptyState?.let { offlineEmptyState ->
                    OfflineEmptyStatePlaceholder(
                        model = offlineEmptyState,
                        modifier = Modifier.padding(bottom = bottomChromePadding()),
                    )
                }
            } else {
                BoxWithConstraints(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                ) {
                    // Below 720dp a 360dp identity sidebar plus a readable narrative column no longer
                    // fit, so the layout collapses to a single scrolling column. The expanded two-pane
                    // detail slot (`BottomBarScreen`, ~460–700dp) lands here, while a full-screen pushed
                    // detail on a normal desktop window clears it and gets the two-column treatment.
                    if (maxWidth >= 720.dp) {
                        TwoColumnContent(
                            state = state,
                            runAction = runAction,
                            celebrationKey = celebrationKey,
                            onCoverClick = onCoverClick,
                        )
                    } else {
                        SingleColumnContent(
                            state = state,
                            runAction = runAction,
                            celebrationKey = celebrationKey,
                            onCoverClick = onCoverClick,
                        )
                    }
                }
            }
        }
    }

    BookDetailOverlays(
        state = state,
        runAction = runAction,
        onCreateNewListClick = onCreateNewListClick,
        shareCaptureEvents = shareCaptureEvents,
    )
}

@Composable
private fun TwoColumnContent(
    state: BookDetailUiState,
    runAction: (BookDetailAction) -> Unit,
    celebrationKey: Int,
    onCoverClick: () -> Unit,
) {
    Row(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .width(360.dp)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
        ) {
            BookHero(
                state = state,
                runAction = runAction,
                onCoverClick = onCoverClick,
            )

            Spacer(modifier = Modifier.height(20.dp))

            ShelveControlCard(
                state = state,
                runAction = runAction,
                dateStyle = state.dateStyle,
                celebrationKey = celebrationKey,
            )

            Spacer(modifier = Modifier.height(24.dp + bottomChromePadding()))
        }

        VerticalDivider()

        NarrativeColumn(
            state = state,
            runAction = runAction,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        )
    }
}

/**
 * The scrolling narrative column (two-column desktop layout). The lens toggle rides a `stickyHeader`
 * atop it, so it stays pinned while the Yours/The Book content beneath scrolls — the desktop
 * counterpart of the mobile column's sticky lens toggle.
 */
@Composable
private fun NarrativeColumn(
    state: BookDetailUiState,
    runAction: (BookDetailAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()

    Box(modifier = modifier) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = bottomChromePadding()),
            state = listState,
        ) {
            stickyHeader {
                LensToggle(
                    model = state.lensToggle,
                    onLensSelected = { runAction(OnLensSelectedAction(lens = it)) },
                )
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            item {
                LensContent(
                    state = state,
                    runAction = runAction,
                )
            }
        }

        DesktopVerticalScrollbar(
            listState = listState,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .padding(vertical = 4.dp),
        )
    }
}

@Composable
private fun SingleColumnContent(
    state: BookDetailUiState,
    runAction: (BookDetailAction) -> Unit,
    celebrationKey: Int,
    onCoverClick: () -> Unit,
) {
    val listState = rememberLazyListState()

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = bottomChromePadding()),
            state = listState,
        ) {
            item {
                BookHero(
                    state = state,
                    runAction = runAction,
                    onCoverClick = onCoverClick,
                )
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }

            item {
                ShelveControlCard(
                    state = state,
                    runAction = runAction,
                    dateStyle = state.dateStyle,
                    celebrationKey = celebrationKey,
                )
            }

            state.scanEditionCallout?.let { scanEditionCallout ->
                item { Spacer(modifier = Modifier.height(20.dp)) }

                item {
                    Callout(
                        model = scanEditionCallout,
                        onEvent = { event -> runAction(bookDetailActionFor(event)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }

            stickyHeader {
                LensToggle(
                    model = state.lensToggle,
                    onLensSelected = { runAction(OnLensSelectedAction(lens = it)) },
                )
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            item {
                LensContent(
                    state = state,
                    runAction = runAction,
                )
            }
        }

        DesktopVerticalScrollbar(
            listState = listState,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .padding(vertical = 4.dp),
        )
    }
}

@Composable
private fun BookHero(
    state: BookDetailUiState,
    runAction: (BookDetailAction) -> Unit,
    onCoverClick: () -> Unit,
) {
    val currentEditionId = state.book?.currentEdition?.id

    val editionMutationFailed =
        currentEditionId != null && currentEditionId in state.failedMutationEditionIds

    Box(
        modifier = Modifier.shakeOnError(
            trigger = editionMutationFailed,
            onShakeEnd = {
                if (currentEditionId != null) {
                    runAction(
                        OnClearMutationFailureAction(editionId = currentEditionId),
                    )
                }
            },
        ),
    ) {
        GeneralBookInfoSection(
            edition = state.displayedEdition,
            heroCover = state.heroCover,
            backdropCover = state.heroBackdropCover,
            isLoading = state.loadingBookDetails && state.book == null,
            isExpired = state.deadlineProgress?.isExpired == true,
            rating = state.heroRating,
            title = state.heroTitle,
            seriesText = state.heroSeriesText,
            releaseYear = state.displayedEdition?.releaseYear.takeIf { it != -1 }
                ?: state.book?.releaseYear,
            unreleasedBadge = state.unreleasedBadge,
            ownedCoverBadge = state.ownedCoverBadge,
            onCoverClick = onCoverClick,
        )
    }
}
