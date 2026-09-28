package nl.rhaydus.softcover.feature.reading.presentation.screen

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import nl.rhaydus.common.currentLocalDate
import nl.rhaydus.designsystem.component.DesktopVerticalScrollbar
import nl.rhaydus.designsystem.layout.rememberBottomBarPadding
import nl.rhaydus.softcover.core.component.celebration.MarkAsReadBurst
import nl.rhaydus.softcover.core.component.celebration.MarkAsReadBurstUiModel
import nl.rhaydus.softcover.core.component.header.MastheadRefreshAction
import nl.rhaydus.softcover.core.component.header.MastheadRefreshEvent
import nl.rhaydus.softcover.core.component.header.PageMasthead
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.presentation.prefetch.LocalBookDetailPrefetcher
import nl.rhaydus.softcover.core.presentation.prefetch.rememberBookDetailPrefetcher
import nl.rhaydus.softcover.feature.reading.presentation.action.OnDismissPlanTodayAction
import nl.rhaydus.softcover.feature.reading.presentation.action.ReadingAction
import nl.rhaydus.softcover.feature.reading.presentation.action.RefreshAction
import nl.rhaydus.softcover.feature.reading.presentation.component.StreakStrip
import nl.rhaydus.softcover.feature.reading.presentation.screen.section.CompactBookEntry
import nl.rhaydus.softcover.feature.reading.presentation.screen.section.EmptyCurrentlyReadingScreen
import nl.rhaydus.softcover.feature.reading.presentation.screen.section.FeaturedBookCard
import nl.rhaydus.softcover.feature.reading.presentation.screen.section.ReadingBooksColumn
import nl.rhaydus.softcover.feature.reading.presentation.screen.section.ReadingOverlays
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState

/** The reading screen celebrates at full-screen scale, so its burst is bigger and slower. */
private const val READING_BURST_PARTICLES = 28
private const val READING_BURST_DURATION_MS = 1000

private val readingScrollState = ScrollState(initial = 0)

/**
 * Desktop Reading: a static editorial header (greeting, subtitle, streak strip, explicit Refresh
 * control with hover/cursor affordances) above the scrolling currently-reading column, which carries
 * a persistent desktop scrollbar. No status-bar insets, no pager, and no pull-to-refresh — the
 * featured card, "also reading" list, progress sheet, and mark-as-read celebration all reuse the
 * shared shelf pieces ([FeaturedBookCard], [CompactBookEntry], [ReadingOverlays],
 * [rememberMarkAsReadController]).
 *
 * The body is a non-lazy [verticalScroll] column rather than the shared lazy [ReadingBooksColumn]:
 * the list is short and its items vary wildly in height (the tall featured card above short rows), so
 * a fully-measured [ScrollState] gives the scrollbar an exact extent and a stable thumb where a lazy
 * adapter's estimate would make it jump as the featured card scrolls out of view.
 */
@Composable
internal actual fun ReadingScreenLayout(
    state: ReadingScreenUiState,
    runAction: (ReadingAction) -> Unit,
    onBookClick: (Book) -> Unit,
    onNavigateToSearch: () -> Unit,
) {
    val controller = rememberMarkAsReadController(
        books = state.books,
        runAction = runAction,
    )

    var showStreakSheet by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                when {
                    state.books.isNotEmpty() -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            DesktopReadingHeader(
                                state = state,
                                runAction = runAction,
                                onExpandStreak = { showStreakSheet = true },
                            )

                            DesktopReadingContent(
                                state = state,
                                runAction = runAction,
                                onBookClick = onBookClick,
                                controller = controller,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                            )
                        }
                    }

                    state.isLoading -> Unit
                    else -> EmptyCurrentlyReadingScreen(
                        wantToReadBooks = state.wantToReadBooks,
                        trendingBooks = state.trendingBooks,
                        pickUpNextCovers = state.pickUpNextCovers,
                        trendingTileCover = state.trendingTileCover,
                        pickUpNextHeader = state.pickUpNextHeader,
                        trendingHeader = state.trendingHeader,
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

        MarkAsReadBurst(
            model = remember(controller.celebrationKey) {
                MarkAsReadBurstUiModel(
                    triggerKey = controller.celebrationKey,
                    particleCount = READING_BURST_PARTICLES,
                    durationMillis = READING_BURST_DURATION_MS,
                )
            },
            modifier = Modifier.fillMaxSize(),
        )
    }
}

