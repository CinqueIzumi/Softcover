package nl.rhaydus.softcover.core.component.chip

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.haptics.LocalHaptics
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.designsystem.modifier.pressScaleClickable
import nl.rhaydus.softcover.core.component.DISABLED_ALPHA
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Corners
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.spoilerEditorHighlight

/**
 * The pill-shaped chip family: one label on a fully-rounded
 * surface, its colours resolved from [ChipUiModel.tone] and [ChipUiModel.selected] through one
 * [ChipScaffold]. [ChipUiModel.interaction] gates the tap affordance independently of the tone.
 * Every clickable pill reports through this one [ChipEvent] lambda using the same press-scale +
 * hand-cursor affordance, never a `Surface(onClick)` ripple.
 */
@Composable
fun Chip(
    model: ChipUiModel,
    onEvent: (ChipEvent) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    ChipScaffold(
        model = model,
        style = model.tone.style(
            scheme = MaterialTheme.colorScheme,
            selected = model.selected,
        ),
        onEvent = onEvent,
        modifier = modifier,
    )
}

@Composable
private fun ChipScaffold(
    model: ChipUiModel,
    style: ChipStyle,
    onEvent: (ChipEvent) -> Unit,
    modifier: Modifier,
) {
    val haptics = LocalHaptics.current
    val colorSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Color>()
    val container by animateColorAsState(
        targetValue = style.container,
        animationSpec = colorSpec,
        label = "ChipContainer",
    )
    val ink by animateColorAsState(
        targetValue = style.ink,
        animationSpec = colorSpec,
        label = "ChipInk",
    )
    val animatedStyle = style.copy(
        container = container,
        ink = ink,
    )
    val selectable = model.tone == ChipTone.Tonal || model.tone == ChipTone.Choice

    val rowModifier = modifier
        .clip(Corners.pill)
        .background(animatedStyle.container)
        .thenChipBorder(
            tone = model.tone,
            scheme = MaterialTheme.colorScheme,
        )
        .then(if (selectable) Modifier.semantics { selected = model.selected } else Modifier)
        .chipInteraction(interaction = model.interaction) {
            if (selectable) {
                haptics.select()
            }

            onEvent(ChipEvent.Clicked(key = model.key))
        }
        .then(
            if (model.tone == ChipTone.Spoiler) {
                Modifier.clearAndSetSemantics {
                    contentDescription = "Spoiler, tap to reveal"
                    role = Role.Button
                }
            } else {
                Modifier
            },
        )
        .padding(
            when (model.size) {
                ChipSize.Regular -> PaddingValues(
                    horizontal = Spacing.sm,
                    vertical = Spacing.xs,
                )
                ChipSize.Compact -> PaddingValues(
                    horizontal = Spacing.xs,
                    vertical = Spacing.xxs,
                )
            },
        )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = rowModifier,
    ) {
        ChipLeadingSlot(
            leading = model.leading,
            style = animatedStyle,
            onToggle = { onEvent(ChipEvent.SpoilerToggled(key = model.key)) },
        )

        ChipLabel(
            model = model,
            style = animatedStyle,
        )

        ChipTrailingSlot(
            trailing = model.trailing,
            style = animatedStyle,
            onDismiss = { onEvent(ChipEvent.Dismissed(key = model.key)) },
        )
    }
}

private fun Modifier.thenChipBorder(
    tone: ChipTone,
    scheme: ColorScheme,
): Modifier = when (tone) {
    ChipTone.Outlined -> this.border(
        width = 1.dp,
        color = scheme.outlineVariant,
        shape = Corners.pill,
    )
    ChipTone.Dashed -> this.drawBehind {
        drawRoundRect(
            color = scheme.primary,
            style = Stroke(
                width = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(intervals = floatArrayOf(6.dp.toPx(), 4.dp.toPx())),
            ),
            cornerRadius = CornerRadius(size.height / 2f),
        )
    }
    else -> this
}

@Composable
private fun Modifier.chipInteraction(
    interaction: ChipInteraction,
    onClick: () -> Unit,
): Modifier = when (interaction) {
    ChipInteraction.Clickable -> pointerHandCursor().pressScaleClickable(onClick = onClick)
    ChipInteraction.Disabled -> alpha(DISABLED_ALPHA)
    ChipInteraction.Inert -> this
}

@Composable
private fun ChipLeadingSlot(
    leading: ChipLeading?,
    style: ChipStyle,
    onToggle: () -> Unit,
) {
    when (leading) {
        null -> Unit

        is ChipLeading.Icon -> {
            ChipIconSlot(
                icon = leading.icon,
                contentDescription = null,
                tint = style.ink,
                size = 18.dp,
                onClick = null,
            )
            Spacer(modifier = Modifier.width(Spacing.xs))
        }

        is ChipLeading.SpoilerToggle -> {
            ChipIconSlot(
                icon = if (leading.marked) SoftcoverIcon.VisibilityOff else SoftcoverIcon.Visibility,
                contentDescription = leading.label,
                tint = if (leading.marked) MaterialTheme.colorScheme.primary else style.ink,
                size = 18.dp,
                onClick = onToggle,
            )
            Spacer(modifier = Modifier.width(Spacing.xs))
        }
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

    SoftcoverText(
        text = model.label,
        style = textStyle,
        color = style.ink,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = if (spoilerWash) {
            Modifier
                .clip(Corners.xxs)
                .background(MaterialTheme.colorScheme.spoilerEditorHighlight)
                .padding(
                    horizontal = Spacing.xxxs,
                    vertical = Spacing.xxxs,
                )
        } else {
            Modifier
        },
    )
}

@Composable
private fun ChipTrailingSlot(
    trailing: ChipTrailing?,
    style: ChipStyle,
    onDismiss: () -> Unit,
) {
    when (trailing) {
        null -> Unit

        is ChipTrailing.Icon -> {
            Spacer(modifier = Modifier.width(Spacing.xs))
            ChipIconSlot(
                icon = trailing.icon,
                contentDescription = trailing.description,
                tint = style.ink,
                size = 18.dp,
                onClick = null,
            )
        }

        is ChipTrailing.Dismiss -> {
            Spacer(modifier = Modifier.width(Spacing.xs))
            ChipIconSlot(
                icon = SoftcoverIcon.Close,
                contentDescription = trailing.label,
                tint = style.ink,
                size = 16.dp,
                onClick = onDismiss,
            )
        }
    }
}

@Composable
private fun ChipIconSlot(
    icon: SoftcoverIcon,
    contentDescription: String?,
    tint: Color,
    size: Dp,
    onClick: (() -> Unit)?,
) {
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
}
