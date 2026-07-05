package nl.rhaydus.softcover.feature.reading.presentation.screen

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import nl.rhaydus.common.currentLocalDate
import nl.rhaydus.designsystem.component.DesktopVerticalScrollbar
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.softcover.core.designsystem.presentation.component.MarkAsReadBurst
import nl.rhaydus.softcover.core.designsystem.presentation.prefetch.LocalBookDetailPrefetcher
import nl.rhaydus.softcover.core.designsystem.presentation.prefetch.rememberBookDetailPrefetcher
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.ReadingDayActivity
import nl.rhaydus.softcover.feature.reading.presentation.action.OnDismissPlanTodayAction
import nl.rhaydus.softcover.feature.reading.presentation.action.ReadingAction
import nl.rhaydus.softcover.feature.reading.presentation.action.RefreshAction
import nl.rhaydus.softcover.feature.reading.presentation.component.StreakStrip
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState

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
                                bookCount = state.books.size,
                                averageProgress = state.books.averageProgress(),
                                recentReadingActivity = state.recentReadingActivity,
                                streakEnabled = state.streakEnabled,
                                isRefreshing = state.isLoading,
                                onExpandStreak = { showStreakSheet = true },
                                onRefreshClick = { runAction(RefreshAction) },
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
            triggerKey = controller.celebrationKey,
            modifier = Modifier.fillMaxSize(),
            particleCount = 28,
            durationMillis = 1000,
        )
    }
}

/**
 * The desktop currently-reading column: the featured card, the optional plan-today nudge, and the
 * "also reading" rows, in a non-lazy [verticalScroll] so the [DesktopVerticalScrollbar] thumb tracks
 * the true content height. Mirrors the item order of the shared [ReadingBooksColumn] (which the mobile
 * layout uses lazily), reusing the same shared cards.
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

    val featuredDeadlineProgress = featured.deadlineProgressFrom(state)
    val planTodayMessage = planTodayNudgeFor(progress = featuredDeadlineProgress)
    val isPlanTodayDismissed = state.dismissedPlanTodayByBook[featured.id] == today

    Box(modifier = modifier) {
        CompositionLocalProvider(LocalBookDetailPrefetcher provides prefetcher) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(readingScrollState)
                    .padding(top = 8.dp, bottom = 24.dp),
            ) {
                if (planTodayMessage != null && isPlanTodayDismissed.not()) {
                    PlanTodayNudge(
                        text = planTodayMessage,
                        onDismiss = {
                            runAction(OnDismissPlanTodayAction(bookId = featured.id))
                        },
                    )
                }

                FeaturedBookCard(
                    book = featured,
                    deadlineProgress = featuredDeadlineProgress,
                    dateStyle = state.dateStyle,
                    mutationFailed = featured.id in state.failedMutationBookIds,
                    runAction = runAction,
                    onBookClick = onBookClick,
                    onMarkAsRead = controller::requestMarkAsRead,
                    modifier = controller.slideModifier(featured.id),
                )

                if (rest.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                        SectionLabel(text = "Also between your fingers")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    rest.forEach { book ->
                        CompactBookEntry(
                            modifier = controller.slideModifier(book.id),
                            book = book,
                            deadlineProgress = book.deadlineProgressFrom(state),
                            dateStyle = state.dateStyle,
                            mutationFailed = book.id in state.failedMutationBookIds,
                            runAction = runAction,
                            onBookClick = onBookClick,
                            onMarkAsRead = controller::requestMarkAsRead,
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
    bookCount: Int,
    averageProgress: Float?,
    recentReadingActivity: List<ReadingDayActivity>,
    streakEnabled: Boolean,
    isRefreshing: Boolean,
    onExpandStreak: () -> Unit,
    onRefreshClick: () -> Unit,
) {
    val greeting = remember { greetingForNow() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 12.dp, top = 16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isRefreshing) "Catching up on your reading…" else "Now reading",
                    style = MaterialTheme.editorialTypography.eyebrow,
                    color = MaterialTheme.colorScheme.primary,
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = greeting,
                    style = MaterialTheme.editorialTypography.pageTitle,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(end = 16.dp),
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = buildSubtitle(
                        bookCount = bookCount,
                        averageProgress = averageProgress,
                    ),
                    style = MaterialTheme.editorialTypography.body,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 16.dp),
                )
            }

            TextButton(
                onClick = onRefreshClick,
                enabled = isRefreshing.not(),
                modifier = Modifier.pointerHandCursor(),
            ) {
                Text(text = if (isRefreshing) "Refreshing…" else "Refresh")
            }
        }

        if (streakEnabled && recentReadingActivity.isNotEmpty()) {
            Spacer(modifier = Modifier.height(14.dp))

            StreakStrip(
                activity = recentReadingActivity,
                onClick = onExpandStreak,
            )
        }

        Spacer(modifier = Modifier.height(4.dp))
    }
}
