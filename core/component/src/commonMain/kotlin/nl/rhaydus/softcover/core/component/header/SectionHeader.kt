package nl.rhaydus.softcover.core.component.header

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.motion.playDecorativeMotion
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import kotlin.math.abs

/**
 * Opens a region within a page or sheet: one of [SectionHeaderUiModel]'s three accent-bar registers,
 * from [SectionHeaderUiModel.Section]'s rounded bar and optional headline/description down to
 * [SectionHeaderUiModel.Label]'s bare eyebrow. Inert — a header is read, never tapped.
 */
@Composable
fun SectionHeader(
    model: SectionHeaderUiModel,
    modifier: Modifier = Modifier,
) {
    when (model) {
        is SectionHeaderUiModel.Section -> SectionHeaderRegion(
            model = model,
            modifier = modifier,
        )
        is SectionHeaderUiModel.Inline -> SectionHeaderInline(
            model = model,
            modifier = modifier,
        )
        is SectionHeaderUiModel.Label -> SectionHeaderLabel(
            model = model,
            modifier = modifier,
        )
    }
}

@Composable
private fun SectionHeaderRegion(
    model: SectionHeaderUiModel.Section,
    modifier: Modifier = Modifier,
) {
    val playMotion = playDecorativeMotion()

    val pulse = remember { Animatable(initialValue = 0f) }

    LaunchedEffect(model.pulseKey) {
        if (model.pulseKey == 0 || playMotion.not()) return@LaunchedEffect

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

    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .height(4.dp)
                    .width(barWidth)
                    .graphicsLayer { scaleY = 1f + envelope * 0.5f }
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.primary),
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = model.eyebrow.uppercase(),
                style = MaterialTheme.editorialTypography.eyebrow,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        if (model.headline != null) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = model.headline,
                style = MaterialTheme.editorialTypography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        if (model.description != null) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = model.description,
                style = MaterialTheme.editorialTypography.body,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SectionHeaderInline(
    model: SectionHeaderUiModel.Inline,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .height(1.dp)
                .width(20.dp)
                .background(MaterialTheme.colorScheme.primary),
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = model.eyebrow.uppercase(),
            style = MaterialTheme.editorialTypography.eyebrowSmall,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun SectionHeaderLabel(
    model: SectionHeaderUiModel.Label,
    modifier: Modifier = Modifier,
) {
    Text(
        text = model.eyebrow.uppercase(),
        style = MaterialTheme.editorialTypography.eyebrowSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}
