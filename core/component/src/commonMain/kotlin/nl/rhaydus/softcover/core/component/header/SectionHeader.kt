package nl.rhaydus.softcover.core.component.header

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.motion.playDecorativeMotion
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import kotlin.math.abs

private const val PULSE_BAR_WIDTH_DELTA_DP = 16
private const val PULSE_SCALE_DELTA = 0.5f

/**
 * Opens a region within a page or sheet: one of [SectionHeaderUiModel]'s three accent-bar registers,
 * from [SectionHeaderUiModel.Section]'s rounded bar and optional headline/description down to
 * [SectionHeaderUiModel.Label]'s bare eyebrow. The header text itself is inert — it is read, never
 * tapped; [trailing] is the only interactive part, trailing the header column (e.g. an action pill).
 *
 * Without [trailing], the register sizes itself under [modifier] alone — a [SectionHeaderUiModel.Section]
 * still fills the width, an [SectionHeaderUiModel.Inline] or [SectionHeaderUiModel.Label] shrink-wraps.
 * Passing [trailing] switches the whole header to a fill-width row with the register weighted against it.
 */
@Composable
fun SectionHeader(
    model: SectionHeaderUiModel,
    modifier: Modifier = Modifier,
    trailing: (@Composable RowScope.() -> Unit)? = null,
) {
    if (trailing == null) {
        SectionHeaderRegister(
            model = model,
            modifier = modifier,
        )
        return
    }

    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        SectionHeaderRegister(
            model = model,
            modifier = Modifier.weight(1f),
        )

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = spacedBy(4.dp)) {
            trailing()
        }
    }
}

