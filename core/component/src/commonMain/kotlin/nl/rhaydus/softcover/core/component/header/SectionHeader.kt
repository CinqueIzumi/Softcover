package nl.rhaydus.softcover.core.component.header

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.component.state.skeletonMask
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

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

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = spacedBy(Spacing.xxs)) {
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
        is SectionHeaderUiModel.Inline -> SectionHeaderInlineEyebrow(
            eyebrow = model.eyebrow,
            color = sectionHeaderToneColor(
                tone = model.tone,
                colorScheme = MaterialTheme.colorScheme,
            ),
            isLoading = model.isLoading,
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
    Column(modifier = modifier.fillMaxWidth()) {
        SectionHeaderBarRow(
            eyebrow = model.eyebrow,
            barWidth = 32.dp,
            barHeight = 4.dp,
            barShape = RoundedCornerShape(2.dp),
            gap = 12.dp,
            textStyle = MaterialTheme.editorialTypography.eyebrow,
            color = MaterialTheme.colorScheme.primary,
            isLoading = model.isLoading,
            isHeading = model.headline == null,
        )

        if (model.headline != null) {
            Spacer(modifier = Modifier.height(Spacing.xs))

            SoftcoverText(
                text = headerAnnotatedString(
                    segments = model.headline,
                    colorScheme = MaterialTheme.colorScheme,
                ),
                isLoading = model.isLoading,
                standIn = model.headlineText.orEmpty(),
                style = MaterialTheme.editorialTypography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.semantics { heading() },
            )
        }

        if (model.description != null) {
            Spacer(modifier = Modifier.height(Spacing.xs))

            SoftcoverText(
                text = headerAnnotatedString(
                    segments = model.description,
                    colorScheme = MaterialTheme.colorScheme,
                ),
                isLoading = model.isLoading,
                standIn = model.description.joinToString(separator = "") { it.text },
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
            is HeaderTextSegment.Emphasised -> withStyle(SpanStyle(color = colorScheme.primary)) {
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

/** The hairline-bar eyebrow of [SectionHeaderUiModel.Inline], for callers that colour it themselves. */
@Composable
internal fun SectionHeaderInlineEyebrow(
    eyebrow: String,
    color: Color,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
) {
    SectionHeaderBarRow(
        eyebrow = eyebrow,
        barWidth = 20.dp,
        barHeight = 1.dp,
        barShape = RectangleShape,
        gap = 8.dp,
        textStyle = MaterialTheme.editorialTypography.eyebrowSmall,
        color = color,
        isLoading = isLoading,
        modifier = modifier,
    )
}

@Composable
private fun SectionHeaderLabel(
    model: SectionHeaderUiModel.Label,
    modifier: Modifier = Modifier,
) {
    SectionHeaderEyebrowText(
        eyebrow = model.eyebrow,
        style = MaterialTheme.editorialTypography.eyebrowSmall,
        color = sectionHeaderToneColor(
            tone = model.tone,
            colorScheme = MaterialTheme.colorScheme,
        ),
        isLoading = model.isLoading,
        modifier = modifier.semantics(mergeDescendants = true) { heading() },
    )
}

@Composable
private fun SectionHeaderBarRow(
    eyebrow: String,
    barWidth: Dp,
    barHeight: Dp,
    barShape: Shape,
    gap: Dp,
    textStyle: TextStyle,
    color: Color,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    isHeading: Boolean = true,
) {
    Row(
        modifier = modifier.then(
            if (isHeading) Modifier.semantics(mergeDescendants = true) { heading() } else Modifier,
        ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(
                    width = barWidth,
                    height = barHeight,
                )
                .skeletonMask(
                    visible = isLoading,
                    shape = barShape,
                )
                .clip(barShape)
                .background(color),
        )

        Spacer(modifier = Modifier.width(gap))

        SectionHeaderEyebrowText(
            eyebrow = eyebrow,
            style = textStyle,
            color = color,
            isLoading = isLoading,
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
    isLoading: Boolean = false,
) {
    SoftcoverText(
        text = eyebrow.uppercase(),
        isLoading = isLoading,
        standIn = eyebrow.uppercase(),
        style = style,
        color = color,
        modifier = modifier,
    )
}
