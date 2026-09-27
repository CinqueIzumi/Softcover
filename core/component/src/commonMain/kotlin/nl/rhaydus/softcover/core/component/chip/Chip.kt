package nl.rhaydus.softcover.core.component.chip

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.designsystem.modifier.pressScaleClickable
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.spoilerCover
import nl.rhaydus.softcover.core.designsystem.presentation.theme.spoilerEditorHighlight

private val ChipShape = RoundedCornerShape(percent = 50)
private val DashLength = 6.dp
private val DashGap = 4.dp

/**
 * The pill-shaped chip family (`component-contract.md` § 7.2 R2): one label on a fully-rounded
 * surface, its colours resolved from [ChipUiModel.tone] and [ChipUiModel.selected] through one
 * [ChipScaffold]. [ChipUiModel.interaction] gates the tap affordance independently of the tone.
 * Every clickable pill reports through this one [ChipEvent] lambda (R1) using the same press-scale +
 * hand-cursor affordance as `WhenReadRow` and `ChooseListsRow`, never a `Surface(onClick)` ripple.
 */
@Composable
fun Chip(
    model: ChipUiModel,
    onEvent: (ChipEvent) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    ChipScaffold(
        model = model,
        style = chipStyleFor(
            tone = model.tone,
            selected = model.selected,
        ),
        onEvent = onEvent,
        modifier = modifier,
    )
}

