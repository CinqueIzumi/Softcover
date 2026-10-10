package nl.rhaydus.softcover.core.component.state

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.clearAndSetSemantics
import nl.rhaydus.designsystem.motion.playDecorativeMotion
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Corners

internal const val SKELETON_GRACE_MILLIS = 250L

internal const val SKELETON_MINIMUM_VISIBLE_MILLIS = 300L

internal const val SKELETON_FADE_MILLIS = 150

private const val SHIMMER_SWEEP_MILLIS = 1600

private const val SHIMMER_BASE_ALPHA = 0.08f

private const val SHIMMER_HIGHLIGHT_ALPHA = 0.16f

private const val NOT_STARTED = -1L

/**
 * The phase of a skeleton [elapsedMillis] after loading began. [loadingEndedAtMillis] is the elapsed
 * time at which loading finished, or `null` while it is still running.
 */
internal fun skeletonRevealPhase(
    elapsedMillis: Long,
    loadingEndedAtMillis: Long?,
    skipsGrace: Boolean = false,
): SkeletonRevealPhase {
    val graceMillis = if (skipsGrace) 0L else SKELETON_GRACE_MILLIS

    if (loadingEndedAtMillis == null) {
        return if (elapsedMillis < graceMillis) {
            SkeletonRevealPhase.Hidden
        } else {
            SkeletonRevealPhase.Shimmer
        }
    }

    if (loadingEndedAtMillis < graceMillis) return SkeletonRevealPhase.Content

    val shimmerEndsAtMillis = maxOf(
        loadingEndedAtMillis,
        graceMillis + SKELETON_MINIMUM_VISIBLE_MILLIS,
    )

    return if (elapsedMillis < shimmerEndsAtMillis) {
        SkeletonRevealPhase.Shimmer
    } else {
        SkeletonRevealPhase.Content
    }
}

/**
 * Masks the content while [visible], timed by [skeletonRevealPhase]: nothing is drawn for the first
 * [SKELETON_GRACE_MILLIS] of a first load, then a shimmer that stays at least [SKELETON_MINIMUM_VISIBLE_MILLIS], then
 * a crossfade to the content over [SKELETON_FADE_MILLIS] (a plain fade-in when loading ends within the grace).
 * The grace is skipped when the skeleton replaces content this mask, or its [SkeletonRevealGroup], already showed.
 * The content still measures and lays out, so a stand-in value sizes the slot the real one will fill.
 *
 * Timed on the frame clock, so leaves composed in the same frame share one start. The minimum hold only
 * finishes while the leaf stays composed; a leaf that leaves composition when loading ends drops it.
 */
