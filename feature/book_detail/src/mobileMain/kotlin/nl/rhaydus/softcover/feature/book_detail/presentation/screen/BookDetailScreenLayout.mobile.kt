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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.modifier.shakeOnError
import nl.rhaydus.designsystem.theme.StandardPreview
import nl.rhaydus.softcover.core.component.state.EmptyState
import nl.rhaydus.softcover.core.component.state.offlineEmptyStateUiModel
import nl.rhaydus.softcover.core.component.topbar.TopBar
import nl.rhaydus.softcover.core.component.topbar.TopBarEvent
import nl.rhaydus.softcover.core.component.topbar.TopBarNavigation
import nl.rhaydus.softcover.core.component.topbar.TopBarSurface
import nl.rhaydus.softcover.core.component.topbar.TopBarUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.layout.bottomChromePadding
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.domain.model.BookSeries
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.preview.PreviewData
import nl.rhaydus.softcover.feature.book_detail.presentation.action.BookDetailAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnClearMutationFailureAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnLensSelectedAction
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
            state.book?.title.orEmpty()
        }
    }

    // Until the hero has scrolled away the bar sits *over* the cover art, so it goes transparent and
    // its controls take a scrim. `TopBar` derives all of that from this one field — before the
    // component library the bar's fill, the back button's ink and the overflow menu's ink were three
    // separate derivations of this same flag, any one of which could have been forgotten.
    val topBar = remember(title, shouldBeExpanded) {
        TopBarUiModel(
            title = title,
            navigation = TopBarNavigation.Back,
            surface = if (shouldBeExpanded) TopBarSurface.OPAQUE else TopBarSurface.OVER_MEDIA,
        )
    }

    // The offline placeholder has no hero for the bar to sit over, and no `LazyColumn` either — so
    // `shouldBeExpanded` never leaves `false` on that branch and [topBar] above would pin it to
    // OVER_MEDIA, drawing a transparent bar with scrimmed controls over a plain empty state. It gets
    // its own model instead, matching the opaque default the branch had before the bar took a model.
    val offlineTopBar = remember {
        TopBarUiModel(
            title = "",
            navigation = TopBarNavigation.Back,
        )
    }

    val showOfflinePlaceholder =
        isOnline.not() && state.book == null && state.loadingBookDetails.not()

    Scaffold(
        contentWindowInsets = WindowInsets(),
    ) { innerPadding ->
        if (showOfflinePlaceholder) {
            EmptyState(
                model = offlineEmptyStateUiModel(),
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(bottom = bottomChromePadding()),
            )

            TopBar(
                model = offlineTopBar,
                onEvent = { event ->
                    when (event) {
                        TopBarEvent.BackClicked -> onNavigateBack()
                    }
                },
                scrollBehavior = topAppBarScrollBehavior,
            )

            return@Scaffold
        }

        val yoursEnabled = state.book?.userBook != null

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
                        heroCover = state.heroCover,
                        backdropCover = state.heroBackdropCover,
                        isLoading = state.loadingBookDetails && state.book == null,
                        isExpired = state.deadlineProgress?.isExpired == true,
                        rating = state.book?.rating,
                        title = state.book?.title,
                        seriesText = state.book?.seriesText,
                        releaseYear = state.displayedEdition?.releaseYear.takeIf { it != -1 }
                            ?: state.book?.releaseYear,
                        unreleasedBadge = state.unreleasedBadge,
                        isOwned = state.isEditionOwned(edition = state.displayedEdition),
                        onCoverClick = onCoverClick,
                    )
                }
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
                    onLensSelected = { lens -> runAction(OnLensSelectedAction(lens = lens)) },
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

            item { Spacer(modifier = Modifier.height(bottomChromePadding())) }
        }

        TopBar(
            model = topBar,
            onEvent = { event ->
                when (event) {
                    TopBarEvent.BackClicked -> onNavigateBack()
                }
            },
            scrollBehavior = topAppBarScrollBehavior,
        ) { controlColors ->
            BookOverflowMenu(
                state = state,
                runAction = runAction,
                isOnline = isOnline,
                iconColors = controlColors,
            )
        }

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
                celebrationKey = 0,
            )
        }
    }
}
