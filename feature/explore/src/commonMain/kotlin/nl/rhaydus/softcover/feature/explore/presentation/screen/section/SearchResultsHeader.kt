package nl.rhaydus.softcover.feature.explore.presentation.screen.section

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

/** The "SHOWING {N} RESULTS" header (explore-3a §4 "Search results"). */
@Composable
internal fun SearchResultsHeader(
    resultCount: Int,
    subtitle: String?,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .height(4.dp)
                .width(32.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.primary),
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Showing $resultCount results".uppercase(),
            style = MaterialTheme.editorialTypography.eyebrow,
            color = MaterialTheme.colorScheme.primary,
        )

        if (subtitle != null) {
            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
