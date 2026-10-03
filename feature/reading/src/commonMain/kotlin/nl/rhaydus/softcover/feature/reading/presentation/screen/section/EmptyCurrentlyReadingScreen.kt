package nl.rhaydus.softcover.feature.reading.presentation.screen.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.designsystem.layout.rememberBottomBarPadding
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.softcover.core.component.bookcard.BookCardEvent
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.state.EmptyState
import nl.rhaydus.softcover.core.component.state.EmptyStateButtonUiModel
import nl.rhaydus.softcover.core.component.state.EmptyStateEvent
import nl.rhaydus.softcover.core.component.state.EmptyStateUiModel
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.modifier.quoteGlyphSway
import nl.rhaydus.softcover.core.designsystem.presentation.theme.LocalDarkTheme
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.ReadingDayActivity
import nl.rhaydus.softcover.core.presentation.prefetch.rememberPrefetchingBookCardEventHandler
import nl.rhaydus.softcover.feature.reading.presentation.component.StreakStrip

@Composable
internal fun EmptyCurrentlyReadingScreen(
    pickUpNextHeader: SectionHeaderUiModel,
    trendingHeader: SectionHeaderUiModel,
    emptyState: EmptyStateUiModel,
    wantToReadBooks: List<Book> = emptyList(),
    trendingBooks: List<Book> = emptyList(),
    pickUpNextCards: ImmutableList<BookCardUiModel>? = persistentListOf(),
    trendingTileCover: CoverUiModel? = null,
    streakEnabled: Boolean = false,
    recentReadingActivity: List<ReadingDayActivity> = emptyList(),
    onExpandStreak: () -> Unit = {},
    onBookClick: (Book) -> Unit = {},
    onNavigateToSearch: () -> Unit,
) {
    // Mirrors CoverModelsCollector's own trendingBooks.firstOrNull(): the tile's cover is mapped there.
    val trendingTile = trendingBooks.firstOrNull()
    val showAdaptive = pickUpNextCards == null || pickUpNextCards.isNotEmpty() || trendingTile != null
    val booksByKey = remember(wantToReadBooks) {
        wantToReadBooks.associateBy { BookCardKey.Book(it.id) }
    }

    val onPickUpNextEvent = rememberPrefetchingBookCardEventHandler(
        resolve = booksByKey::get,
    ) { event, book ->
        if (event is BookCardEvent.Clicked) onBookClick(book)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp)
            .verticalScroll(rememberScrollState())
            .padding(bottom = rememberBottomBarPadding()),
        verticalArrangement = if (showAdaptive) Arrangement.Top else Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (showAdaptive) Spacer(modifier = Modifier.height(40.dp))
        EmptyState(
            model = emptyState,
            onEvent = { event ->
                when (event) {
                    EmptyStateEvent.ButtonClicked -> onNavigateToSearch()
                }
            },
        )

        // When the streak is enabled we show the full 21-day grid even with zero days
        // read (all-unlit), inviting a brand-new reader to start a streak; isNotEmpty()
        // only suppresses the brief pre-load window before activity data arrives.
        if (streakEnabled && recentReadingActivity.isNotEmpty()) {
            Spacer(modifier = Modifier.height(32.dp))

            StreakStrip(
                activity = recentReadingActivity,
                onClick = onExpandStreak,
            )
        }

        if (pickUpNextCards == null || pickUpNextCards.isNotEmpty()) {
            Spacer(modifier = Modifier.height(40.dp))

            PickUpNextSection(
                cards = pickUpNextCards,
                header = pickUpNextHeader,
                onEvent = onPickUpNextEvent,
            )
        } else if (trendingTile != null) {
            Spacer(modifier = Modifier.height(40.dp))

            TrendingTileSection(
                book = trendingTile,
                cover = trendingTileCover,
                header = trendingHeader,
                onBookClick = onBookClick,
            )
        }
    }
}
