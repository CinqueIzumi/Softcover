package nl.rhaydus.softcover.core.presentation.util

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

private const val SKELETON_CROSSFADE_DURATION_MS = 150

/**
 * Crossfades between the loading and loaded branches of a skeleton-bearing UI.
 *
 * Wraps an `isLoading` boolean swap so the moment shimmer hands off to real content is a
 * ~150ms alpha fade instead of a hard cut. The shimmer itself is unchanged — only the
 * swap-to-content moment animates.
 *
 * Gated by [playDecorativeMotion]: when the user has disabled animations system-wide
 * the swap is instant.
 */
@Composable
fun SkeletonCrossfade(
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    label: String = "SkeletonCrossfade",
    content: @Composable (isLoading: Boolean) -> Unit,
) {
    if (playDecorativeMotion()) {
        Crossfade(
            targetState = isLoading,
            modifier = modifier,
            animationSpec = tween(durationMillis = SKELETON_CROSSFADE_DURATION_MS),
            label = label,
            content = content,
        )
    } else {
        Box(modifier = modifier) {
            content(isLoading)
        }
    }
}