@Composable
fun Modifier.skeletonMask(
    visible: Boolean,
    shape: Shape = Corners.xs,
): Modifier {
    var phase by remember { mutableStateOf(SkeletonRevealPhase.Content) }
    var startMillis by remember { mutableLongStateOf(NOT_STARTED) }
    var hasRevealed by remember { mutableStateOf(false) }
    var graceSkipped by remember { mutableStateOf(false) }
    val group = LocalSkeletonRevealGroup.current
    val skipsGrace = hasRevealed || group?.hasShownContent == true

    LaunchedEffect(visible) {
        if (visible) {
            if (phase == SkeletonRevealPhase.Shimmer) return@LaunchedEffect

            startMillis = withFrameMillis { it }
            graceSkipped = skipsGrace

            if (skipsGrace) {
                phase = SkeletonRevealPhase.Shimmer

                return@LaunchedEffect
            }

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
            hasRevealed = true
            group?.hasShownContent = true

            return@LaunchedEffect
        }

        val endedAtMillis = withFrameMillis { it } - startMillis

        while (true) {
            phase = skeletonRevealPhase(
                elapsedMillis = withFrameMillis { it } - startMillis,
                loadingEndedAtMillis = endedAtMillis,
                skipsGrace = graceSkipped,
            )

            if (phase == SkeletonRevealPhase.Content) break
        }

        hasRevealed = true
        group?.hasShownContent = true
    }

    val effectivePhase = when {
        visible && phase == SkeletonRevealPhase.Content && skipsGrace -> SkeletonRevealPhase.Shimmer
        visible && phase == SkeletonRevealPhase.Content -> SkeletonRevealPhase.Hidden
        visible.not() && phase == SkeletonRevealPhase.Hidden -> SkeletonRevealPhase.Content
        else -> phase
    }

    val playMotion = playDecorativeMotion()
    val reveal = remember { Animatable(1f) }
    var fadesFromShimmer by remember { mutableStateOf(false) }

    LaunchedEffect(effectivePhase, playMotion) {
        if (effectivePhase != SkeletonRevealPhase.Content) {
            fadesFromShimmer = effectivePhase == SkeletonRevealPhase.Shimmer
            reveal.snapTo(0f)

            return@LaunchedEffect
        }

        if (playMotion) {
            reveal.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = SKELETON_FADE_MILLIS),
            )
        } else {
            reveal.snapTo(1f)
        }

        fadesFromShimmer = false
    }

    val shimmerColor = MaterialTheme.colorScheme.onSurface
    val showsShimmer = effectivePhase == SkeletonRevealPhase.Shimmer || fadesFromShimmer
    val sweep = if (showsShimmer && playMotion) rememberShimmerSweep() else null

    return this
        .drawWithCache {
            val outline = shape.createOutline(
                size = size,
                layoutDirection = layoutDirection,
                density = this,
            )

            onDrawWithContent {
                val shimmerAlpha = when {
                    effectivePhase == SkeletonRevealPhase.Shimmer -> 1f
                    effectivePhase == SkeletonRevealPhase.Content && fadesFromShimmer && playMotion -> 1f - reveal.value
                    else -> 0f
                }

                if (shimmerAlpha > 0f) {
                    drawShimmer(
                        outline = outline,
                        color = shimmerColor,
                        progress = sweep?.value,
                        alpha = shimmerAlpha,
                    )
                }

                drawContent()
            }
        }
        .graphicsLayer {
            alpha = when {
                effectivePhase != SkeletonRevealPhase.Content -> 0f
                playMotion -> reveal.value
                else -> 1f
            }
        }
        .then(
            if (effectivePhase == SkeletonRevealPhase.Content) {
                Modifier
            } else {
                Modifier.clearAndSetSemantics { }
            },
        )
}

@Composable
fun Modifier.clippedSkeletonMask(
    visible: Boolean,
    shape: Shape,
): Modifier = this
    .clip(shape)
    .skeletonMask(
        visible = visible,
        shape = shape,
    )

@Composable
private fun rememberShimmerSweep(): State<Float> = rememberInfiniteTransition(label = "skeletonShimmer")
    .animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = SHIMMER_SWEEP_MILLIS,
                easing = FastOutSlowInEasing,
            ),
        ),
        label = "skeletonShimmerSweep",
    )

private fun DrawScope.drawShimmer(
    outline: Outline,
    color: Color,
    progress: Float?,
    alpha: Float,
) {
    val base = color.copy(alpha = SHIMMER_BASE_ALPHA)

    if (progress == null) {
        drawOutline(
            outline = outline,
            color = base,
            alpha = alpha,
        )

        return
    }

    val bandWidth = size.width / 1.5f
    val bandStart = -bandWidth + progress * (size.width + bandWidth)

    drawOutline(
        outline = outline,
        brush = Brush.linearGradient(
            colors = listOf(base, color.copy(alpha = SHIMMER_HIGHLIGHT_ALPHA), base),
            start = Offset(
                x = bandStart,
                y = 0f,
            ),
            end = Offset(
                x = bandStart + bandWidth,
                y = size.height,
            ),
        ),
        alpha = alpha,
    )
}
