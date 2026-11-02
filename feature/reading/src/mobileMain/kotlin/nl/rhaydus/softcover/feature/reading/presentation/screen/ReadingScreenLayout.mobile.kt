package nl.rhaydus.softcover.feature.reading.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Scaffold
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import nl.rhaydus.designsystem.layout.rememberBottomBarPadding
import nl.rhaydus.softcover.core.component.celebration.MarkAsReadBurst
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.presentation.util.TabReselectScrollToTopEffect
import nl.rhaydus.softcover.feature.reading.presentation.action.OnShowStreakSheetClickAction
import nl.rhaydus.softcover.feature.reading.presentation.action.ReadingAction
import nl.rhaydus.softcover.feature.reading.presentation.action.RefreshAction
import nl.rhaydus.softcover.feature.reading.presentation.screen.section.EmptyCurrentlyReadingScreen
import nl.rhaydus.softcover.feature.reading.presentation.screen.section.ReadingBooksColumn
import nl.rhaydus.softcover.feature.reading.presentation.screen.section.ReadingMasthead
import nl.rhaydus.softcover.feature.reading.presentation.screen.section.ReadingOverlays
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState

private val booksListState = LazyListState()

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal actual fun ReadingScreenLayout(
    state: ReadingScreenUiState,
    runAction: (ReadingAction) -> Unit,
    onBookClick: (Book) -> Unit,
    onNavigateToSearch: () -> Unit,
) {
    TabReselectScrollToTopEffect(
        tab = ReadingTab,
        state = booksListState,
    )

    val pullToRefreshState = rememberPullToRefreshState()

    val controller = rememberMarkAsReadController(
        books = state.books,
        runAction = runAction,
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            contentWindowInsets = WindowInsets.statusBars,
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .imePadding(),
            ) {
                PullToRefreshBox(
                    isRefreshing = state.isRefreshing,
                    onRefresh = { runAction(RefreshAction) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    state = pullToRefreshState,
                    indicator = {
                        PullToRefreshDefaults.LoadingIndicator(
                            state = pullToRefreshState,
                            isRefreshing = state.isRefreshing,
                            modifier = Modifier.align(Alignment.TopCenter),
                        )
                    },
                ) {
                    when {
                        state.books.isNotEmpty() || state.isLoading -> {
                            ReadingBooksColumn(
                                state = state,
                                runAction = runAction,
                                onBookClick = onBookClick,
                                controller = controller,
                                listState = booksListState,
                                contentPadding = PaddingValues(bottom = rememberBottomBarPadding()),
                            ) {
                                ReadingMasthead(
                                    state = state,
                                    onExpandStreak = { runAction(OnShowStreakSheetClickAction) },
                                    pullToRefreshState = pullToRefreshState,
                                )
                            }
                        }

                        else -> EmptyCurrentlyReadingScreen(
                            trendingBooks = state.trendingBooks,
                            pickUpNext = state.pickUpNext,
                            onHold = state.onHold,
                            onHoldHeader = state.onHoldHeader,
                            runAction = runAction,
                            trendingTile = state.trendingTile,
                            pickUpNextHeader = state.pickUpNextHeader,
                            trendingHeader = state.trendingHeader,
                            emptyState = state.emptyCurrentlyReading,
                            streakEnabled = state.streakEnabled,
                            streakStrip = state.streakStrip,
                            onExpandStreak = { runAction(OnShowStreakSheetClickAction) },
                            onBookClick = onBookClick,
                            onNavigateToSearch = onNavigateToSearch,
                        )
                    }
                }

                ReadingOverlays(
                    state = state,
                    runAction = runAction,
                    controller = controller,
                )
            }
        }

        MarkAsReadBurst(
            model = controller.burst,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
