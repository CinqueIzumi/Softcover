package nl.rhaydus.softcover.feature.book_detail.presentation.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.modifier.shakeOnError
import nl.rhaydus.designsystem.theme.StandardPreview
import nl.rhaydus.softcover.core.designsystem.presentation.component.OfflineScreenContent
import nl.rhaydus.softcover.core.designsystem.presentation.component.SoftcoverTopBar
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.model.SoftcoverIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.preview.PreviewData
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.designsystem.presentation.util.BottomNavigationSpacer
import nl.rhaydus.softcover.core.domain.model.BookSeries
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.feature.book_detail.presentation.action.BookDetailAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnClearMutationFailureAction
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState

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
    val topAppBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val lazyListState = rememberLazyListState()

    val shouldBeExpanded by remember {
        derivedStateOf { lazyListState.firstVisibleItemIndex >= 1 }
    }

    val title = remember(shouldBeExpanded) {
        if (shouldBeExpanded.not()) {
            ""
        } else {
            state.book?.title ?: ""
        }
    }

    val containerColor by animateColorAsState(
        if (shouldBeExpanded.not()) {
            Color.Transparent
        } else {
            Color.Unspecified
        },
    )

    val defaultIconButtonColors = IconButtonDefaults.iconButtonColors()
    val iconButtonColorsWithScrim = IconButtonDefaults.iconButtonColors(
        containerColor = Color.Black.copy(alpha = 0.35f),
        contentColor = Color.White,
    )

    val backButtonColors = remember(shouldBeExpanded) {
        if (shouldBeExpanded) {
            defaultIconButtonColors
        } else {
            iconButtonColorsWithScrim
        }
    }

    val showOfflinePlaceholder =
        isOnline.not() && state.book == null && state.loadingBookDetails.not()

    Scaffold(
        contentWindowInsets = WindowInsets(),
    ) { innerPadding ->
        if (showOfflinePlaceholder) {
            OfflineScreenContent(modifier = Modifier.padding(innerPadding))

            SoftcoverTopBar(
                title = "",
                onNavigateBack = onNavigateBack,
                navigateBackButton = {
                    IconButton(
                        onClick = onNavigateBack,
                        colors = backButtonColors,
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
                },
                scrollBehavior = topAppBarScrollBehavior,
            )

            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            state = lazyListState,
        ) {
            item {
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

            item { Spacer(modifier = Modifier.height(20.dp)) }

            item {
                ShelfActionBar(
                    state = state,
                    runAction = runAction,
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

            item { EditionMetadataStrip(state = state) }

            item {
                ExternalLinksStrip(
                    state = state,
                    runAction = runAction,
                )
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

            item { BottomNavigationSpacer() }
        }

        SoftcoverTopBar(
            title = title,
            onNavigateBack = onNavigateBack,
            navigateBackButton = {
                IconButton(
                    onClick = onNavigateBack,
                    colors = backButtonColors,
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
            },
            additionalActions = {
                BookOverflowMenu(
                    state = state,
                    runAction = runAction,
                    isOnline = isOnline,
                    iconColors = backButtonColors,
                )
            },
            scrollBehavior = topAppBarScrollBehavior,
            colors = TopAppBarDefaults.topAppBarColors().copy(
                containerColor = containerColor,
            ),
        )

        BookDetailOverlays(
            state = state,
            runAction = runAction,
            onCreateNewListClick = onCreateNewListClick,
        )
    }
}

@StandardPreview
@Composable
private fun BookDetailScreenReadingPreview() {
    SoftcoverTheme {
        Column(
            modifier = Modifier.background(color = MaterialTheme.colorScheme.background),
        ) {
            val book = PreviewData.baseBook.copy(
                userBook = PreviewData.baseBook.userBook?.copy(status = BookStatus.Reading),
                userBookRead = PreviewData.baseBook.userBookRead?.copy(
                    currentPage = 20,
                    progress = 0.8f,
                ),
                bookSeries = BookSeries(
                    id = 1,
                    name = "The Maze Runner",
                    amountOfBooks = 3,
                ),
                positionsInSeries = listOf(2.0),
            )

            BookDetailScreenLayout(
                state = BookDetailUiState(
                    book = book,
                    loadingBookDetails = true,
                ),
                runAction = {},
                onNavigateBack = {},
                onCoverClick = {},
                onCreateNewListClick = {},
                isOnline = true,
                bookId = 1,
                transitionSurface = null,
                celebrationKey = 0,
            )
        }
    }
}

@StandardPreview
@Composable
private fun BookDetailScreenNonePreview() {
    SoftcoverTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background),
        ) {
            val book = PreviewData.baseBook.copy(
                userBook = PreviewData.baseBook.userBook?.copy(status = BookStatus.None),
            )

            BookDetailScreenLayout(
                state = BookDetailUiState(
                    book = book,
                    loadingBookDetails = false,
                ),
                runAction = {},
                onNavigateBack = {},
                onCoverClick = {},
                onCreateNewListClick = {},
                isOnline = true,
                bookId = 1,
                transitionSurface = null,
                celebrationKey = 0,
            )
        }
    }
}

@StandardPreview
@Composable
private fun BookDetailScreenDnfPreview() {
    SoftcoverTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background),
        ) {
            val book = PreviewData.baseBook.copy(
                userBook = PreviewData.baseBook.userBook?.copy(status = BookStatus.DidNotFinish),
            )

            BookDetailScreenLayout(
                state = BookDetailUiState(
                    book = book,
                    loadingBookDetails = false,
                ),
                runAction = {},
                onNavigateBack = {},
                onCoverClick = {},
                onCreateNewListClick = {},
                isOnline = true,
                bookId = 1,
                transitionSurface = null,
                celebrationKey = 0,
            )
        }
    }
}

@StandardPreview
@Composable
private fun BookDetailScreenWantToReadPreview() {
    SoftcoverTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background),
        ) {
            val book = PreviewData.baseBook.copy(
                userBook = PreviewData.baseBook.userBook?.copy(status = BookStatus.WantToRead),
            )

            BookDetailScreenLayout(
                state = BookDetailUiState(
                    book = book,
                    loadingBookDetails = false,
                ),
                runAction = {},
                onNavigateBack = {},
                onCoverClick = {},
                onCreateNewListClick = {},
                isOnline = true,
                bookId = 1,
                transitionSurface = null,
                celebrationKey = 0,
            )
        }
    }
}

@StandardPreview
@Composable
private fun BookDetailScreenReadPreview() {
    SoftcoverTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background),
        ) {
            val book = PreviewData.baseBook.copy(
                userBook = PreviewData.baseBook.userBook?.copy(status = BookStatus.Read),
            )

            BookDetailScreenLayout(
                state = BookDetailUiState(
                    book = book,
                    loadingBookDetails = false,
                ),
                runAction = {},
                onNavigateBack = {},
                onCoverClick = {},
                onCreateNewListClick = {},
                isOnline = true,
                bookId = 1,
                transitionSurface = null,
                celebrationKey = 0,
            )
        }
    }
}
