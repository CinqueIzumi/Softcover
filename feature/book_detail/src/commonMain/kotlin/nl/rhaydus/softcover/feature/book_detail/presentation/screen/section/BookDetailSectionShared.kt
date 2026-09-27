package nl.rhaydus.softcover.feature.book_detail.presentation.screen.section

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.motion.playDecorativeMotion
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import kotlin.math.abs

/**
 * Small in-flow label with NO accent bar (design-system.md's "small in-flow labels" contract) — used
 * for the compact labels that sit directly inside a section's flow rather than opening a new region:
 * YOUR RATING / YOUR TAGS / YOUR REVIEW / TAGS / FIND IT.
 */
@Composable
internal fun SmallSectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.editorialTypography.eyebrowSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
internal fun SectionLabel(
    text: String,
    color: Color = MaterialTheme.colorScheme.primary,
    pulseKey: Int = 0,
) {
    val playMotion = playDecorativeMotion()

    val pulse = remember { Animatable(initialValue = 0f) }

    LaunchedEffect(pulseKey) {
        if (pulseKey == 0 || playMotion.not()) return@LaunchedEffect

        pulse.snapTo(targetValue = 0f)
        pulse.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 400),
        )
    }

    val envelope = (1f - abs(pulse.value * 2f - 1f)).coerceIn(
        minimumValue = 0f,
        maximumValue = 1f,
    )

    val barWidth = 32.dp + (16.dp * envelope)

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .height(4.dp)
                .width(barWidth)
                .graphicsLayer {
                    scaleY = 1f + envelope * 0.5f
                }
                .clip(RoundedCornerShape(2.dp))
                .background(color),
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = text.uppercase(),
            style = MaterialTheme.editorialTypography.eyebrow,
            color = color,
        )
    }
}
