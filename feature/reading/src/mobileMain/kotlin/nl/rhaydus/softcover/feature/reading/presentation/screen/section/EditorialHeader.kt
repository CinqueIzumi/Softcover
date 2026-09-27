package nl.rhaydus.softcover.feature.reading.presentation.screen.section

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.rhaydus.designsystem.editorial.component.PullToRefreshEyebrow
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.domain.model.ReadingDayActivity
import nl.rhaydus.softcover.feature.reading.presentation.component.StreakStrip
import nl.rhaydus.softcover.feature.reading.presentation.screen.buildSubtitle
import nl.rhaydus.softcover.feature.reading.presentation.screen.greetingForNow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EditorialHeader(
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