@Composable
private fun chipStyleFor(
    tone: ChipTone,
    selected: Boolean,
): ChipStyle = when (tone) {
    ChipTone.Tonal -> tonalStyle(selected = selected)
    ChipTone.Choice -> if (selected) {
        ChipStyle(
            container = MaterialTheme.colorScheme.primary,
            ink = MaterialTheme.colorScheme.onPrimary,
            weight = FontWeight.SemiBold,
        )
    } else {
        tonalStyle(selected = false)
    }

    ChipTone.Filled -> ChipStyle(
        container = MaterialTheme.colorScheme.primary,
        ink = MaterialTheme.colorScheme.onPrimary,
        weight = FontWeight.SemiBold,
    )

    ChipTone.Container -> ChipStyle(
        container = MaterialTheme.colorScheme.primaryContainer,
        ink = MaterialTheme.colorScheme.onPrimaryContainer,
        weight = FontWeight.SemiBold,
    )

    ChipTone.Outlined -> ChipStyle(
        container = Color.Transparent,
        ink = MaterialTheme.colorScheme.onSurfaceVariant,
        weight = FontWeight.Medium,
        border = ChipBorder.Solid(color = MaterialTheme.colorScheme.outlineVariant),
        mutedInk = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    ChipTone.Dashed -> ChipStyle(
        container = Color.Transparent,
        ink = MaterialTheme.colorScheme.primary,
        weight = FontWeight.SemiBold,
        border = ChipBorder.Dashed(color = MaterialTheme.colorScheme.primary),
    )

    ChipTone.Spoiler -> ChipStyle(
        container = MaterialTheme.colorScheme.spoilerCover,
        ink = Color.Transparent,
        weight = FontWeight.Medium,
    )
}

@Composable
private fun tonalStyle(selected: Boolean): ChipStyle = if (selected) {
    ChipStyle(
        container = MaterialTheme.colorScheme.secondaryContainer,
        ink = MaterialTheme.colorScheme.onSecondaryContainer,
        weight = FontWeight.SemiBold,
    )
} else {
    ChipStyle(
        container = MaterialTheme.colorScheme.surfaceContainerHigh,
        ink = MaterialTheme.colorScheme.onSurface,
        weight = FontWeight.Medium,
        mutedInk = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun ChipScaffold(
    model: ChipUiModel,
    style: ChipStyle,
    onEvent: (ChipEvent) -> Unit,
    modifier: Modifier,
) {
    val tappable = model.tone != ChipTone.Spoiler

    val rowModifier = modifier
        .clip(ChipShape)
        .background(style.container)
        .thenChipBorder(border = style.border)
        .chipInteraction(interaction = model.interaction) {
            onEvent(ChipEvent.Clicked(key = model.key))
        }
        .padding(ChipDimensions.paddingFor(size = model.size))

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = rowModifier,
    ) {
        ChipLeadingSlot(
            leading = model.leading,
            style = style,
            tappable = tappable,
            onToggle = { onEvent(ChipEvent.SpoilerToggled(key = model.key)) },
        )

        ChipLabel(
            model = model,
            style = style,
        )

        ChipTrailingSlot(
            trailing = model.trailing,
            style = style,
            tappable = tappable,
            onDismiss = { onEvent(ChipEvent.Dismissed(key = model.key)) },
        )
    }
}

private fun Modifier.thenChipBorder(border: ChipBorder): Modifier = when (border) {
    ChipBorder.None -> this
    is ChipBorder.Solid -> this.border(
        width = ChipDimensions.borderWidth,
        color = border.color,
        shape = ChipShape,
    )
    is ChipBorder.Dashed -> this.drawBehind {
        drawRoundRect(
            color = border.color,
            style = Stroke(
                width = ChipDimensions.borderWidth.toPx(),
                pathEffect = PathEffect.dashPathEffect(intervals = floatArrayOf(DashLength.toPx(), DashGap.toPx())),
            ),
            cornerRadius = CornerRadius(size.height / 2f),
        )
    }
}

@Composable
private fun Modifier.chipInteraction(
    interaction: ChipInteraction,
    onClick: () -> Unit,
): Modifier = when (interaction) {
    ChipInteraction.Clickable -> pointerHandCursor().pressScaleClickable(onClick = onClick)
    ChipInteraction.Disabled -> alpha(ChipDimensions.disabledAlpha)
    ChipInteraction.Inert -> this
}

@Composable
private fun ChipLeadingSlot(
    leading: ChipLeading?,
    style: ChipStyle,
    tappable: Boolean,
    onToggle: () -> Unit,
) {
    when (leading) {
        null -> Unit

        is ChipLeading.Icon -> ChipIconSlot(
            icon = leading.icon,
            contentDescription = leading.description,
            tint = style.mutedInk,
            size = ChipDimensions.iconSize,
            gap = ChipDimensions.iconGap,
            gapPosition = ChipGapPosition.After,
            onClick = null,
        )

        is ChipLeading.SpoilerToggle -> ChipIconSlot(
            icon = if (leading.marked) SoftcoverIcon.VisibilityOff else SoftcoverIcon.Visibility,
            contentDescription = leading.label,
            tint = if (leading.marked) MaterialTheme.colorScheme.primary else style.mutedInk,
            size = ChipDimensions.iconSize,
            gap = ChipDimensions.iconGap,
            gapPosition = ChipGapPosition.After,
            onClick = if (tappable) onToggle else null,
        )
    }
}

@Composable
private fun ChipLabel(
    model: ChipUiModel,
    style: ChipStyle,
) {
    val textStyle = MaterialTheme.typography.labelMedium.copy(
        fontWeight = if (model.face == ChipFace.Bold) FontWeight.Bold else style.weight,
        fontStyle = if (model.face == ChipFace.Italic) FontStyle.Italic else FontStyle.Normal,
    )
    val spoilerWash = (model.leading as? ChipLeading.SpoilerToggle)?.marked == true

    Text(
        text = model.label,
        style = textStyle,
        color = style.ink,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = if (spoilerWash) {
            Modifier
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.spoilerEditorHighlight)
                .padding(horizontal = 3.dp, vertical = 1.dp)
        } else {
            Modifier
        },
    )
}

@Composable
private fun ChipTrailingSlot(
    trailing: ChipTrailing?,
    style: ChipStyle,
    tappable: Boolean,
    onDismiss: () -> Unit,
) {
    when (trailing) {
        null -> Unit

        is ChipTrailing.Icon -> ChipIconSlot(
            icon = trailing.icon,
            contentDescription = trailing.description,
            tint = style.mutedInk,
            size = ChipDimensions.iconSize,
            gap = ChipDimensions.iconGap,
            gapPosition = ChipGapPosition.Before,
            onClick = null,
        )

        is ChipTrailing.Dismiss -> ChipIconSlot(
            icon = SoftcoverIcon.Close,
            contentDescription = trailing.label,
            tint = style.mutedInk,
            size = ChipDimensions.dismissIconSize,
            gap = ChipDimensions.dismissIconGap,
            gapPosition = ChipGapPosition.Before,
            onClick = if (tappable) onDismiss else null,
        )
    }
}

private enum class ChipGapPosition { Before, After }

@Composable
private fun ChipIconSlot(
    icon: SoftcoverIcon,
    contentDescription: String?,
    tint: Color,
    size: Dp,
    gap: Dp,
    gapPosition: ChipGapPosition,
    onClick: (() -> Unit)?,
) {
    if (gapPosition == ChipGapPosition.Before) {
        Spacer(modifier = Modifier.width(gap))
    }

    val resource = drawableIconResource(
        icon = icon,
        contentDescription = contentDescription.orEmpty(),
    )

    Icon(
        painter = resource.getIconPainter(),
        contentDescription = resource.contentDescription,
        tint = tint,
        modifier = Modifier
            .size(size)
            .let { base ->
                if (onClick != null) base.pointerHandCursor().pressScaleClickable(onClick = onClick) else base
            },
    )

    if (gapPosition == ChipGapPosition.After) {
        Spacer(modifier = Modifier.width(gap))
    }
}
