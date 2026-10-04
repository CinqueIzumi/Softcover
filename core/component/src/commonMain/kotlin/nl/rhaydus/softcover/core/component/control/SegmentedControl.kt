package nl.rhaydus.softcover.core.component.control

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.haptics.rememberHaptics
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.designsystem.modifier.pressScaleClickable
import nl.rhaydus.designsystem.motion.playDecorativeMotion
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

private val TrackShape = RoundedCornerShape(percent = 50)

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
    val haptics = rememberHaptics()

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = TrackShape,
    ) {
        Row(modifier = Modifier.selectableGroup().padding(4.dp)) {
            model.options.forEach { option ->
                TrackSegment(
                    option = option,
                    isSelected = option.key == model.selected,
                    onSelect = {
                        haptics.select()
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
    val playMotion = playDecorativeMotion()
    val colorSpec = if (playMotion) tween<Color>(durationMillis = 220) else snap()

    val containerColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = colorSpec,
        label = "SegmentContainer",
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = colorSpec,
        label = "SegmentContent",
    )

    // A segment flips enabled the instant its data lands; an un-animated alpha snap would read as a
    // flash amid everything else settling, so it rides the same tween/snap gate as the selection.
    val floatSpec = if (playMotion) tween<Float>(durationMillis = 220) else snap()
    val disabledAlpha by animateFloatAsState(
        targetValue = if (option.enabled) 1f else 0.4f,
        animationSpec = floatSpec,
        label = "SegmentEnabledAlpha",
    )

    Surface(
        modifier = Modifier
            .weight(1f)
            .alpha(disabledAlpha)
            .semantics {
                role = Role.Tab
                selected = isSelected
            },
        color = containerColor,
        contentColor = contentColor,
        shape = TrackShape,
        onClick = { if (isSelected.not()) onSelect() },
        enabled = option.enabled,
    ) {
        Box(
            modifier = Modifier.padding(vertical = 9.dp),
            contentAlignment = Alignment.Center,
        ) {
            SoftcoverText(
                text = option.label,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
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
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.selectableGroup(),
    ) {
        SoftcoverText(
            text = variant.prefix,
            style = MaterialTheme.editorialTypography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )

        model.options.forEachIndexed { index, option ->
            if (index > 0) {
                SoftcoverText(
                    text = " / ",
                    style = MaterialTheme.editorialTypography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                )
            }

            ProseWord(
                label = option.label,
                isSelected = option.key == model.selected,
                enabled = option.enabled,
                onSelect = { onEvent(SegmentedControlEvent.Selected(key = option.key)) },
            )
        }

        SoftcoverText(
            text = variant.suffix,
            style = MaterialTheme.editorialTypography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

/**
 * A word sits well below the 48dp minimum touch target, so [minimumInteractiveComponentSize] pads the
 * tap area invisibly. The underline is always laid out (transparent when unchosen) so a change never
 * reflows the sentence, and it is sized to the word's measured width rather than the row's.
 */
@Composable
private fun ProseWord(
    label: String,
    isSelected: Boolean,
    enabled: Boolean,
    onSelect: () -> Unit,
) {
    val wordColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
    }
    val underlineColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent

    Box(
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .alpha(if (enabled) 1f else 0.4f)
            .pointerHandCursor()
            .pressScaleClickable(onClick = { if (enabled && isSelected.not()) onSelect() })
            .semantics {
                role = Role.RadioButton
                selected = isSelected
            },
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(IntrinsicSize.Min),
        ) {
            SoftcoverText(
                text = label,
                style = MaterialTheme.editorialTypography.bodyLarge.copy(
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                ),
                color = wordColor,
            )

            Spacer(modifier = Modifier.height(1.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(underlineColor),
            )
        }
    }
}
