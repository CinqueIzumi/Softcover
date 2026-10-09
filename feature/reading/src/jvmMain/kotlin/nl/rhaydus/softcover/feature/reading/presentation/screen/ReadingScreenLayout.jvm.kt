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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import nl.rhaydus.designsystem.component.DesktopVerticalScrollbar
import nl.rhaydus.designsystem.layout.rememberBottomBarPadding
import nl.rhaydus.designsystem.util.SkeletonCrossfade
import nl.rhaydus.softcover.core.component.bookcard.BookCard
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.bookcard.bookCardStaggeredEntry
import nl.rhaydus.softcover.core.component.bookcard.rememberBookCardEntryCoordinator
import nl.rhaydus.softcover.core.component.celebration.MarkAsReadBurst
import nl.rhaydus.softcover.core.component.celebration.MarkAsReadBurstScale
import nl.rhaydus.softcover.core.component.header.MastheadRefreshAction
import nl.rhaydus.softcover.core.component.header.MastheadRefreshEvent
import nl.rhaydus.softcover.core.component.header.PageMasthead
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.reading.presentation.action.ReadingAction
import nl.rhaydus.softcover.feature.reading.presentation.action.RefreshAction
import nl.rhaydus.softcover.feature.reading.presentation.component.StreakStrip
import nl.rhaydus.softcover.feature.reading.presentation.mapper.ALSO_READING_STAND_IN_CARDS
import nl.rhaydus.softcover.feature.reading.presentation.screen.section.AlsoReadingHeader
import nl.rhaydus.softcover.feature.reading.presentation.screen.section.EmptyCurrentlyReadingScreen
import nl.rhaydus.softcover.feature.reading.presentation.screen.section.ReadingFeaturedSection
import nl.rhaydus.softcover.feature.reading.presentation.screen.section.ReadingOverlays
import nl.rhaydus.softcover.feature.reading.presentation.screen.section.rememberReadingCardEventHandler
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState

private val readingScrollState = ScrollState(initial = 0)

/**
 * Desktop Reading: a static editorial header (greeting, subtitle, streak strip, explicit Refresh
 * control with hover/cursor affordances) above the scrolling currently-reading column, which carries
 * a persistent desktop scrollbar. No status-bar insets, no pager, and no pull-to-refresh.
 *
 * The body is a non-lazy [verticalScroll] column rather than the lazy list the mobile layout uses:
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
                    state.books.isNotEmpty() || state.isLoading -> {
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

                    else -> EmptyCurrentlyReadingScreen(
                        trendingBooks = state.trendingBooks,
                        pickUpNext = state.pickUpNext,
                        trendingTileCover = state.trendingTileCover,
                        pickUpNextHeader = state.pickUpNextHeader,
                        trendingHeader = state.trendingHeader,
                        emptyState = state.emptyCurrentlyReading,
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
            scale = MarkAsReadBurstScale.Screen,
        )
    }
}

@Composable
private fun DesktopReadingContent(
    state: ReadingScreenUiState,
    runAction: (ReadingAction) -> Unit,
    onBookClick: (Book) -> Unit,
    controller: MarkAsReadController,
    modifier: Modifier = Modifier,
) {
    val onCardEvent = rememberReadingCardEventHandler(
        alsoReading = state.visibleAlsoReading,
        runAction = runAction,
        onBookClick = onBookClick,
    )

    val isShowingStandIns = state.visibleAlsoReading.cards.any(BookCardUiModel::isLoading)

    val entry = rememberBookCardEntryCoordinator(
        cards = state.visibleAlsoReading.cards,
        key = "reading:rest",
    )

    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(readingScrollState)
                .padding(top = Spacing.xs, bottom = Spacing.xl + rememberBottomBarPadding()),
        ) {
            ReadingFeaturedSection(
                state = state,
                runAction = runAction,
                onBookClick = onBookClick,
                controller = controller,
            )

            if (state.visibleAlsoReading.isEmpty.not()) {
                AlsoReadingHeader(model = state.visibleAlsoReadingHeader)

                SkeletonCrossfade(isLoading = isShowingStandIns) { showStandIns ->
                    Column {
                        val cards = if (showStandIns) ALSO_READING_STAND_IN_CARDS else state.visibleAlsoReading.cards

                        cards.forEachIndexed { index, card ->
                            val bookId = (card.key as? BookCardKey.Book)?.bookId ?: return@forEachIndexed

                            BookCard(
                                model = card,
                                onEvent = onCardEvent,
                                modifier = Modifier
                                    .bookCardStaggeredEntry(
                                        card = card,
                                        coordinator = entry,
                                        index = index,
                                    )
                                    .then(controller.slideModifier(bookId)),
                            )
                        }
                    }
                }
            }
        }

        DesktopVerticalScrollbar(
            scrollState = readingScrollState,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .padding(vertical = Spacing.xxs),
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
            .padding(start = Spacing.gutter, end = Spacing.sm, top = Spacing.md),
    ) {
        PageMasthead(model = state.visibleMasthead) {
            MastheadRefreshAction(
                model = state.mastheadRefresh,
                onEvent = {
                    when (it) {
                        MastheadRefreshEvent.Refresh -> runAction(RefreshAction)
                    }
                },
            )
        }

        if (state.streakEnabled && (state.isLoading || state.recentReadingActivity.isNotEmpty())) {
            Spacer(modifier = Modifier.height(Spacing.md))

            StreakStrip(
                activity = state.recentReadingActivity,
                onClick = onExpandStreak,
                isLoading = state.isLoading,
            )
        }

        Spacer(modifier = Modifier.height(Spacing.xxs))
    }
}
