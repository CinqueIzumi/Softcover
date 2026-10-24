package nl.rhaydus.softcover.feature.profile.presentation.screen.section

import androidx.compose.animation.Crossfade
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.designsystem.motion.playDecorativeMotion

/**
 * Fades from one scope's [target] to the next so content a component cannot animate itself, such as a
 * row of labels, never pops. Reduced motion swaps it in place.
 */
@Composable
internal fun <T> ScopeCrossfade(
    target: T,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit,
) {
    if (playDecorativeMotion()) {
        Crossfade(
            targetState = target,
            modifier = modifier.animateScopeContentSize(),
            label = "ScopeCrossfade",
            content = content,
        )
    } else {
        content(target)
    }
}
