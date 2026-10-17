package nl.rhaydus.softcover.feature.reading.presentation.screen.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.designsystem.layout.rememberBottomBarPadding
import nl.rhaydus.softcover.core.component.bookcard.BookCardEvent
import nl.rhaydus.softcover.core.component.bookcard.BookCardSet
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.state.EmptyState
import nl.rhaydus.softcover.core.component.state.EmptyStateEvent
import nl.rhaydus.softcover.core.component.state.EmptyStateUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.presentation.prefetch.rememberPrefetchingBookCardEventHandler
import nl.rhaydus.softcover.feature.reading.presentation.action.ReadingAction
import nl.rhaydus.softcover.feature.reading.presentation.component.StreakStrip
import nl.rhaydus.softcover.feature.reading.presentation.component.StreakStripEvent
import nl.rhaydus.softcover.feature.reading.presentation.state.StreakStripUiModel
import nl.rhaydus.softcover.feature.reading.presentation.state.TrendingTileUiModel

@Composable
internal fun EmptyCurrentlyReadingScreen(
    pickUpNextHeader: SectionHeaderUiModel,
    trendingHeader: SectionHeaderUiModel,
    emptyState: EmptyStateUiModel,
    onHold: BookCardSet<Book>,
    onHoldHeader: SectionHeaderUiModel,
    runAction: (ReadingAction) -> Unit,
    trendingBooks: ImmutableList<Book> = persistentListOf(),
    pickUpNext: BookCardSet<Book> = BookCardSet.empty(),
    trendingTile: TrendingTileUiModel? = null,
    streakEnabled: Boolean = false,
    streakStrip: StreakStripUiModel,
    onExpandStreak: () -> Unit = {},
    onBookClick: (Book) -> Unit = {},
    onNavigateToSearch: () -> Unit,
) {
    // Mirrors CoverModelsCollector's own trendingBooks.firstOrNull(): the tile model is built there.
    val trendingBook = trendingBooks.firstOrNull()
    val showAdaptive = pickUpNext.isEmpty.not() || trendingTile != null
    val onPickUpNextEvent = rememberPrefetchingBookCardEventHandler(
        set = pickUpNext,
    ) { event, book ->
        if (event is BookCardEvent.Clicked) onBookClick(book)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = rememberBottomBarPadding()),
        verticalArrangement = if (showAdaptive) Arrangement.Top else Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        EmptyState(
            model = emptyState,
            modifier = if (showAdaptive) {
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.gutter)
                    .padding(top = Spacing.section)
            } else {
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = Spacing.gutter, vertical = Spacing.xl)
            },
            onEvent = { event ->
                when (event) {
                    EmptyStateEvent.ButtonClicked -> onNavigateToSearch()
                }
            },
        )

        // When the streak is enabled we show the full 21-day grid even with zero days
        // read (all-unlit), inviting a brand-new reader to start a streak; isNotEmpty()
        // only suppresses the brief pre-load window before activity data arrives.
        if (streakEnabled && streakStrip.days.isNotEmpty()) {
            Spacer(modifier = Modifier.height(Spacing.section))

            StreakStrip(
                model = streakStrip,
                onEvent = { event ->
                    when (event) {
                        StreakStripEvent.Clicked -> onExpandStreak()
                    }
                },
                modifier = Modifier.padding(horizontal = Spacing.gutter),
            )
        }

        if (onHold.isEmpty.not()) {
            Spacer(modifier = Modifier.height(Spacing.section))

            OnHoldSection(
                header = onHoldHeader,
                onHold = onHold,
                runAction = runAction,
                onBookClick = onBookClick,
            )
        }

        if (pickUpNext.isEmpty.not()) {
            Spacer(modifier = Modifier.height(Spacing.section))

            Box(modifier = Modifier.padding(horizontal = Spacing.gutter)) {
                PickUpNextSection(
                    cards = pickUpNext.cards,
                    header = pickUpNextHeader,
                    onEvent = onPickUpNextEvent,
                )
            }
        } else if (trendingTile != null) {
            Spacer(modifier = Modifier.height(Spacing.section))

            Box(modifier = Modifier.padding(horizontal = Spacing.gutter)) {
                TrendingTileSection(
                    model = trendingTile,
                    header = trendingHeader,
                    onClick = {
                        trendingBook?.let(onBookClick)
                    },
                )
            }
        }
    }
}
