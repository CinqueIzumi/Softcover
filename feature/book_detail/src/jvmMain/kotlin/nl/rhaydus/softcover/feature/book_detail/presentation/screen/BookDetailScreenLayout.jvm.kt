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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.component.DesktopTooltip
import nl.rhaydus.designsystem.component.DesktopVerticalScrollbar
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.designsystem.modifier.shakeOnError
import nl.rhaydus.softcover.core.designsystem.presentation.component.OfflineScreenContent
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.layout.bottomChromePadding
import nl.rhaydus.softcover.feature.book_detail.presentation.action.BookDetailAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnClearMutationFailureAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnLensSelectedAction
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState

private val IDENTITY_SIDEBAR_WIDTH = 360.dp

// Below this available width a 360dp identity sidebar plus a readable narrative column no longer fit,
// so the layout collapses to a single scrolling column. The expanded two-pane detail slot
// (`BottomBarScreen`, ~460–700dp) lands here, while a full-screen pushed detail on a normal desktop
// window clears it and gets the two-column treatment.
private val TWO_COLUMN_MIN_WIDTH = 720.dp

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
    bookId: Int,
    transitionSurface: String?,
    celebrationKey: Int,
) {
    val showOfflinePlaceholder =
        isOnline.not() && state.book == null && state.loadingBookDetails.not()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            DesktopBookDetailTopBar(
                state = state,
                runAction = runAction,
                isOnline = isOnline,
                onNavigateBack = onNavigateBack,
            )

            if (showOfflinePlaceholder) {
                OfflineScreenContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = bottomChromePadding()),
                )
            } else {
                BoxWithConstraints(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                ) {
                    if (maxWidth >= TWO_COLUMN_MIN_WIDTH) {
                        TwoColumnContent(
                            state = state,
                            runAction = runAction,
                            bookId = bookId,
                            transitionSurface = transitionSurface,
                            celebrationKey = celebrationKey,
                            onCoverClick = onCoverClick,
                        )
                    } else {
                        SingleColumnContent(
                            state = state,
                            runAction = runAction,
                            bookId = bookId,
                            transitionSurface = transitionSurface,
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
    )
}

@Composable
private fun DesktopBookDetailTopBar(
    state: BookDetailUiState,
    runAction: (BookDetailAction) -> Unit,
    isOnline: Boolean,
    onNavigateBack: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DesktopTooltip(text = "Back") {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.pointerHandCursor(),
            ) {
                val backIcon = drawableIconResource(
                    icon = SoftcoverIcon.ArrowBack,
                    contentDescription = "Navigate back icon",
                )

                Icon(
                    painter = backIcon.getIconPainter(),
                    contentDescription = backIcon.contentDescription,
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        BookOverflowMenu(
            state = state,
            runAction = runAction,
            isOnline = isOnline,
        )
    }
}

@Composable
private fun TwoColumnContent(
    state: BookDetailUiState,
    runAction: (BookDetailAction) -> Unit,
    bookId: Int,
    transitionSurface: String?,
    celebrationKey: Int,
    onCoverClick: () -> Unit,
) {
    Row(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .width(IDENTITY_SIDEBAR_WIDTH)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
        ) {
            BookHero(
                state = state,
                runAction = runAction,
                bookId = bookId,
                transitionSurface = transitionSurface,
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
    val yoursEnabled = state.book?.userBook != null

    Box(modifier = modifier) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = bottomChromePadding()),
            state = listState,
        ) {
            stickyHeader {
                LensToggle(
                    selectedLens = state.selectedLens,
                    onLensSelected = { runAction(OnLensSelectedAction(lens = it)) },
                    yoursEnabled = yoursEnabled,
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
    bookId: Int,
    transitionSurface: String?,
    celebrationKey: Int,
    onCoverClick: () -> Unit,
) {
    val listState = rememberLazyListState()
    val yoursEnabled = state.book?.userBook != null

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
                    bookId = bookId,
                    transitionSurface = transitionSurface,
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

            if (state.showScanEditionUpdateBanner) {
                item { Spacer(modifier = Modifier.height(20.dp)) }

                item {
                    ScanEditionUpdateBanner(
                        isUpdating = state.isUpdatingScannedEdition,
                        runAction = runAction,
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }

            stickyHeader {
                LensToggle(
                    selectedLens = state.selectedLens,
                    onLensSelected = { runAction(OnLensSelectedAction(lens = it)) },
                    yoursEnabled = yoursEnabled,
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
    bookId: Int,
    transitionSurface: String?,
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
            isLoading = state.loadingBookDetails && state.book == null,
            fallBackEdition = state.book?.defaultEdition ?: state.initialCover?.defaultEdition,
            fallbackCoverUrl = state.book?.coverUrl ?: state.initialCover?.fallbackCoverUrl,
            isExpired = state.deadlineProgress?.isExpired == true,
            rating = state.book?.rating,
            title = state.book?.title,
            seriesText = state.book?.seriesText,
            releaseYear = state.displayedEdition?.releaseYear.takeIf { it != -1 }
                ?: state.book?.releaseYear,
            unreleasedDate = state.book?.takeIf { it.isUnreleased }?.effectiveReleaseDate,
            isOwned = state.isEditionOwned(edition = state.displayedEdition),
            bookId = bookId,
            transitionSurface = transitionSurface,
            onCoverClick = onCoverClick,
        )
    }
}
