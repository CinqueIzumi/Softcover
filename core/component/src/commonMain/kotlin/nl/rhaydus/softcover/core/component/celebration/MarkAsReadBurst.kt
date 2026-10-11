package nl.rhaydus.softcover.core.component.celebration

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.motion.playDecorativeMotion
import nl.rhaydus.softcover.core.designsystem.presentation.theme.motion
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random

/**
 * A radial particle burst played on a successful mark-as-read commit. The caller composes this as a
 * sibling in a `Box` and sets [modifier] to `Modifier.matchParentSize()` (or a fixed footprint) —
 * particles travel outward from the centre of that footprint.
 *
 * The burst plays each time [MarkAsReadBurstUiModel.triggerKey] changes after first composition; the key
 * it first composes with never plays, so returning to a screen does not replay an old celebration.
 * Suppressed when the user has disabled system animations. [MarkAsReadBurstUiModel.scale] sets the
 * particle count and duration.
 *
 * The burst plays no haptic: pair every trigger with `Feedback.Milestone` so the two land together. Read is
 * deliberately the only shelf-status change with this celebration; no other transition should reuse it.
 */
@Composable
fun MarkAsReadBurst(
    model: MarkAsReadBurstUiModel,
    modifier: Modifier = Modifier,
) {
    val particleCount: Int
    val durationMillis: Int

    when (model.scale) {
        MarkAsReadBurstScale.Card -> {
            particleCount = 16
            durationMillis = 800
        }

        MarkAsReadBurstScale.Screen -> {
            particleCount = 28
            durationMillis = 1000
        }
    }

    val color = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.tertiary
    val playMotion = playDecorativeMotion()
    val celebration = MaterialTheme.motion.celebration(durationMillis = durationMillis)

    val progress = remember { Animatable(initialValue = 0f) }

    var lastPlayedKey by rememberSaveable { mutableIntStateOf(model.triggerKey) }

    LaunchedEffect(model.triggerKey) {
        if (model.triggerKey == lastPlayedKey) return@LaunchedEffect

        lastPlayedKey = model.triggerKey

        if (playMotion.not()) return@LaunchedEffect

        progress.snapTo(targetValue = 0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = celebration,
        )
    }

    val particles = remember(particleCount) { buildParticleSeeds(count = particleCount) }

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
