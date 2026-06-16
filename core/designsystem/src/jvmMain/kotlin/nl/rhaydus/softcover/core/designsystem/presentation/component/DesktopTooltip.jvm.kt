package nl.rhaydus.softcover.core.designsystem.presentation.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Desktop hover tooltip via Compose Desktop's `TooltipArea` (the hover-triggered foundation primitive,
// not Material3's long-press `TooltipBox`). The surface follows the editorial register — a
// `surfaceContainerHighest` card with a small radius and an Inter `labelMedium` label — rather than the
// stock Material tooltip chrome.
private const val TOOLTIP_HOVER_DELAY_MS = 500

@OptIn(ExperimentalFoundationApi::class)
@Composable
actual fun DesktopTooltip(
    text: String,
    content: @Composable () -> Unit,
) {
    TooltipArea(
        tooltip = {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                contentColor = MaterialTheme.colorScheme.onSurface,
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(
                        horizontal = 12.dp,
                        vertical = 6.dp,
                    ),
                )
            }
        },
        delayMillis = TOOLTIP_HOVER_DELAY_MS,
        content = content,
    )
}