/**
 * The desktop currently-reading column: the featured card (which fuses its own optional plan-today
 * nudge to its top edge) and the "also reading" rows, in a non-lazy [verticalScroll] so the
 * [DesktopVerticalScrollbar] thumb tracks the true content height. Mirrors the item order of the
 * shared [ReadingBooksColumn] (which the mobile layout uses lazily), reusing the same shared cards.
 */
@Composable
private fun DesktopReadingContent(
    state: ReadingScreenUiState,
    runAction: (ReadingAction) -> Unit,
    onBookClick: (Book) -> Unit,
    controller: MarkAsReadController,
    modifier: Modifier = Modifier,
) {
    val featured = state.books.first()
    val rest = state.books.drop(1)

    val today = remember { currentLocalDate().toString() }

    val isInspection = LocalInspectionMode.current
    val prefetcher = if (isInspection) null else rememberBookDetailPrefetcher()

    val featuredDeadlineProgress = state.deadlineProgressByBook[featured.id]
    val planTodayMessage = planTodayNudgeFor(progress = featuredDeadlineProgress)
    val isPlanTodayDismissed = state.dismissedPlanTodayByBook[featured.id] == today

    Box(modifier = modifier) {
        CompositionLocalProvider(LocalBookDetailPrefetcher provides prefetcher) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(readingScrollState)
                    .padding(top = 8.dp, bottom = 24.dp + rememberBottomBarPadding()),
            ) {
                FeaturedBookCard(
                    book = featured,
                    backdropCover = state.featuredBackdropCover,
                    heroCover = state.featuredCover,
                    deadlineCoverOverlay = state.deadlineCoverOverlays[featured.id],
                    deadlineSummary = state.featuredDeadlineSummary,
                    mutationFailed = featured.id in state.failedMutationBookIds,
                    paceForecast = state.featuredBookPace,
                    planTodayMessage = planTodayMessage.takeIf { isPlanTodayDismissed.not() },
                    onDismissPlanToday = {
                        runAction(OnDismissPlanTodayAction(bookId = featured.id))
                    },
                    runAction = runAction,
                    onBookClick = onBookClick,
                    modifier = controller.slideModifier(featured.id),
                )

                if (rest.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(32.dp))

                    Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                        SectionHeader(model = state.alsoReadingHeader)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    rest.forEach { book ->
                        CompactBookEntry(
                            modifier = controller.slideModifier(book.id),
                            book = book,
                            cover = state.bookCovers[book.id],
                            deadlineCoverOverlay = state.deadlineCoverOverlays[book.id],
                            deadlineSummary = state.deadlineSummaries[book.id],
                            mutationFailed = book.id in state.failedMutationBookIds,
                            runAction = runAction,
                            onBookClick = onBookClick,
                        )
                    }
                }
            }
        }

        DesktopVerticalScrollbar(
            scrollState = readingScrollState,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .padding(vertical = 4.dp),
        )
    }
}

@Composable
private fun DesktopReadingHeader(
    state: ReadingScreenUiState,
    runAction: (ReadingAction) -> Unit,
    onExpandStreak: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 12.dp, top = 16.dp),
    ) {
        PageMasthead(model = state.masthead) {
            MastheadRefreshAction(
                model = state.mastheadRefresh,
                onEvent = {
                    when (it) {
                        MastheadRefreshEvent.Refresh -> runAction(RefreshAction)
                    }
                },
            )
        }

        if (state.streakEnabled && state.recentReadingActivity.isNotEmpty()) {
            Spacer(modifier = Modifier.height(14.dp))

            StreakStrip(
                activity = state.recentReadingActivity,
                onClick = onExpandStreak,
            )
        }

        Spacer(modifier = Modifier.height(4.dp))
    }
}
