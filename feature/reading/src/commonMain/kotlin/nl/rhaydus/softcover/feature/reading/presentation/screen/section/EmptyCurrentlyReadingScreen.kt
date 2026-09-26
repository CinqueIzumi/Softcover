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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.rhaydus.designsystem.layout.rememberBottomBarPadding
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.modifier.quoteGlyphSway
import nl.rhaydus.softcover.core.designsystem.presentation.theme.LocalDarkTheme
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.ReadingDayActivity
import nl.rhaydus.softcover.feature.reading.presentation.component.StreakStrip

@Composable
internal fun EmptyCurrentlyReadingScreen(
    wantToReadBooks: List<Book> = emptyList(),
    trendingBooks: List<Book> = emptyList(),
    pickUpNextCovers: Map<Int, CoverUiModel> = emptyMap(),
    trendingTileCover: CoverUiModel? = null,
    streakEnabled: Boolean = false,
    recentReadingActivity: List<ReadingDayActivity> = emptyList(),
    onExpandStreak: () -> Unit = {},
    onBookClick: (Book) -> Unit = {},
    onNavigateToSearch: () -> Unit,
) {
    // Mirrors CoverModelsCollector's own wantToReadBooks.take(3) / firstOrNull() — kept in step
    // here, not derived from the cover maps, so a change to this screen's tile count must update
    // both places.
    val pickUpNext = wantToReadBooks.take(3)
    val trendingTile = trendingBooks.firstOrNull()
    val showAdaptive = pickUpNext.isNotEmpty() || trendingTile != null

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
        val quoteAlpha = if (LocalDarkTheme.current) 0.15f else 0.3f

        Text(
            text = "“",
            style = MaterialTheme.editorialTypography.quoteGlyph.copy(
                fontSize = 140.sp,
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = quoteAlpha),
            modifier = Modifier
                .padding(top = 8.dp)
                .quoteGlyphSway(),
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "An open page awaits",
            style = MaterialTheme.editorialTypography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Nothing is currently between your fingers. Find a title worth losing an evening to.",
            style = MaterialTheme.editorialTypography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Surface(
            onClick = onNavigateToSearch,
            color = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = RoundedCornerShape(percent = 50),
            modifier = Modifier.pointerHandCursor(),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val searchIcon = drawableIconResource(
                    icon = SoftcoverIcon.Search,
                    contentDescription = "",
                )

                Icon(
                    painter = searchIcon.getIconPainter(),
                    contentDescription = searchIcon.contentDescription,
                    modifier = Modifier.size(18.dp),
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Find a book",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                )
            }
        }

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

        if (pickUpNext.isNotEmpty()) {
            Spacer(modifier = Modifier.height(40.dp))

            PickUpNextSection(
                books = pickUpNext,
                covers = pickUpNextCovers,
                onBookClick = onBookClick,
            )
        } else if (trendingTile != null) {
            Spacer(modifier = Modifier.height(40.dp))

            TrendingTileSection(
                book = trendingTile,
                cover = trendingTileCover,
                onBookClick = onBookClick,
            )
        }
    }
}
