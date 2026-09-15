package nl.rhaydus.softcover.core.component.callout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * A full-width line of chrome that slides in above a screen's content to report a condition the
 * reader should know about — today the offline notice, raised once at the app root rather than per
 * screen.
 *
 * It owns its own expand/shrink transition, so a caller flips [BannerUiModel.visible] and the banner
 * handles arriving and leaving.
 */
@Composable
fun Banner(
    model: BannerUiModel,
    modifier: Modifier = Modifier,
) {
    val container = when (model.tone) {
        BannerTone.WARNING -> MaterialTheme.colorScheme.errorContainer
    }

    val content = when (model.tone) {
        BannerTone.WARNING -> MaterialTheme.colorScheme.onErrorContainer
    }

    AnimatedVisibility(
        visible = model.visible,
        enter = expandVertically(),
        exit = shrinkVertically(),
    ) {
        Text(
            text = model.message,
            style = MaterialTheme.typography.bodySmall,
            color = content,
            textAlign = TextAlign.Center,
            modifier = modifier
                .fillMaxWidth()
                .background(container)
                .padding(
                    horizontal = 16.dp,
                    vertical = 8.dp,
                ),
        )
    }
}
