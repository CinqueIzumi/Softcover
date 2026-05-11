package nl.rhaydus.softcover.feature.explore.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
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
import nl.rhaydus.softcover.core.domain.model.BookSeries
import nl.rhaydus.softcover.core.presentation.component.EditionImage
import nl.rhaydus.softcover.core.presentation.component.EditorialSectionHeader
import nl.rhaydus.softcover.core.presentation.component.rememberLazyItemMutationAnimator
import nl.rhaydus.softcover.core.presentation.component.rememberMutationAnimatedModifier
import nl.rhaydus.softcover.core.presentation.component.SoftcoverButton
import nl.rhaydus.softcover.core.presentation.component.SoftcoverSearchTopBar
import nl.rhaydus.softcover.core.presentation.model.ButtonStyle
import nl.rhaydus.softcover.core.presentation.modifier.noRippleClickable
import nl.rhaydus.softcover.core.presentation.modifier.pressScaleClickable
import nl.rhaydus.softcover.core.presentation.modifier.shimmer
import nl.rhaydus.softcover.core.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.presentation.theme.StandardPreview
import nl.rhaydus.softcover.core.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.presentation.transition.bookCoverTransitionKey
import nl.rhaydus.softcover.core.presentation.util.SkeletonCrossfade
import nl.rhaydus.softcover.core.presentation.util.rememberBottomBarPadding
import nl.rhaydus.softcover.feature.book_detail.presentation.screen.BookDetailScreen
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookInitialCover
import nl.rhaydus.softcover.feature.connectivity.presentation.component.OfflineScreenContent
import nl.rhaydus.softcover.feature.connectivity.presentation.component.rememberIsOnline
import nl.rhaydus.softcover.feature.explore.data.mock.ExploreMockData
import nl.rhaydus.softcover.feature.explore.presentation.action.ExploreAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnAddBookToLibraryClickAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnDismissContinueSeriesAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnDismissContinueSeriesBookAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnQueryChangeAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnRemoveAllSearchQueriesClickedAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnRemoveBookFromLibraryClickAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnRemoveSearchQueryClickedAction
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreScreenScreenModel
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import kotlin.time.Duration.Companion.seconds

private const val TRENDING_SKELETON_COUNT = 4
private const val CONTINUE_SERIES_SKELETON_COUNT = 4

object ExploreScreen : Screen {
    private val editorialScrollState = ScrollState(initial = 0)
    private val trendingListState = LazyListState()
    private val continueSeriesListState = LazyListState()

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        val screenModel = koinScreenModel<ExploreScreenScreenModel>()

        val state by screenModel.state.collectAsStateWithLifecycle()

        val isOnline = rememberIsOnline()

