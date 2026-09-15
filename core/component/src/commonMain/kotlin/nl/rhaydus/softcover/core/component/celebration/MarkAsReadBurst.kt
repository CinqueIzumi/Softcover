package nl.rhaydus.softcover.core.component.celebration

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random
import nl.rhaydus.designsystem.motion.playDecorativeMotion

/**
 * A radial particle burst played on a successful mark-as-read commit. The caller composes this as a
 * sibling in a `Box` and sets [modifier] to `Modifier.matchParentSize()` (or a fixed footprint) —
 * particles travel outward from the centre of that footprint.
 *
 * The burst replays whenever [MarkAsReadBurstUiModel.triggerKey] changes to a non-zero value,
 * including on repeat commits. Suppressed when the user has disabled system animations.
 *
 * [color] and [secondaryColor] stay parameters rather than model fields: they default to the theme
 * roles the burst is specified in, and the one caller that overrides them is overriding a *tint*,
 * not describing different data.
 */
@Composable
fun MarkAsReadBurst(
    model: MarkAsReadBurstUiModel,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    secondaryColor: Color = MaterialTheme.colorScheme.tertiary,
) {
    val playMotion = playDecorativeMotion()

    val progress = remember { Animatable(initialValue = 0f) }

    LaunchedEffect(model.triggerKey) {
        if (model.triggerKey == 0 || playMotion.not()) return@LaunchedEffect

        progress.snapTo(targetValue = 0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = model.durationMillis,
                easing = FastOutSlowInEasing,
            ),
        )
    }

    val particles = remember(model.particleCount) { buildParticleSeeds(count = model.particleCount) }

    Canvas(modifier = modifier) {
        val current = progress.value
        if (current == 0f) return@Canvas

        val centre = Offset(
            x = size.width / 2f,
            y = size.height / 2f,
        )
        val maxRadius = min(
            a = size.width,
            b = size.height,
        ) * 0.65f

        particles.forEach { seed ->
            val travel = maxRadius * seed.radiusFactor * easeOutCubic(current)
            val x = centre.x + cos(seed.angle) * travel
            val y = centre.y + sin(seed.angle) * travel

            val particleRadius = seed.sizeDp.dp.toPx() * (1f - current * 0.35f)

            val alpha = (1f - current).pow(1.4f).coerceIn(
                minimumValue = 0f,
                maximumValue = 1f,
            )

            val tint = if (seed.useSecondary) secondaryColor else color

            drawCircle(
                color = tint.copy(alpha = alpha),
                radius = particleRadius.coerceAtLeast(minimumValue = 0f),
                center = Offset(
                    x = x,
                    y = y,
                ),
            )
        }
    }
}

private data class ParticleSeed(
    val angle: Float,
    val radiusFactor: Float,
    val sizeDp: Float,
    val useSecondary: Boolean,
)

private fun buildParticleSeeds(count: Int): List<ParticleSeed> {
    val random = Random(seed = 0x5EAF00D)
    val twoPi = (2f * PI).toFloat()

    return List(size = count) { index ->
        val baseAngle = (index / count.toFloat()) * twoPi
        val jitter = (random.nextFloat() - 0.5f) * (twoPi / count) * 0.6f
        val radiusFactor = 0.7f + random.nextFloat() * 0.5f
        val sizeDp = 2.5f + random.nextFloat() * 3f
        val useSecondary = index % 3 == 0

        ParticleSeed(
            angle = baseAngle + jitter,
            radiusFactor = radiusFactor,
            sizeDp = sizeDp,
            useSecondary = useSecondary,
        )
    }
}

private fun easeOutCubic(t: Float): Float = 1f - (1f - t).pow(3)
