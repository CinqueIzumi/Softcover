package nl.rhaydus.softcover.feature.explore.presentation.screen.section

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.modifier.pointerHandCursor

/**
 * Presentation-only title-casing for a mood label (explore-3a feedback item 3): the API returns
 * moods lowercase ("adventurous", "cosy & comforting"). The domain model stays untouched — only the
 * render layer capitalizes each whitespace-separated word before it reaches a `Text`.
 */
internal fun String.toTitleCaseWords(): String =
    split(' ').joinToString(" ") { word -> word.replaceFirstChar { it.titlecase() } }

@Composable
internal fun RecentSearchChip(
    query: String,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.pointerHandCursor(),
        shape = RoundedCornerShape(percent = 50),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    ) {
        Text(
            text = query,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 15.dp, vertical = 9.dp),
        )
    }
}