        Screen(
            state = state,
            runAction = screenModel::runAction,
            onBookClick = {
                navigator.parent?.push(
                    item = BookDetailScreen(
                        id = it.id,
                        initialCover = BookInitialCover.fromBook(book = it),
                    ),
                )
            },
            isOnline = isOnline,
        )
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
    @Composable
    fun Screen(
        state: ExploreScreenUiState,
        runAction: (ExploreAction) -> Unit,
        onBookClick: (Book) -> Unit,
        isOnline: Boolean,
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                SoftcoverSearchTopBar(
                    onNavigateBack = null,
                    searchText = state.searchText,
                    onSearchValueChange = {
                        runAction(OnQueryChangeAction(newQuery = it))
                    },
                    isLoading = state.isLoading,
                )
            }
        ) { padding ->
            if (isOnline.not()) {
                OfflineScreenContent(modifier = Modifier.padding(padding))
                return@Scaffold
            }

            when {
                state.searchText.isEmpty() -> EditorialContent(
                    state = state,
                    runAction = runAction,
                    onBookClick = onBookClick,
                    contentPadding = padding,
                )

                else -> ActiveSearchContent(
                    state = state,
                    runAction = runAction,
                    onBookClick = onBookClick,
                    contentPadding = padding,
                )
            }
        }
    }

    @Composable
    private fun EditorialContent(
        state: ExploreScreenUiState,
        runAction: (ExploreAction) -> Unit,
        onBookClick: (Book) -> Unit,
        contentPadding: PaddingValues,
    ) {
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize()
                .verticalScroll(editorialScrollState),
            verticalArrangement = Arrangement.spacedBy(40.dp),
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            TrendingSection(
                books = state.trendingBooks,
                isLoading = state.loadingTrendingBooks && state.trendingBooks.isEmpty(),
                onBookClick = onBookClick,
            )

            ContinueSeriesSection(
                books = state.continueSeriesBooks,
                isLoading = state.loadingContinueSeriesBooks && state.continueSeriesBooks.isEmpty(),
                onBookClick = onBookClick,
                runAction = runAction,
            )

            RecentSearchesSection(
                queries = state.previousSearchQueries,
                runAction = runAction,
            )

            Spacer(modifier = Modifier.height(rememberBottomBarPadding()))
        }
    }

    @Composable
    private fun TrendingSection(
        books: List<Book>,
        isLoading: Boolean,
        onBookClick: (Book) -> Unit,
    ) {
        if (isLoading.not() && books.isEmpty()) return

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            SectionHeader(
                eyebrow = "THIS WEEK",
                title = "Trending",
            )

            SkeletonCrossfade(
                isLoading = isLoading,
                label = "TrendingRow",
            ) { loading ->
                if (loading) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        items(TRENDING_SKELETON_COUNT) {
                            TrendingCardSkeleton()
                        }
                    }
                } else {
                    LazyRow(
                        state = trendingListState,
                        contentPadding = PaddingValues(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        items(books, key = { it.id }) { book ->
                            TrendingCard(
                                book = book,
                                onClick = { onBookClick(book) },
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun TrendingCardSkeleton() {
        Column(
            modifier = Modifier.width(150.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f)
                    .clip(RoundedCornerShape(6.dp))
                    .shimmer(isLoading = true),
            )

            Box(
                modifier = Modifier
                    .height(16.dp)
                    .fillMaxWidth(0.9f)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmer(isLoading = true),
            )

            Box(
                modifier = Modifier
                    .height(12.dp)
                    .fillMaxWidth(0.6f)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmer(isLoading = true),
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun ContinueSeriesSection(
        books: List<Book>,
        isLoading: Boolean,
        onBookClick: (Book) -> Unit,
        runAction: (ExploreAction) -> Unit,
    ) {
        if (isLoading.not() && books.isEmpty()) return

        var sheetBook by remember { mutableStateOf<Book?>(null) }

        val sheetState = rememberModalBottomSheetState()

        val coroutineScope = rememberCoroutineScope()

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            SectionHeader(
                eyebrow = "PICK UP WHERE YOU LEFT OFF",
                title = "Up next in your series",
            )

            SkeletonCrossfade(
                isLoading = isLoading,
                label = "ContinueSeriesRow",
            ) { loading ->
                if (loading) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        items(CONTINUE_SERIES_SKELETON_COUNT) {
                            SeriesCardSkeleton()
                        }
                    }
                } else {
                    LazyRow(
                        state = continueSeriesListState,
                        contentPadding = PaddingValues(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        items(books, key = { it.id }) { book ->
                            SeriesCard(
                                book = book,
                                onClick = { onBookClick(book) },
                                onMenuClick = { sheetBook = book },
                            )
                        }
                    }
                }
            }
        }

        sheetBook?.let { book ->
            val dismiss: () -> Unit = {
                coroutineScope.launch {
                    sheetState.hide()
                }.invokeOnCompletion {
                    sheetBook = null
                }
            }

            ModalBottomSheet(
                onDismissRequest = { sheetBook = null },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            ) {
                ContinueSeriesDismissSheet(
                    book = book,
                    onDismissBookClick = {
                        runAction(
                            OnDismissContinueSeriesBookAction(
                                bookId = book.id,
                                bookTitle = book.title,
                            )
                        )

                        dismiss()
                    },
                    onDismissSeriesClick = {
                        val series = book.bookSeries ?: return@ContinueSeriesDismissSheet

                        runAction(
                            OnDismissContinueSeriesAction(
                                seriesId = series.id,
                                seriesName = series.name,
                            )
                        )

                        dismiss()
                    },
                )
            }
        }
    }

    @Composable
    private fun ContinueSeriesDismissSheet(
        book: Book,
        onDismissBookClick: () -> Unit,
        onDismissSeriesClick: () -> Unit,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
        ) {
            EditorialSectionHeader(
                eyebrow = book.bookSeries?.name ?: "Up next",
                headline = book.title,
                modifier = Modifier.padding(horizontal = 24.dp),
            )

            Spacer(modifier = Modifier.height(24.dp))

            DismissSheetOption(
                label = "I've already read this one",
                description = "Hide this book from \"Up next in your series\"",
                onClick = onDismissBookClick,
            )

            book.bookSeries?.let {
                DismissSheetOption(
                    label = "Stop recommending this series",
                    description = "Hide every book from this series",
                    onClick = onDismissSeriesClick,
                )
            }
        }
    }

    @Composable
    private fun DismissSheetOption(
        label: String,
        description: String,
        onClick: () -> Unit,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(
                    horizontal = 24.dp,
                    vertical = 16.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.editorialTypography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Text(
                text = description,
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    @Composable
    private fun SeriesCardSkeleton() {
        Column(
            modifier = Modifier.width(120.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f)
                    .clip(RoundedCornerShape(6.dp))
                    .shimmer(isLoading = true),
            )

            Box(
                modifier = Modifier
                    .height(12.dp)
                    .fillMaxWidth(0.7f)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmer(isLoading = true),
            )

            Box(
                modifier = Modifier
                    .height(14.dp)
                    .fillMaxWidth(0.9f)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmer(isLoading = true),
            )

            Box(
                modifier = Modifier
                    .height(12.dp)
                    .fillMaxWidth(0.4f)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmer(isLoading = true),
            )
        }
    }

    @Composable
    private fun RecentSearchesSection(
        queries: List<String>,
        runAction: (ExploreAction) -> Unit,
    ) {
        if (queries.isEmpty()) return

        Column(
            modifier = Modifier.padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "RECENT SEARCHES",
                    style = MaterialTheme.editorialTypography.eyebrowSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                SoftcoverButton(
                    label = "Clear all",
                    onClick = { runAction(OnRemoveAllSearchQueriesClickedAction()) },
                    style = ButtonStyle.TEXT,
                )
            }

            QueryChipsRow(
                queries = queries,
                runAction = runAction,
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    private fun QueryChipsRow(
        queries: List<String>,
        runAction: (ExploreAction) -> Unit,
    ) {
        val animator = rememberLazyItemMutationAnimator(keys = queries)

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(queries, key = { it }) { query ->
                FilterChip(
                    modifier = rememberMutationAnimatedModifier(animator = animator, itemKey = query),
                    selected = false,
                    onClick = {
                        runAction(
                            OnQueryChangeAction(
                                newQuery = query,
                                searchDelay = 0.seconds,
                            )
                        )
                    },
                    label = { Text(text = query) },
                    trailingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_close),
                            contentDescription = "Remove query icon",
                            modifier = Modifier
                                .size(16.dp)
                                .noRippleClickable {
                                    runAction(OnRemoveSearchQueryClickedAction(query = query))
                                },
                        )
                    },
                )
            }
        }
    }

    @Composable
    private fun SectionHeader(
        eyebrow: String,
        title: String,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = eyebrow,
                style = MaterialTheme.editorialTypography.eyebrow,
                color = MaterialTheme.colorScheme.primary,
            )

            Text(
                text = title,
                style = MaterialTheme.editorialTypography.display,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }

    @Composable
    private fun TrendingCard(
        book: Book,
        onClick: () -> Unit,
    ) {
        Column(
            modifier = Modifier
                .width(150.dp)
                .pressScaleClickable(onClick = onClick),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            EditionImage(
                edition = book.currentEdition,
                defaultEdition = book.defaultEdition,
                isLoading = false,
                fallbackCoverUrl = book.coverUrl,
                modifier = Modifier.fillMaxWidth(),
                elevation = 6.dp,
                cornerRadius = 6.dp,
                sharedTransitionKey = bookCoverTransitionKey(
                    editionId = book.currentEdition?.id,
                    bookId = book.id,
                ),
            )

            Text(
                text = book.title,
                style = MaterialTheme.editorialTypography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                minLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            Text(
                text = book.currentEdition?.authorString.orEmpty(),
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.height(16.dp),
            ) {
                if (book.rating != 0.0) {
                    Icon(
                        painter = painterResource(R.drawable.ic_star_filled),
                        contentDescription = "",
                        tint = Color(0xFFFBBF23),
                        modifier = Modifier.size(14.dp),
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "%.1f".format(book.rating),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }

    @Composable
    private fun SeriesCard(
        book: Book,
        onClick: () -> Unit,
        onMenuClick: () -> Unit,
    ) {
        Column(
            modifier = Modifier
                .width(120.dp)
                .pressScaleClickable(onClick = onClick),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                EditionImage(
                    edition = book.currentEdition,
                    defaultEdition = book.defaultEdition,
                    isLoading = false,
                    fallbackCoverUrl = book.coverUrl,
                    modifier = Modifier.fillMaxWidth(),
                    elevation = 4.dp,
                    cornerRadius = 6.dp,
                    sharedTransitionKey = bookCoverTransitionKey(
                        editionId = book.currentEdition?.id,
                        bookId = book.id,
                    ),
                )

                IconButton(
                    onClick = onMenuClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(2.dp)
                        .size(15.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                            shape = CircleShape,
                        ),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_more_vert),
                        contentDescription = "More options",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }

            book.bookSeries?.let { series ->
                Text(
                    text = series.name,
                    style = MaterialTheme.editorialTypography.eyebrowSmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Text(
                text = book.title,
                style = MaterialTheme.editorialTypography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                minLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            Text(
                text = book.positionInSeriesDisplay?.let { "Book #$it" }.orEmpty(),
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                minLines = 1,
                maxLines = 1,
            )
        }
    }

    @Composable
    private fun ActiveSearchContent(
        state: ExploreScreenUiState,
        runAction: (ExploreAction) -> Unit,
        onBookClick: (Book) -> Unit,
        contentPadding: PaddingValues,
    ) {
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .padding(horizontal = 24.dp)
                .fillMaxSize(),
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            val text = when {
                state.isLoading -> "Loading..."
                state.queriedBooks.isEmpty() -> "No results found"
                else -> "Showing ${state.queriedBooks.size} results"
            }

            Text(
                text = text,
                style = MaterialTheme.editorialTypography.eyebrowSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(state.queriedBooks, key = { it.id }) { book ->
                    SearchResultRow(
                        book = book,
                        onBookClick = onBookClick,
                        runAction = runAction,
                    )
                }
            }
        }
    }

    @Composable
    private fun SearchResultRow(
        book: Book,
        onBookClick: (Book) -> Unit,
        runAction: (ExploreAction) -> Unit,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .pressScaleClickable(onClick = { onBookClick(book) }),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            EditionImage(
                edition = book.currentEdition,
                modifier = Modifier.width(80.dp),
                isLoading = false,
                defaultEdition = book.defaultEdition,
                fallbackCoverUrl = book.coverUrl,
                sharedTransitionKey = bookCoverTransitionKey(
                    editionId = book.currentEdition?.id,
                    bookId = book.id,
                ),
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                book.seriesText?.let { seriesText ->
                    Text(
                        text = seriesText,
                        style = MaterialTheme.editorialTypography.eyebrowSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                Text(
                    text = book.title,
                    style = MaterialTheme.editorialTypography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Text(
                    text = "By ${book.currentEdition?.authorString.orEmpty()}",
                    style = MaterialTheme.editorialTypography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                val strings = listOf(
                    book.releaseYear.takeIf { it != -1 },
                    book.usersCount.let { "$it readers" },
                    book.rating.takeIf { it != 0.0 },
                ).mapNotNull { it?.toString() }

                val label = strings.joinToString(separator = " • ") { it }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    if (book.rating != 0.0) {
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

            Spacer(modifier = Modifier.width(4.dp))

            val addedToLibrary = book.userBook != null

            IconToggleButton(
                checked = addedToLibrary,
                onCheckedChange = { newValue: Boolean ->
                    when (newValue) {
                        true -> runAction(OnAddBookToLibraryClickAction(book = book))
                        false -> runAction(OnRemoveBookFromLibraryClickAction(book = book))
                    }
                },
            ) {
                val iconResource = when {
                    addedToLibrary -> R.drawable.ic_bookmark_added
                    else -> R.drawable.ic_bookmark_add
                }

                val contentDescription = when {
                    addedToLibrary -> "Remove from library icon"
                    else -> "Add to library icon"
                }

                Icon(
                    painter = painterResource(iconResource),
                    contentDescription = contentDescription,
                )
            }
        }
    }
}

private val previewMockState = ExploreScreenUiState(
    previousSearchQueries = listOf(
        "Bubblegum",
        "Earthlings",
        "Convenience Store",
        "Babel",
        "Piranesi",
    ),
    trendingBooks = ExploreMockData.trending,
    loadingTrendingBooks = false,
    continueSeriesBooks = ExploreMockData.continueSeries,
    loadingContinueSeriesBooks = false,
)

@StandardPreview
@Composable
private fun ExploreScreenPreview() {
    SoftcoverTheme {
        ExploreScreen.Screen(
            runAction = {},
            onBookClick = {},
            isOnline = true,
            state = previewMockState,
        )
    }
}

@StandardPreview
@Composable
private fun EmptyFirstLaunchExploreScreenPreview() {
    SoftcoverTheme {
        ExploreScreen.Screen(
            runAction = {},
            onBookClick = {},
            isOnline = true,
            state = ExploreScreenUiState(
                trendingBooks = ExploreMockData.trending,
                loadingTrendingBooks = false,
                continueSeriesBooks = emptyList(),
                loadingContinueSeriesBooks = false,
                previousSearchQueries = emptyList(),
            ),
        )
    }
}

@StandardPreview
@Composable
private fun LoadingTrendingExploreScreenPreview() {
    SoftcoverTheme {
        ExploreScreen.Screen(
            runAction = {},
            onBookClick = {},
            isOnline = true,
            state = ExploreScreenUiState(
                trendingBooks = emptyList(),
                loadingTrendingBooks = true,
                continueSeriesBooks = ExploreMockData.continueSeries,
                loadingContinueSeriesBooks = false,
                previousSearchQueries = listOf("Bubblegum", "Earthlings"),
            ),
        )
    }
}

@StandardPreview
@Composable
private fun LoadingContinueSeriesExploreScreenPreview() {
    SoftcoverTheme {
        ExploreScreen.Screen(
            runAction = {},
            onBookClick = {},
            isOnline = true,
            state = ExploreScreenUiState(
                trendingBooks = ExploreMockData.trending,
                loadingTrendingBooks = false,
                continueSeriesBooks = emptyList(),
                loadingContinueSeriesBooks = true,
                previousSearchQueries = emptyList(),
            ),
        )
    }
}

@StandardPreview
@Composable
private fun OfflineExploreScreenPreview() {
    SoftcoverTheme {
        ExploreScreen.Screen(
            runAction = {},
            onBookClick = {},
            isOnline = false,
            state = previewMockState,
        )
    }
}

@StandardPreview
@Composable
private fun ActiveExploreScreenPreview() {
    SoftcoverTheme {
        ExploreScreen.Screen(
            runAction = {},
            onBookClick = {},
            isOnline = true,
            state = ExploreScreenUiState(
                searchText = "Last to leave",
                queriedBooks = listOf(
                    PreviewData.baseBook.copy(
                        title = "Last to Leave the Room",
                        defaultEdition = PreviewData.baseEdition.copy(releaseYear = 2023),
                        rating = 3.7,
                        bookSeries = BookSeries(
                            id = 1,
                            name = "Starling",
                            amountOfBooks = 20,
                        ),
                    ),
                    PreviewData.baseBook.copy(
                        id = 2,
                        title = "The Last to Leave",
                        defaultEdition = PreviewData.baseEdition.copy(releaseYear = 2021),
                        rating = 4.2,
                        userBook = PreviewData.baseBook.userBook,
                        bookSeries = BookSeries(
                            id = 1,
                            name = "Starling",
                            amountOfBooks = 20,
                        ),
                        positionsInSeries = listOf(2.0),
                    ),
                    PreviewData.baseBook.copy(
                        id = 3,
                        title = "Last One to Leave",
                        defaultEdition = PreviewData.baseEdition.copy(releaseYear = 2022),
                        rating = 4.0,
                    ),
                    PreviewData.baseBook.copy(
                        id = 4,
                        title = "Will the Last Person To Leave the Planet Please Shut Off the Sun",
                        defaultEdition = PreviewData.baseEdition.copy(releaseYear = 2021),
                        rating = 0.0,
                    ),
                ),
            ),
        )
    }
}

@StandardPreview
@Composable
private fun LoadingActiveExploreScreenPreview() {
    SoftcoverTheme {
        ExploreScreen.Screen(
            runAction = {},
            onBookClick = {},
            isOnline = true,
            state = ExploreScreenUiState(
                searchText = "Piranesi",
                queriedBooks = emptyList(),
                isLoading = true,
            ),
        )
    }
}

@StandardPreview
@Composable
private fun NoResultsActiveExploreScreenPreview() {
    SoftcoverTheme {
        ExploreScreen.Screen(
            runAction = {},
            onBookClick = {},
            isOnline = true,
            state = ExploreScreenUiState(
                searchText = "qwertyuiop",
                queriedBooks = emptyList(),
                isLoading = false,
            ),
        )
    }
}
