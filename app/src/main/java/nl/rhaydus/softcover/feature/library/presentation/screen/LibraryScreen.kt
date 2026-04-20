package nl.rhaydus.softcover.feature.library.presentation.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import nl.rhaydus.softcover.R
import nl.rhaydus.softcover.core.PreviewData
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.presentation.component.DeadlineBadge
import nl.rhaydus.softcover.core.presentation.component.DeadlineCoverOverlay
import nl.rhaydus.softcover.core.presentation.component.DeadlineSummaryLine
import nl.rhaydus.softcover.core.presentation.component.EditionImage
import nl.rhaydus.softcover.core.presentation.component.SoftcoverTopBar
import nl.rhaydus.softcover.core.presentation.modifier.noRippleClickable
import nl.rhaydus.softcover.feature.deadlines.domain.model.BookDeadline
import nl.rhaydus.softcover.feature.deadlines.domain.model.DeadlineProgress
import nl.rhaydus.softcover.feature.deadlines.domain.model.DeadlineUnit
import nl.rhaydus.softcover.core.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.presentation.theme.StandardPreview
import nl.rhaydus.softcover.core.presentation.util.rememberBottomBarPadding
import nl.rhaydus.softcover.feature.book_detail.presentation.screen.BookDetailScreen
import nl.rhaydus.softcover.feature.library.presentation.action.LibraryAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnGridLayoutChangeAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnLayoutMenuExpandedChangeAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnRefreshAction
import nl.rhaydus.softcover.feature.library.presentation.model.LibraryStatusTab
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryScreenScreenModel
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.softcover.feature.search.presentation.screen.SearchScreen
import nl.rhaydus.softcover.feature.settings.domain.model.DateStyle
import nl.rhaydus.softcover.feature.settings.domain.model.LibraryGridLayout

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
            },
            onNavigateToSearch = {
                navigator.parent?.push(item = SearchScreen())
            },
            onEditionClick = {
                navigator.parent?.push(item = BookDetailScreen(id = it.bookId))
            }
        )
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    fun Screen(
        state: LibraryUiState,
        runAction: (LibraryAction) -> Unit,
        onBookClick: (Book) -> Unit,
        onEditionClick: (BookEdition) -> Unit,
        onNavigateToSearch: () -> Unit,
    ) {
        val tabs = LibraryStatusTab.entries
        val scope = rememberCoroutineScope()

        val pullToRefreshState = rememberPullToRefreshState()

        Scaffold(
            topBar = {
                SoftcoverTopBar(
                    title = "Library",
                    onNavigateToSearch = onNavigateToSearch,
                    additionalActions = {
                        LayoutMenuAction(
                            state = state,
                            runAction = runAction,
                        )
                    },
                )
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
                        val currentTab: LibraryStatusTab = tabs[page]

                        if (currentTab == LibraryStatusTab.OWNED) {
                            EditionList(
                                state = state,
                                onEditionClick = onEditionClick,
                            )
                        } else {
                            BookList(
                                statusTab = currentTab,
                                state = state,
                                onBookClick = onBookClick
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun LayoutMenuAction(
        state: LibraryUiState,
        runAction: (LibraryAction) -> Unit,
    ) {
        Box {
            IconButton(
                onClick = {
                    runAction(OnLayoutMenuExpandedChangeAction(expanded = true))
                }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_view_layout),
                    contentDescription = "Change library layout"
                )
            }

            DropdownMenu(
                expanded = state.isLayoutMenuExpanded,
                onDismissRequest = {
                    runAction(OnLayoutMenuExpandedChangeAction(expanded = false))
                }
            ) {
                LibraryGridLayout.entries.forEach { layout ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = layout.label,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        },
                        onClick = {
                            runAction(OnGridLayoutChangeAction(newLayout = layout))
                        }
                    )
                }
            }
        }
    }

    @Composable
    private fun EditionList(
        state: LibraryUiState,
        onEditionClick: (BookEdition) -> Unit,
    ) {
        val editions = state.ownedEditions ?: return

        if (editions.isEmpty() && state.isLoading.not()) {
            EmptyListScreen(listName = LibraryStatusTab.OWNED.label)

            return
        }

        LayoutGrid(
            layout = state.gridLayout,
            gridState = state.ownedEditionsGridState,
        ) {
            items(editions) { edition ->
                LayoutEditionEntry(
                    edition = edition,
                    layout = state.gridLayout,
                    onEditionClick = onEditionClick,
                )
            }
        }
    }

    @Composable
    private fun BookList(
        statusTab: LibraryStatusTab,
        state: LibraryUiState,
        onBookClick: (Book) -> Unit,
    ) {
        val books = when (statusTab) {
            LibraryStatusTab.ALL -> state.allBooks
            LibraryStatusTab.WANT_TO_READ -> state.wantToReadBooks
            LibraryStatusTab.CURRENTLY_READING -> state.currentlyReadingBooks
            LibraryStatusTab.READ -> state.readBooks
            LibraryStatusTab.DID_NOT_FINISH -> state.dnfBooks
            LibraryStatusTab.OWNED -> return
        }

        val gridState: LazyGridState = when (statusTab) {
            LibraryStatusTab.ALL -> state.allBooksGridState
            LibraryStatusTab.WANT_TO_READ -> state.wantToReadBooksGridState
            LibraryStatusTab.CURRENTLY_READING -> state.currentlyReadingBooksGridState
            LibraryStatusTab.READ -> state.readBooksGridState
            LibraryStatusTab.DID_NOT_FINISH -> state.dnfBooksGridState
            LibraryStatusTab.OWNED -> return
        }

        if (books != null && books.isEmpty() && state.isLoading.not()) {
            EmptyListScreen(listName = statusTab.label)

            return
        }

        if (books == null) return

        LayoutGrid(
            layout = state.gridLayout,
            gridState = gridState,
        ) {
            items(books) { book ->
                LayoutBookEntry(
                    book = book,
                    layout = state.gridLayout,
                    onBookClick = onBookClick,
                    deadline = state.deadlines[book.id],
                    dateStyle = state.dateStyle,
                )
            }
        }
    }

    @Composable
    private fun LayoutGrid(
        layout: LibraryGridLayout,
        gridState: LazyGridState,
        content: LazyGridScope.() -> Unit,
    ) {
        val columns = when (layout) {
            LibraryGridLayout.GRID_TWO_COLUMNS -> 2
            LibraryGridLayout.GRID_THREE_COLUMNS -> 3
            LibraryGridLayout.LIST_COMPACT,
            LibraryGridLayout.LIST_LARGE -> 1
        }

        val itemSpacing = when (layout) {
            LibraryGridLayout.GRID_TWO_COLUMNS,
            LibraryGridLayout.GRID_THREE_COLUMNS,
            LibraryGridLayout.LIST_LARGE -> 16.dp
            LibraryGridLayout.LIST_COMPACT -> 4.dp
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = rememberBottomBarPadding()),
            verticalArrangement = Arrangement.spacedBy(itemSpacing),
            horizontalArrangement = Arrangement.spacedBy(itemSpacing),
            state = gridState,
            content = content,
        )
    }

    @Composable
    private fun LayoutBookEntry(
        book: Book,
        layout: LibraryGridLayout,
        onBookClick: (Book) -> Unit,
        deadline: BookDeadline? = null,
        dateStyle: DateStyle = DateStyle.DAY_MONTH_YEAR,
    ) {
        val authorName = book.authors.map { it.name }.firstOrNull().orEmpty()

        val currentEdition = book.currentEdition
        val deadlineProgress = deadline?.let {
            if (currentEdition == null) return@let null
            val current = when (it.unit) {
                DeadlineUnit.PAGES -> book.userBookRead?.currentPage ?: 0
                DeadlineUnit.SECONDS -> book.userBookRead?.currentSeconds ?: 0
            }
            val total = when (it.unit) {
                DeadlineUnit.PAGES -> currentEdition.pages ?: 0
                DeadlineUnit.SECONDS -> currentEdition.audioSeconds ?: 0
            }

            DeadlineProgress.compute(
                deadline = it,
                current = current,
                total = total,
            )
        }

        when (layout) {
            LibraryGridLayout.GRID_TWO_COLUMNS,
            LibraryGridLayout.GRID_THREE_COLUMNS -> {
                GridBookCell(
                    title = book.title,
                    authorName = authorName,
                    onClick = { onBookClick(book) },
                ) { modifier ->
                    DeadlineCoverOverlay(progress = deadlineProgress) {
                        EditionImage(
                            edition = currentEdition,
                            modifier = modifier,
                            isLoading = false,
                            defaultEdition = book.defaultEdition,
                            fallbackCoverUrl = book.coverUrl,
                        )
                    }
                }
            }

            LibraryGridLayout.LIST_COMPACT -> {
                CompactRow(
                    title = book.title,
                    authorName = authorName,
                    onClick = { onBookClick(book) },
                    deadlineProgress = deadlineProgress,
                )
            }

            LibraryGridLayout.LIST_LARGE -> {
                LargeRow(
                    title = book.title,
                    authorName = currentEdition?.authorString.orEmpty(),
                    onClick = { onBookClick(book) },
                    seriesText = book.seriesText,
                    releaseYear = book.releaseYear,
                    usersCount = book.usersCount,
                    rating = book.rating,
                    deadlineProgress = deadlineProgress,
                    dateStyle = dateStyle,
                ) { modifier ->
                    DeadlineCoverOverlay(progress = deadlineProgress) {
                        EditionImage(
                            edition = currentEdition,
                            modifier = modifier,
                            isLoading = false,
                            defaultEdition = book.defaultEdition,
                            fallbackCoverUrl = book.coverUrl,
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun LayoutEditionEntry(
        edition: BookEdition,
        layout: LibraryGridLayout,
        onEditionClick: (BookEdition) -> Unit,
    ) {
        val title = edition.title.orEmpty()
        val authorName = edition.authors.map { it.name }.firstOrNull().orEmpty()

        when (layout) {
            LibraryGridLayout.GRID_TWO_COLUMNS,
            LibraryGridLayout.GRID_THREE_COLUMNS -> {
                GridBookCell(
                    title = title,
                    authorName = authorName,
                    onClick = { onEditionClick(edition) },
                ) { modifier ->
                    EditionImage(
                        edition = edition,
                        modifier = modifier,
                        isLoading = false,
                        defaultEdition = edition,
                    )
                }
            }

            LibraryGridLayout.LIST_COMPACT -> {
                CompactRow(
                    title = title,
                    authorName = authorName,
                    onClick = { onEditionClick(edition) },
                )
            }

            LibraryGridLayout.LIST_LARGE -> {
                LargeRow(
                    title = title,
                    authorName = authorName,
                    onClick = { onEditionClick(edition) },
                ) { modifier ->
                    EditionImage(
                        edition = edition,
                        modifier = modifier,
                        isLoading = false,
                        defaultEdition = edition,
                    )
                }
            }
        }
    }

    @Composable
    private fun GridBookCell(
        title: String,
        authorName: String,
        onClick: () -> Unit,
        cover: @Composable (Modifier) -> Unit,
    ) {
        Column(
            modifier = Modifier.noRippleClickable(onClick = onClick)
        ) {
            cover(Modifier.fillMaxWidth())

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = authorName,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    @Composable
    private fun CompactRow(
        title: String,
        authorName: String,
        onClick: () -> Unit,
        deadlineProgress: DeadlineProgress? = null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .noRippleClickable(onClick = onClick)
                .padding(vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )

                if (deadlineProgress != null) {
                    Spacer(modifier = Modifier.width(8.dp))

                    DeadlineBadge(status = deadlineProgress.status)
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = authorName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    @Composable
    private fun LargeRow(
        title: String,
        authorName: String,
        onClick: () -> Unit,
        seriesText: String? = null,
        releaseYear: Int? = null,
        usersCount: Int? = null,
        rating: Double? = null,
        deadlineProgress: DeadlineProgress? = null,
        dateStyle: DateStyle = DateStyle.DAY_MONTH_YEAR,
        cover: @Composable (Modifier) -> Unit,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .noRippleClickable(onClick = onClick),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            cover(
                Modifier
                    .width(100.dp)
                    .aspectRatio(ratio = 2f / 3f)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                seriesText?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                )

                Text(
                    text = "By $authorName",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                val hasRating = rating != null && rating != 0.0

                val statsLabel = listOfNotNull(
                    releaseYear?.takeIf { it != -1 }?.toString(),
                    usersCount?.let { "$it readers" },
                    rating?.takeIf { it != 0.0 }?.toString(),
                ).joinToString(separator = " • ")

                if (statsLabel.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = statsLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        if (hasRating) {
                            Spacer(modifier = Modifier.width(4.dp))

                            Icon(
                                painter = painterResource(R.drawable.ic_star_filled),
                                contentDescription = "",
                                tint = Color(0xFFFBBF23),
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }

                if (deadlineProgress != null) {
                    DeadlineSummaryLine(
                        progress = deadlineProgress,
                        dateStyle = dateStyle,
                    )
                }
            }
        }
    }

    @Composable
    private fun EmptyListScreen(listName: String) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            Image(
                painter = painterResource(R.drawable.illu_no_results),
                contentDescription = "No images were found"
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "No books were found in your $listName list. Start by adding new books to this list.",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
            onNavigateToSearch = {},
            onEditionClick = {},
        )
    }
}

@StandardPreview
@Composable
private fun LibraryEmptyScreenPreview() {
    SoftcoverTheme {
        LibraryScreen.Screen(
            state = LibraryUiState(
                wantToReadBooks = emptyList(),
                isLoading = false,
            ),
            onBookClick = {},
            runAction = {},
            onNavigateToSearch = {},
            onEditionClick = {},
        )
    }
}