@Composable
private fun SectionHeaderRegister(
    model: SectionHeaderUiModel,
    modifier: Modifier,
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

private data class SectionHeaderEyebrowStyle(
    val bar: BarSpec?,
    val gap: Dp,
    val textStyle: TextStyle,
) {
    data class BarSpec(
        val width: Dp,
        val height: Dp,
        val shape: Shape,
    )
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

    Column(modifier = modifier.fillMaxWidth()) {
        SectionHeaderEyebrowRow(
            eyebrow = model.eyebrow,
            style = SectionHeaderEyebrowStyle(
                bar = SectionHeaderEyebrowStyle.BarSpec(
                    width = 32.dp,
                    height = 4.dp,
                    shape = RoundedCornerShape(2.dp),
                ),
                gap = 12.dp,
                textStyle = MaterialTheme.editorialTypography.eyebrow,
            ),
            color = sectionHeaderToneColor(
                tone = model.tone,
                colorScheme = MaterialTheme.colorScheme,
            ),
            pulse = pulse,
            isHeading = model.headline == null,
        )

        if (model.headline != null) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = headerAnnotatedString(
                    segments = model.headline,
                    colorScheme = MaterialTheme.colorScheme,
                ),
                style = MaterialTheme.editorialTypography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.semantics { heading() },
            )
        }

        if (model.description != null) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = headerAnnotatedString(
                    segments = model.description,
                    colorScheme = MaterialTheme.colorScheme,
                ),
                style = MaterialTheme.editorialTypography.body,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

internal fun headerAnnotatedString(
    segments: List<HeaderTextSegment>,
    colorScheme: ColorScheme,
): AnnotatedString = buildAnnotatedString {
    segments.forEach { segment ->
        when (segment) {
            is HeaderTextSegment.Plain -> append(segment.text)
            is HeaderTextSegment.Emphasised -> withStyle(
                when (segment.treatment) {
                    EmphasisTreatment.Accent -> SpanStyle(color = colorScheme.primary)
                },
            ) {
                append(segment.text)
            }
        }
    }
}

internal fun sectionHeaderToneColor(
    tone: SectionHeaderTone,
    colorScheme: ColorScheme,
): Color = when (tone) {
    SectionHeaderTone.Accent -> colorScheme.primary
    SectionHeaderTone.Error -> colorScheme.error
    SectionHeaderTone.Muted -> colorScheme.onSurfaceVariant
}

@Composable
private fun SectionHeaderInline(
    model: SectionHeaderUiModel.Inline,
    modifier: Modifier = Modifier,
) {
    SectionHeaderEyebrowRow(
        eyebrow = model.eyebrow,
        style = SectionHeaderEyebrowStyle(
            bar = SectionHeaderEyebrowStyle.BarSpec(
                width = 20.dp,
                height = 1.dp,
                shape = RectangleShape,
            ),
            gap = 8.dp,
            textStyle = MaterialTheme.editorialTypography.eyebrowSmall,
        ),
        color = sectionHeaderToneColor(
            tone = model.tone,
            colorScheme = MaterialTheme.colorScheme,
        ),
        modifier = modifier,
    )
}

@Composable
private fun SectionHeaderLabel(
    model: SectionHeaderUiModel.Label,
    modifier: Modifier = Modifier,
) {
    SectionHeaderEyebrowRow(
        eyebrow = model.eyebrow,
        style = SectionHeaderEyebrowStyle(
            bar = null,
            gap = 0.dp,
            textStyle = MaterialTheme.editorialTypography.eyebrowSmall,
        ),
        color = sectionHeaderToneColor(
            tone = model.tone,
            colorScheme = MaterialTheme.colorScheme,
        ),
        modifier = modifier,
    )
}

@Composable
private fun SectionHeaderEyebrowRow(
    eyebrow: String,
    style: SectionHeaderEyebrowStyle,
    color: Color,
    modifier: Modifier = Modifier,
    pulse: Animatable<Float, AnimationVector1D>? = null,
    isHeading: Boolean = true,
) {
    Row(
        modifier = modifier.then(
            if (isHeading) Modifier.semantics(mergeDescendants = true) { heading() } else Modifier,
        ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val bar = style.bar

        if (bar != null) {
            Box(
                modifier = Modifier
                    .height(bar.height)
                    .pulsingBarWidth(
                        baseWidth = bar.width,
                        pulse = pulse,
                    )
                    .pulsingScaleY(pulse = pulse)
                    .clip(bar.shape)
                    .background(color),
            )

            Spacer(modifier = Modifier.width(style.gap))
        }

        SectionHeaderEyebrowText(
            eyebrow = eyebrow,
            style = style.textStyle,
            color = color,
        )
    }
}

/** [PageMasthead] reuses this for its own no-bar eyebrow. */
@Composable
internal fun SectionHeaderEyebrowText(
    eyebrow: String,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Text(
        text = eyebrow.uppercase(),
        style = style,
        color = color,
        modifier = modifier,
    )
}

/**
 * Fraction of a pulse cycle spent at its peak, `0f` at the ends of [progress] and `1f` at its
 * midpoint — the widen-and-settle envelope for [SectionHeaderRegion]'s bar.
 */
internal fun sectionHeaderPulseEnvelope(progress: Float): Float =
    (1f - abs(progress * 2f - 1f)).coerceIn(
        minimumValue = 0f,
        maximumValue = 1f,
    )

private fun Modifier.pulsingBarWidth(
    baseWidth: Dp,
    pulse: Animatable<Float, AnimationVector1D>?,
): Modifier = if (pulse == null) {
    width(baseWidth)
} else {
    layout { measurable, constraints ->
        val envelope = sectionHeaderPulseEnvelope(progress = pulse.value)
        val width = (baseWidth + PULSE_BAR_WIDTH_DELTA_DP.dp * envelope).roundToPx()
        val placeable = measurable.measure(
            constraints.copy(
                minWidth = width,
                maxWidth = width,
            ),
        )

        layout(width = width, height = placeable.height) {
            placeable.placeRelative(
                x = 0,
                y = 0,
            )
        }
    }
}

private fun Modifier.pulsingScaleY(pulse: Animatable<Float, AnimationVector1D>?): Modifier = if (pulse == null) {
    this
} else {
    graphicsLayer {
        scaleY = 1f + sectionHeaderPulseEnvelope(progress = pulse.value) * PULSE_SCALE_DELTA
    }
}
