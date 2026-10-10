package nl.rhaydus.softcover.feature.book_detail.presentation.screen

import androidx.compose.foundation.background
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import nl.rhaydus.designsystem.theme.StandardPreview
import nl.rhaydus.softcover.core.component.state.OfflineEmptyStatePlaceholder
import nl.rhaydus.softcover.core.component.topbar.TopBar
import nl.rhaydus.softcover.core.component.topbar.TopBarEvent
import nl.rhaydus.softcover.core.designsystem.presentation.layout.bottomChromePadding
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.domain.model.BookSeries
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.preview.PreviewData
import nl.rhaydus.softcover.feature.book_detail.presentation.action.BookDetailAction
import nl.rhaydus.softcover.feature.book_detail.presentation.screen.section.BookDetailOverlays
import nl.rhaydus.softcover.feature.book_detail.presentation.screen.section.BookOverflowMenu
import nl.rhaydus.softcover.feature.book_detail.presentation.screen.section.bookDetailItems
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.state.topBarFor

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
internal actual fun BookDetailScreenLayout(
    state: BookDetailUiState,
    runAction: (BookDetailAction) -> Unit,
    onNavigateBack: () -> Unit,
    onCoverClick: () -> Unit,
    onCreateNewListClick: () -> Unit,
    isOnline: Boolean,
) {
    val topAppBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val lazyListState = rememberLazyListState()

    val shouldBeExpanded by remember {
        derivedStateOf { lazyListState.firstVisibleItemIndex >= 1 }
    }

    val showOfflinePlaceholder =
        isOnline.not() && state.book == null && state.loadingBookDetails.not()

    Scaffold(
        contentWindowInsets = WindowInsets(),
    ) { innerPadding ->
        if (showOfflinePlaceholder) {
            state.offlineEmptyState?.let { offlineEmptyState ->
                OfflineEmptyStatePlaceholder(
                    model = offlineEmptyState,
                    modifier = Modifier
                        .padding(innerPadding)
                        .padding(bottom = bottomChromePadding()),
                )
            }

            TopBar(
                model = state.topBar,
                onEvent = { event ->
                    when (event) {
                        TopBarEvent.BackClicked -> onNavigateBack()
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
            bookDetailItems(
                state = state,
                runAction = runAction,
                onCoverClick = onCoverClick,
            )

            item { Spacer(modifier = Modifier.height(bottomChromePadding())) }
        }

        TopBar(
            model = if (shouldBeExpanded) state.topBar else state.overMediaTopBar,
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
                    topBar = topBarFor(book = book),
                    loadingBookDetails = true,
                ),
                runAction = {},
                onNavigateBack = {},
                onCoverClick = {},
                onCreateNewListClick = {},
                isOnline = true,
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
                    topBar = topBarFor(book = book),
                    loadingBookDetails = false,
                ),
                runAction = {},
                onNavigateBack = {},
                onCoverClick = {},
                onCreateNewListClick = {},
                isOnline = true,
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
                    topBar = topBarFor(book = book),
                    loadingBookDetails = false,
                ),
                runAction = {},
                onNavigateBack = {},
                onCoverClick = {},
                onCreateNewListClick = {},
                isOnline = true,
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
                    topBar = topBarFor(book = book),
                    loadingBookDetails = false,
                ),
                runAction = {},
                onNavigateBack = {},
                onCoverClick = {},
                onCreateNewListClick = {},
                isOnline = true,
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
                    topBar = topBarFor(book = book),
                    loadingBookDetails = false,
                ),
                runAction = {},
                onNavigateBack = {},
                onCoverClick = {},
                onCreateNewListClick = {},
                isOnline = true,
            )
        }
    }
}
