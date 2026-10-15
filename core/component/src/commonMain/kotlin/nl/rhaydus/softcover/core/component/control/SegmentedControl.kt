package nl.rhaydus.softcover.core.component.control

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.modifier.hoverHighlight
import nl.rhaydus.softcover.core.component.DISABLED_ALPHA
import nl.rhaydus.softcover.core.component.modifier.tactileSelectable
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Corners
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

/**
 * A one-of-N choice. The caller supplies the outer padding; [SegmentedControlVariant.Track] fills the
 * width it is given. The key type [K] is only forwarded back through the event.
 */
@Composable
fun <K> SegmentedControl(
    model: SegmentedControlUiModel<K>,
    onEvent: (SegmentedControlEvent<K>) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (val variant = model.variant) {
        SegmentedControlVariant.Track -> TrackSegments(
            model = model,
            onEvent = onEvent,
            modifier = modifier,
        )

        is SegmentedControlVariant.Prose -> ProseSegments(
            model = model,
            variant = variant,
            onEvent = onEvent,
            modifier = modifier,
        )
    }
}

@Composable
private fun <K> TrackSegments(
    model: SegmentedControlUiModel<K>,
    onEvent: (SegmentedControlEvent<K>) -> Unit,
    modifier: Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = Corners.pill,
    ) {
        Row(modifier = Modifier.selectableGroup().padding(Spacing.xxs)) {
            model.options.forEach { option ->
                TrackSegment(
                    option = option,
                    isSelected = option.key == model.selected,
                    onSelect = {
                        onEvent(SegmentedControlEvent.Selected(key = option.key))
                    },
                )
            }
        }
    }
}

@Composable
private fun <K> RowScope.TrackSegment(
    option: SegmentUiModel<K>,
    isSelected: Boolean,
    onSelect: () -> Unit,
) {
    val colorSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Color>()

    val scheme = MaterialTheme.colorScheme

    val containerColor by animateColorAsState(
        targetValue = if (isSelected) scheme.primary else Color.Transparent,
        animationSpec = colorSpec,
        label = "SegmentContainer",
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) scheme.onPrimary else scheme.onSurfaceVariant,
        animationSpec = colorSpec,
        label = "SegmentContent",
    )

    // A segment flips enabled the instant its data lands; an un-animated alpha snap would read as a
    // flash amid everything else settling, so it rides the same spec as the selection.
    val floatSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()
    val disabledAlpha by animateFloatAsState(
        targetValue = if (option.enabled) 1f else DISABLED_ALPHA,
        animationSpec = floatSpec,
        label = "SegmentEnabledAlpha",
    )

    Surface(
        modifier = Modifier
            .weight(1f)
            .alpha(disabledAlpha)
            .minimumInteractiveComponentSize()
            .tactileSelectable(
                selected = isSelected,
                onClick = { if (isSelected.not()) onSelect() },
                role = Role.RadioButton,
                enabled = option.enabled,
                hoverShape = Corners.pill,
            ),
        color = containerColor,
        contentColor = contentColor,
        shape = Corners.pill,
    ) {
        Box(
            modifier = Modifier.padding(vertical = Spacing.xs),
            contentAlignment = Alignment.Center,
        ) {
            SoftcoverText(
                text = option.label,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun <K> ProseSegments(
    model: SegmentedControlUiModel<K>,
    variant: SegmentedControlVariant.Prose,
    onEvent: (SegmentedControlEvent<K>) -> Unit,
    modifier: Modifier,
) {
    Row(modifier = modifier.selectableGroup()) {
        SoftcoverText(
            text = variant.prefix,
            style = MaterialTheme.editorialTypography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.alignByBaseline(),
        )

        model.options.forEachIndexed { index, option ->
            if (index > 0) {
                SoftcoverText(
                    text = " / ",
                    style = MaterialTheme.editorialTypography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.alignByBaseline(),
                )
            }

            ProseWord(
                label = option.label,
                isSelected = option.key == model.selected,
                enabled = option.enabled,
                onSelect = {
                    onEvent(SegmentedControlEvent.Selected(key = option.key))
                },
                modifier = Modifier.alignByBaseline(),
            )
        }

        SoftcoverText(
            text = variant.suffix,
            style = MaterialTheme.editorialTypography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.alignByBaseline(),
        )
    }
}

/**
 * A word sits well below the 48dp minimum touch target, so [minimumInteractiveComponentSize] pads the
 * tap area invisibly. The underline is always laid out (transparent when unchosen) and the word keeps one
 * font weight, so a change never reflows the sentence; selection reads through colour and underline. The
 * underline is sized to the word's measured width rather than the row's.
 */
@Composable
private fun ProseWord(
    label: String,
    isSelected: Boolean,
    enabled: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scheme = MaterialTheme.colorScheme
    val colorSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Color>()
    val wordColor by animateColorAsState(
        targetValue = if (isSelected) scheme.primary else scheme.onSurfaceVariant.copy(alpha = 0.55f),
        animationSpec = colorSpec,
        label = "ProseWordColor",
    )
    val underlineColor by animateColorAsState(
        targetValue = if (isSelected) scheme.primary else Color.Transparent,
        animationSpec = colorSpec,
        label = "ProseWordUnderline",
    )
    val disabledAlpha by animateFloatAsState(
        targetValue = if (enabled) 1f else DISABLED_ALPHA,
        animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
        label = "ProseWordEnabledAlpha",
    )

    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .alpha(disabledAlpha)
            .tactileSelectable(
                selected = isSelected,
                onClick = { if (isSelected.not()) onSelect() },
                role = Role.RadioButton,
                enabled = enabled,
                hoverShape = null,
                interactionSource = interactionSource,
            ),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .width(IntrinsicSize.Max)
                .hoverHighlight(
                    interactionSource = interactionSource,
                    shape = Corners.pill,
                ),
        ) {
            SoftcoverText(
                text = label,
                style = MaterialTheme.editorialTypography.bodyLarge,
                color = wordColor,
            )

            Spacer(modifier = Modifier.height(Spacing.xxxs))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(underlineColor),
            )
        }
    }
}
