package nl.rhaydus.softcover.feature.book_detail.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
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
import nl.rhaydus.softcover.core.designsystem.presentation.component.DesktopTooltip
import nl.rhaydus.softcover.core.designsystem.presentation.component.DesktopVerticalScrollbar
import nl.rhaydus.softcover.core.designsystem.presentation.component.OfflineScreenContent
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.model.SoftcoverIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.modifier.pointerHandCursor
import nl.rhaydus.softcover.core.designsystem.presentation.modifier.shakeOnError
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.feature.book_detail.presentation.action.BookDetailAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnClearMutationFailureAction
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState

private val IDENTITY_SIDEBAR_WIDTH = 360.dp

// Below this available width a 360dp identity sidebar plus a readable narrative column no longer fit,
// so the layout collapses to a single scrolling column. The expanded two-pane detail slot
// (`BottomBarScreen`, ~460–700dp) lands here, while a full-screen pushed detail on a normal desktop
// window clears it and gets the two-column treatment.
private val TWO_COLUMN_MIN_WIDTH = 720.dp

/**
 * Desktop Book Detail. On a wide surface (a full-screen pushed detail) it is a fixed identity sidebar
 * (cover, title, series, rating, shelf-action bar, edition metadata, external links) beside a scrolling
 * narrative column (about, tags, status/progress, your review, community reviews). In the narrower
 * expanded two-pane detail slot it collapses to a single scrolling column. A static top strip holds the
 * back/close control and the overflow menu; there is no scroll-collapsing top bar. Every section, and
 * the full set of modal overlays ([BookDetailOverlays]), is shared shelf code — only the arrangement is
 * desktop-specific. The whole surface paints an opaque [Surface] background so a pushed detail never
 * lets the screen beneath it bleed through during the navigation transition.
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
                OfflineScreenContent(modifier = Modifier.fillMaxSize())
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
                val backIcon = SoftcoverIconResource.Drawable(
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

            ShelfActionBar(
                state = state,
                runAction = runAction,
                celebrationKey = celebrationKey,
            )

            Spacer(modifier = Modifier.height(28.dp))

            EditionMetadataStrip(state = state)

            ExternalLinksStrip(
                state = state,
                runAction = runAction,
            )

            Spacer(modifier = Modifier.height(24.dp))
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
            state = listState,
        ) {
            narrativeItems(
                state = state,
                runAction = runAction,
            )
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

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
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
                ShelfActionBar(
                    state = state,
                    runAction = runAction,
                    celebrationKey = celebrationKey,
                )
            }

            narrativeItems(
                state = state,
                runAction = runAction,
                includeEditionStrips = true,
            )
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

/**
 * The narrative section sequence shared by both desktop arrangements, mirroring the mobile column's
 * leading-spacer rhythm. [includeEditionStrips] is `true` only for the single column (where the edition
 * metadata + external links belong inline); the two-column layout hosts those in its identity sidebar.
 */
private fun LazyListScope.narrativeItems(
    state: BookDetailUiState,
    runAction: (BookDetailAction) -> Unit,
    includeEditionStrips: Boolean = false,
) {
    if (state.showScanEditionUpdateBanner) {
        item { Spacer(modifier = Modifier.height(20.dp)) }

        item {
            ScanEditionUpdateBanner(
                isUpdating = state.isUpdatingScannedEdition,
                runAction = runAction,
            )
        }
    }

    if (state.book?.userBook != null) {
        item { Spacer(modifier = Modifier.height(24.dp)) }

        item(key = "userTags") {
            UserTagsSection(
                state = state,
                runAction = runAction,
            )
        }
    }

    val ratingBook = state.book

    if (ratingBook != null && ratingBook.status == BookStatus.Read) {
        item { Spacer(modifier = Modifier.height(24.dp)) }

        item {
            PersonalRatingRow(
                book = ratingBook,
                runAction = runAction,
            )
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }

        item {
            PersonalReviewSection(
                reviewDocument = ratingBook.userBook?.reviewDocument,
                hasSpoilers = ratingBook.userBook?.reviewHasSpoilers == true,
                runAction = runAction,
            )
        }
    }

    val shelfPanelStatus = state.book?.status
    val shelfPanelWillRender = shelfPanelStatus == BookStatus.Reading ||
        shelfPanelStatus == BookStatus.DidNotFinish

    if (shelfPanelWillRender) {
        item { Spacer(modifier = Modifier.height(20.dp)) }

        item {
            ShelfStatusPanel(
                state = state,
                runAction = runAction,
            )
        }

        item { Spacer(modifier = Modifier.height(28.dp)) }
    } else {
        item { Spacer(modifier = Modifier.height(28.dp)) }
    }

    item { AboutSection(state = state) }

    item(key = "tags") { TagsSection(state = state) }

    if (includeEditionStrips) {
        item { EditionMetadataStrip(state = state) }

        item {
            ExternalLinksStrip(
                state = state,
                runAction = runAction,
            )
        }
    }

    item {
        BelowDescriptionStatusPanel(
            state = state,
            topSpacing = 28.dp,
        )
    }

    item { Spacer(modifier = Modifier.height(36.dp)) }

    item {
        ReviewsSection(
            state = state,
            runAction = runAction,
            dateStyle = state.dateStyle,
        )
    }

    item { Spacer(modifier = Modifier.height(32.dp)) }
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
