package nl.rhaydus.softcover.feature.reading.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.IndicatorBox
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import nl.rhaydus.designsystem.editorial.component.PullToRefreshEyebrow
import nl.rhaydus.designsystem.theme.StandardPreview
import nl.rhaydus.softcover.core.designsystem.presentation.component.MarkAsReadBurst
import nl.rhaydus.softcover.core.designsystem.presentation.preview.PreviewData
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.designsystem.presentation.util.rememberBottomBarPadding
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookDeadline
import nl.rhaydus.softcover.core.domain.model.BookSeries
import nl.rhaydus.softcover.core.domain.model.DeadlineUnit
import nl.rhaydus.softcover.core.domain.model.ReadingDayActivity
import nl.rhaydus.softcover.feature.reading.presentation.action.ReadingAction
import nl.rhaydus.softcover.feature.reading.presentation.action.RefreshAction
import nl.rhaydus.softcover.feature.reading.presentation.component.StreakStrip
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.ui.common.currentLocalDate

private val booksListState = LazyListState()

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal actual fun ReadingScreenLayout(
    state: ReadingScreenUiState,
    runAction: (ReadingAction) -> Unit,
    onBookClick: (Book) -> Unit,
    onNavigateToSearch: () -> Unit,
) {
    val pullToRefreshState = rememberPullToRefreshState()

    val controller = rememberMarkAsReadController(
        books = state.books,
        runAction = runAction,
    )

    var showStreakSheet by remember { mutableStateOf(false) }

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
                    isRefreshing = state.isLoading,
                    onRefresh = { runAction(RefreshAction) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    state = pullToRefreshState,
                    indicator = {
                        IndicatorBox(
                            modifier = Modifier.align(Alignment.TopCenter),
                            state = pullToRefreshState,
                            isRefreshing = state.isLoading,
                        ) {
                            ContainedLoadingIndicator(modifier = Modifier.align(Alignment.TopCenter))
                        }
                    },
                ) {
                    when {
                        state.books.isNotEmpty() -> {
                            ReadingBooksColumn(
                                state = state,
                                runAction = runAction,
                                onBookClick = onBookClick,
                                controller = controller,
                                listState = booksListState,
                                contentPadding = PaddingValues(bottom = rememberBottomBarPadding()),
                            ) {
                                EditorialHeader(
                                    bookCount = state.books.size,
                                    averageProgress = state.books.averageProgress(),
                                    recentReadingActivity = state.recentReadingActivity,
                                    streakEnabled = state.streakEnabled,
                                    onExpandStreak = { showStreakSheet = true },
                                    pullToRefreshState = pullToRefreshState,
                                    isRefreshing = state.isLoading,
                                )
                            }
                        }

                        state.isLoading -> Unit
                        else -> EmptyCurrentlyReadingScreen(
                            wantToReadBooks = state.wantToReadBooks,
                            trendingBooks = state.trendingBooks,
                            streakEnabled = state.streakEnabled,
                            recentReadingActivity = state.recentReadingActivity,
                            onExpandStreak = { showStreakSheet = true },
                            onBookClick = onBookClick,
                            onNavigateToSearch = onNavigateToSearch,
                        )
                    }
                }

                ReadingOverlays(
                    state = state,
                    runAction = runAction,
                    controller = controller,
                    showStreakSheet = showStreakSheet,
                    onDismissStreakSheet = { showStreakSheet = false },
                )
            }
        }

        MarkAsReadBurst(
            triggerKey = controller.celebrationKey,
            modifier = Modifier.fillMaxSize(),
            particleCount = 28,
            durationMillis = 1000,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditorialHeader(
    bookCount: Int,
    averageProgress: Float?,
    recentReadingActivity: List<ReadingDayActivity>,
    streakEnabled: Boolean,
    onExpandStreak: () -> Unit,
    pullToRefreshState: PullToRefreshState,
    isRefreshing: Boolean,
) {
    val greeting = remember { greetingForNow() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 24.dp),
    ) {
        PullToRefreshEyebrow(
            pullToRefreshState = pullToRefreshState,
            isRefreshing = isRefreshing,
            baseText = "Now reading",
            refreshingText = "Catching up on your reading…",
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = greeting,
            style = MaterialTheme.editorialTypography.headlineMedium.copy(
                lineHeight = 32.sp,
            ),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        val subtitle = buildSubtitle(
            bookCount = bookCount,
            averageProgress = averageProgress,
        )

        Text(
            text = subtitle,
            style = MaterialTheme.editorialTypography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        if (streakEnabled && recentReadingActivity.isNotEmpty()) {
            Spacer(modifier = Modifier.height(14.dp))

            StreakStrip(
                activity = recentReadingActivity,
                onClick = onExpandStreak,
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}

private fun previewBook(
    id: Int,
    title: String,
    authorNames: List<String>,
    pages: Int,
    currentPage: Int,
    progress: Float,
    series: BookSeries? = null,
    positionsInSeries: List<Double> = emptyList(),
    audioSeconds: Int? = null,
    currentSeconds: Int? = null,
): Book {
    val editionId = id * 10
    val edition = PreviewData.baseEdition.copy(
        id = editionId,
        pages = pages,
        audioSeconds = audioSeconds,
        authors = authorNames.map { PreviewData.baseAuthor.copy(name = it) },
    )

    return PreviewData.baseBook.copy(
        id = id,
        title = title,
        editions = listOf(edition),
        defaultEdition = edition,
        authors = edition.authors,
        userBookRead = PreviewData.baseBook.userBookRead?.copy(
            currentPage = currentPage,
            currentSeconds = currentSeconds,
            progress = progress,
        ),
        userBook = PreviewData.baseBook.userBook?.copy(editionId = editionId),
        bookSeries = series,
        positionsInSeries = positionsInSeries,
    )
}

private val previewBooks: List<Book> = listOf(
    previewBook(
        id = 1,
        title = "The Dungeon Anarchist's Cookbook",
        authorNames = listOf("Matt Dinniman"),
        pages = 534,
        currentPage = 470,
        progress = 88.014984f,
        series = BookSeries(
            id = 1,
            name = "Dungeon Crawler Carl",
            amountOfBooks = 20,
        ),
    ),
    previewBook(
        id = 2,
        title = "Last to Leave the Room",
        authorNames = listOf("Caitlin Starling"),
        pages = 320,
        currentPage = 262,
        progress = 81.875f,
        series = BookSeries(
            id = 1,
            name = "Dungeon Crawler Carl",
            amountOfBooks = 20,
        ),
        positionsInSeries = listOf(3.0),
    ),
    previewBook(
        id = 3,
        title = "Cursed Bunny",
        authorNames = listOf("Bora Chung", "Anton Hur"),
        pages = 534,
        currentPage = 49,
        progress = 19.140625f,
    ),
    previewBook(
        id = 4,
        title = "Sherlock Holmes: The complete illustrated novels",
        authorNames = listOf("Arthur Conan Doyle"),
        pages = 534,
        currentPage = 200,
        progress = 40.322582f,
    ),
    previewBook(
        id = 5,
        title = "The Complete Fiction",
        authorNames = listOf("H.P. Lovecraft", "S.T. Joshi"),
        pages = 1098,
        currentPage = 110,
        progress = 10.018215f,
    ),
)

@StandardPreview
@Composable
private fun ReadingScreenEmptyPreview() {
    SoftcoverTheme {
        ReadingScreenLayout(
            state = ReadingScreenUiState(isLoading = false),
            runAction = {},
            onBookClick = {},
            onNavigateToSearch = {},
        )
    }
}

@StandardPreview
@Composable
private fun ReadingScreenEmptyWithPickUpNextPreview() {
    SoftcoverTheme {
        ReadingScreenLayout(
            state = ReadingScreenUiState(
                isLoading = false,
                wantToReadBooks = previewBooks.take(3),
            ),
            runAction = {},
            onBookClick = {},
            onNavigateToSearch = {},
        )
    }
}

@StandardPreview
@Composable
private fun ReadingScreenEmptyWithTrendingPreview() {
    SoftcoverTheme {
        ReadingScreenLayout(
            state = ReadingScreenUiState(
                isLoading = false,
                trendingBooks = listOf(previewBooks.first()),
            ),
            runAction = {},
            onBookClick = {},
            onNavigateToSearch = {},
        )
    }
}

@StandardPreview
@Composable
private fun ReadingScreenLoadingPreview() {
    SoftcoverTheme {
        ReadingScreenLayout(
            state = ReadingScreenUiState(isLoading = true),
            runAction = {},
            onBookClick = {},
            onNavigateToSearch = {},
        )
    }
}

@StandardPreview
@Composable
private fun ReadingScreenSingleBookPreview() {
    SoftcoverTheme {
        ReadingScreenLayout(
            state = ReadingScreenUiState(
                books = previewBooks.take(1),
                isLoading = false,
            ),
            runAction = {},
            onBookClick = {},
            onNavigateToSearch = {},
        )
    }
}

@StandardPreview
@Composable
private fun ReadingScreenPreview() {
    SoftcoverTheme {
        ReadingScreenLayout(
            state = ReadingScreenUiState(
                books = previewBooks,
                isLoading = false,
            ),
            runAction = {},
            onBookClick = {},
            onNavigateToSearch = {},
        )
    }
}

private fun previewReadingActivity(): List<ReadingDayActivity> {
    val today = currentLocalDate()
    val firstDay = today.minus(
        20,
        DateTimeUnit.DAY,
    )
    val litOffsets = setOf(0, 1, 2, 3, 5, 6, 7, 8, 9, 12, 13, 16, 17, 18, 19, 20)

    return (0..20).map { offset ->
        ReadingDayActivity(
            date = firstDay.plus(
                offset,
                DateTimeUnit.DAY,
            ),
            didRead = offset in litOffsets,
        )
    }
}

@StandardPreview
@Composable
private fun ReadingScreenWithStreakStripPreview() {
    SoftcoverTheme {
        ReadingScreenLayout(
            state = ReadingScreenUiState(
                books = previewBooks,
                isLoading = false,
                recentReadingActivity = previewReadingActivity(),
            ),
            runAction = {},
            onBookClick = {},
            onNavigateToSearch = {},
        )
    }
}

@StandardPreview
@Composable
private fun ReadingScreenWithDeadlineAndNudgePreview() {
    val today = currentLocalDate()
    val featured = previewBooks.first()
    val behindBook = previewBooks[2]

    val deadlines = mapOf(
        featured.id to BookDeadline(
            bookId = featured.id,
            deadlineDate = today.plus(
                5,
                DateTimeUnit.DAY,
            ),
            setAt = today.minus(
                10,
                DateTimeUnit.DAY,
            ),
            initialPerDay = 30f,
            unit = DeadlineUnit.PAGES,
        ),
        behindBook.id to BookDeadline(
            bookId = behindBook.id,
            deadlineDate = today.plus(
                3,
                DateTimeUnit.DAY,
            ),
            setAt = today.minus(
                20,
                DateTimeUnit.DAY,
            ),
            initialPerDay = 15f,
            unit = DeadlineUnit.PAGES,
        ),
    )

    SoftcoverTheme {
        ReadingScreenLayout(
            state = ReadingScreenUiState(
                books = previewBooks,
                isLoading = false,
                deadlines = deadlines,
            ),
            runAction = {},
            onBookClick = {},
            onNavigateToSearch = {},
        )
    }
}

@StandardPreview
@Composable
private fun ReadingScreenExpiredDeadlinePreview() {
    val today = currentLocalDate()
    val featured = previewBooks.first()

    val deadlines = mapOf(
        featured.id to BookDeadline(
            bookId = featured.id,
            deadlineDate = today.minus(
                2,
                DateTimeUnit.DAY,
            ),
            setAt = today.minus(
                30,
                DateTimeUnit.DAY,
            ),
            initialPerDay = 20f,
            unit = DeadlineUnit.PAGES,
        ),
    )

    SoftcoverTheme {
        ReadingScreenLayout(
            state = ReadingScreenUiState(
                books = previewBooks.take(3),
                isLoading = false,
                deadlines = deadlines,
            ),
            runAction = {},
            onBookClick = {},
            onNavigateToSearch = {},
        )
    }
}

@StandardPreview
@Composable
private fun ReadingScreenMutationFailedPreview() {
    SoftcoverTheme {
        ReadingScreenLayout(
            state = ReadingScreenUiState(
                books = previewBooks,
                isLoading = false,
                failedMutationBookIds = setOf(previewBooks[0].id, previewBooks[2].id),
            ),
            runAction = {},
            onBookClick = {},
            onNavigateToSearch = {},
        )
    }
}

@StandardPreview
@Composable
private fun ReadingScreenAudiobookPreview() {
    val audioBook = previewBook(
        id = 6,
        title = "Project Hail Mary",
        authorNames = listOf("Andy Weir"),
        pages = 0,
        currentPage = 0,
        progress = 42.5f,
        audioSeconds = 58_000,
        currentSeconds = 24_650,
    ).let {
        val edition = it.editions.first().copy(format = "Audiobook")
        it.copy(
            editions = listOf(edition),
            defaultEdition = edition,
        )
    }

    SoftcoverTheme {
        ReadingScreenLayout(
            state = ReadingScreenUiState(
                books = listOf(audioBook) + previewBooks.take(2),
                isLoading = false,
            ),
            runAction = {},
            onBookClick = {},
            onNavigateToSearch = {},
        )
    }
}

@StandardPreview
@Composable
private fun PlanTodayNudgePreview() {
    SoftcoverTheme {
        PlanTodayNudge(
            text = "Read 24 pages today to stay on pace.",
            onDismiss = {},
        )
    }
}
