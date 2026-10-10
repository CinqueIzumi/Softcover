package nl.rhaydus.softcover.core.component.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember

internal val LocalSkeletonRevealGroup = compositionLocalOf<SkeletonRevealGroupHolder?> { null }

/**
 * Scopes the [skeletonMask]s in [content] so that, once any of them has shown its content, a mask that
 * starts loading afterwards, including a freshly composed one, shimmers at once instead of waiting out the grace.
 */
@Composable
fun SkeletonRevealGroup(content: @Composable () -> Unit) {
    val holder = remember { SkeletonRevealGroupHolder() }

    CompositionLocalProvider(LocalSkeletonRevealGroup provides holder) {
        content()
    }
}
