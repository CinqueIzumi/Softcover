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
import nl.rhaydus.designsystem.editorial.component.PullToRefreshEyebrow
import nl.rhaydus.softcover.core.component.header.PageMasthead
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
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
            .padding(start = Spacing.xl, end = Spacing.xl, top = Spacing.xl),
    ) {
        PullToRefreshEyebrow(
            pullToRefreshState = pullToRefreshState,
            isRefreshing = state.isRefreshing,
            baseText = "Now reading",
            refreshingText = "Catching up on your reading…",
        )

        Spacer(modifier = Modifier.height(Spacing.xxs))

        PageMasthead(model = state.visibleCompactMasthead)

        if (state.streakEnabled && (state.isLoading || state.recentReadingActivity.isNotEmpty())) {
            Spacer(modifier = Modifier.height(Spacing.md))

            StreakStrip(
                activity = state.recentReadingActivity,
                onClick = onExpandStreak,
                isLoading = state.isLoading,
            )
        }

        Spacer(modifier = Modifier.height(Spacing.sm))
    }
}
