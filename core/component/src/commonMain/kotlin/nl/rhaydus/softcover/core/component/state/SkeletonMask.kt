package nl.rhaydus.softcover.core.component.state

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.modifier.shimmer

internal const val SKELETON_GRACE_MILLIS = 250L

internal const val SKELETON_MINIMUM_VISIBLE_MILLIS = 300L

private const val NOT_STARTED = -1L

/**
 * The phase of a skeleton [elapsedMillis] after loading began. [loadingEndedAtMillis] is the elapsed
 * time at which loading finished, or `null` while it is still running.
 */
internal fun skeletonRevealPhase(
    elapsedMillis: Long,
    loadingEndedAtMillis: Long?,
): SkeletonRevealPhase {
    if (loadingEndedAtMillis == null) {
        return if (elapsedMillis < SKELETON_GRACE_MILLIS) {
            SkeletonRevealPhase.Hidden
        } else {
            SkeletonRevealPhase.Shimmer
        }
    }

    if (loadingEndedAtMillis < SKELETON_GRACE_MILLIS) return SkeletonRevealPhase.Content

    val shimmerEndsAtMillis = maxOf(
        loadingEndedAtMillis,
        SKELETON_GRACE_MILLIS + SKELETON_MINIMUM_VISIBLE_MILLIS,
    )

    return if (elapsedMillis < shimmerEndsAtMillis) {
        SkeletonRevealPhase.Shimmer
    } else {
        SkeletonRevealPhase.Content
    }
}

/**
 * Masks the content while [visible], timed by [skeletonRevealPhase]: nothing is drawn for the first
 * [SKELETON_GRACE_MILLIS], then a shimmer that stays at least [SKELETON_MINIMUM_VISIBLE_MILLIS].
 * The content still measures and lays out, so a stand-in value sizes the slot the real one will fill.
 *
 * Timed on the frame clock, so leaves composed in the same frame share one start. The minimum hold only
 * finishes while the leaf stays composed; a leaf that leaves composition when loading ends drops it.
 */
@Composable
fun Modifier.skeletonMask(
    visible: Boolean,
    shape: Shape = RoundedCornerShape(4.dp),
): Modifier {
    var phase by remember { mutableStateOf(SkeletonRevealPhase.Content) }
    var startMillis by remember { mutableLongStateOf(NOT_STARTED) }

    LaunchedEffect(visible) {
        if (visible) {
            if (phase == SkeletonRevealPhase.Shimmer) return@LaunchedEffect

            startMillis = withFrameMillis { it }
            phase = SkeletonRevealPhase.Hidden

            while (phase == SkeletonRevealPhase.Hidden) {
                val now = withFrameMillis { it }
                phase = skeletonRevealPhase(
                    elapsedMillis = now - startMillis,
                    loadingEndedAtMillis = null,
                )
            }

            return@LaunchedEffect
        }

        if (startMillis == NOT_STARTED || phase != SkeletonRevealPhase.Shimmer) {
            phase = SkeletonRevealPhase.Content

            return@LaunchedEffect
        }

        val endedAtMillis = withFrameMillis { it } - startMillis

        while (true) {
            phase = skeletonRevealPhase(
                elapsedMillis = withFrameMillis { it } - startMillis,
                loadingEndedAtMillis = endedAtMillis,
            )

            if (phase == SkeletonRevealPhase.Content) break
        }
    }

    val effectivePhase = when {
        visible && phase == SkeletonRevealPhase.Content -> SkeletonRevealPhase.Hidden
        visible.not() && phase == SkeletonRevealPhase.Hidden -> SkeletonRevealPhase.Content
        else -> phase
    }

    return when (effectivePhase) {
        SkeletonRevealPhase.Hidden -> this
            .drawWithContent { }
            .clearAndSetSemantics { }

        SkeletonRevealPhase.Shimmer -> this
            .shimmer(shape = shape)
            .clearAndSetSemantics { }

        SkeletonRevealPhase.Content -> this
    }
}
