package nl.rhaydus.softcover.feature.explore.presentation.screen.section

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.modifier.shimmer

/**
 * Mirrors `EditorialSectionHeader`'s own anatomy (accent bar, eyebrow row, headline) as shimmer
 * bars. Only a section whose headline text itself depends on data not yet resolved - Because-you-
 * read's genre - needs this: every other section header here is a static string, always known.
 * Reused by both the mobile and desktop Because-you-read layouts.
 */
@Composable
internal fun EditorialSectionHeaderSkeleton(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .height(4.dp)
                    .width(32.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .shimmer(isLoading = true),
            )

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .height(12.dp)
                    .width(100.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmer(isLoading = true),
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .height(22.dp)
                .fillMaxWidth(0.5f)
                .clip(RoundedCornerShape(4.dp))
                .shimmer(isLoading = true),
        )
    }
}
