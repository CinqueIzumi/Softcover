package nl.rhaydus.softcover.feature.library.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.IndicatorBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import nl.rhaydus.softcover.core.PreviewData
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.presentation.component.EditionImage
import nl.rhaydus.softcover.core.presentation.component.SoftcoverTopBar
import nl.rhaydus.softcover.core.presentation.modifier.noRippleClickable
import nl.rhaydus.softcover.core.presentation.screen.LocalBottomBarPadding
import nl.rhaydus.softcover.core.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.presentation.theme.StandardPreview
import nl.rhaydus.softcover.feature.book.presentation.screen.BookDetailScreen
import nl.rhaydus.softcover.feature.library.presentation.action.LibraryAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnRefreshAction
import nl.rhaydus.softcover.feature.library.presentation.model.LibraryStatusTab
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryScreenScreenModel

object LibraryScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        val screenModel = koinScreenModel<LibraryScreenScreenModel>()

        val state by screenModel.state.collectAsStateWithLifecycle()

        Screen(
            state = state,
            runAction = screenModel::runAction,
            onBookClick = {
                navigator.parent?.push(item = BookDetailScreen(id = it.id))
            }
        )
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    fun Screen(
        state: LibraryUiState,
        runAction: (LibraryAction) -> Unit,
        onBookClick: (Book) -> Unit,
    ) {
        val tabs = LibraryStatusTab.entries
        val scope = rememberCoroutineScope()

        val pullToRefreshState = rememberPullToRefreshState()

        Scaffold(
            topBar = {
                SoftcoverTopBar(title = "Library")
            },
            contentWindowInsets = WindowInsets.statusBars,
        ) {
            Column(
                modifier = Modifier.padding(it)
            ) {
                PrimaryScrollableTabRow(
                    selectedTabIndex = state.pagerState.currentPage,
                    tabs = {
                        tabs.forEachIndexed { index, tab ->
                            Tab(
                                selected = state.pagerState.currentPage == index,
                                onClick = {
                                    scope.launch {
                                        state.pagerState.animateScrollToPage(index)
                                    }
                                },
                                modifier = Modifier.padding(all = 8.dp),
                            ) {
                                Text(text = tab.label)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(16.dp))

                val pagerModifier = Modifier.weight(1f)

                PullToRefreshBox(
                    isRefreshing = state.isLoading,
                    onRefresh = {
                        runAction(OnRefreshAction())
                    },
                    indicator = {
                        IndicatorBox(
                            modifier = Modifier.align(Alignment.TopCenter),
                            state = pullToRefreshState,
                            isRefreshing = state.isLoading,
                        ) {
                            ContainedLoadingIndicator(modifier = Modifier.align(Alignment.TopCenter))
                        }
                    },
                    state = pullToRefreshState,
                ) {
                    HorizontalPager(
                        state = state.pagerState,
                        modifier = pagerModifier,
                    ) { page ->
                        val books = when (tabs[page]) {
                            LibraryStatusTab.ALL -> state.allBooks
                            LibraryStatusTab.WANT_TO_READ -> state.wantToReadBooks
                            LibraryStatusTab.CURRENTLY_READING -> state.currentlyReadingBooks
                            LibraryStatusTab.READ -> state.readBooks
                            LibraryStatusTab.DID_NOT_FINISH -> state.dnfBooks
                        }

                        val gridState: LazyGridState = when(tabs[page]) {
                            LibraryStatusTab.ALL -> state.allBooksGridState
                            LibraryStatusTab.WANT_TO_READ -> state.wantToReadBooksGridState
                            LibraryStatusTab.CURRENTLY_READING -> state.currentlyReadingBooksGridState
                            LibraryStatusTab.READ -> state.readBooksGridState
                            LibraryStatusTab.DID_NOT_FINISH -> state.dnfBooksGridState
                        }

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            contentPadding = PaddingValues(bottom = LocalBottomBarPadding.current),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            state = gridState,
                        ) {
                            items(books) { book ->
                                BookEntry(
                                    book = book,
                                    onBookClick = onBookClick,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun BookEntry(
        book: Book,
        onBookClick: (Book) -> Unit,
    ) {
        Column(
            modifier = Modifier.noRippleClickable(onClick = { onBookClick(book) })
        ) {
            EditionImage(
                edition = book.currentEdition,
                modifier = Modifier.fillMaxWidth(),
                isLoading = false,
            )

            Text(
                text = book.title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = book.authors.map { it.name }.firstOrNull().toString(),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@StandardPreview
@Composable
private fun LibraryScreenPreview() {
    SoftcoverTheme {
        LibraryScreen.Screen(
            state = LibraryUiState(
                wantToReadBooks = listOf(
                    PreviewData.baseBook.copy(title = "Last to Leave the room"),
                    PreviewData.baseBook.copy(title = "Futility"),
                    PreviewData.baseBook.copy(title = "We call them witches"),
                )
            ),
            onBookClick = {},
            runAction = {},
        )
    }
}