package nl.rhaydus.softcover.feature.reading.presentation.screen.section

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.editorial.component.PullToRefreshEyebrow
import nl.rhaydus.softcover.core.component.header.PageMasthead
import nl.rhaydus.softcover.feature.reading.presentation.component.StreakStrip
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState

/**
 * The mobile currently-reading header: a [PullToRefreshEyebrow] tracking pull progress, then
 * [state]'s compact [PageMasthead] (title and subtitle only — the eyebrow is the pull indicator
 * above it), then the streak strip.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReadingMasthead(
    state: ReadingScreenUiState,
    onExpandStreak: () -> Unit,
    pullToRefreshState: PullToRefreshState,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 24.dp),
    ) {
        PullToRefreshEyebrow(
            pullToRefreshState = pullToRefreshState,
            isRefreshing = state.isLoading,
            baseText = "Now reading",
            refreshingText = "Catching up on your reading…",
        )

        Spacer(modifier = Modifier.height(4.dp))

        PageMasthead(model = state.compactMasthead)

        if (state.streakEnabled && state.recentReadingActivity.isNotEmpty()) {
            Spacer(modifier = Modifier.height(14.dp))

            StreakStrip(
                activity = state.recentReadingActivity,
                onClick = onExpandStreak,
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}
