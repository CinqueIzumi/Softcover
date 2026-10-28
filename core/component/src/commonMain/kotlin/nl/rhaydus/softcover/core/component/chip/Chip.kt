package nl.rhaydus.softcover.core.component.chip

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.motion.playDecorativeMotion
import nl.rhaydus.softcover.core.component.DISABLED_ALPHA
import nl.rhaydus.softcover.core.component.modifier.tactileClickable
import nl.rhaydus.softcover.core.component.modifier.tactileToggleable
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Corners
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
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
        .chipInteraction(
            interaction = model.interaction,
            selectable = selectable,
            selected = model.selected,
            tentative = model.tone == ChipTone.TonalDashed,
        ) {
            onEvent(ChipEvent.Clicked(key = model.key))
        }
        .then(
            if (model.contentDescription != null) {
                Modifier.semantics { contentDescription = model.contentDescription }
            } else {
                Modifier
            },
        )
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
        .height(IntrinsicSize.Min)

    val padding = when (model.size) {
        ChipSize.Regular -> PaddingValues(
            horizontal = Spacing.sm,
            vertical = Spacing.xs,
        )
        ChipSize.Compact -> PaddingValues(
            horizontal = Spacing.xs,
            vertical = Spacing.xxs,
        )
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = rowModifier,
    ) {
        ChipLeadingSlot(
            leading = model.leading,
            style = animatedStyle,
            padding = padding,
            onToggle = { onEvent(ChipEvent.SpoilerToggled(key = model.key)) },
        )

        ChipLabel(
            model = model,
            style = animatedStyle,
            padding = padding,
        )

        ChipTrailingSlot(
            trailing = model.trailing,
            style = animatedStyle,
            padding = padding,
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
    ChipTone.Excluded -> this.border(
        width = 1.dp,
        color = scheme.outline,
        shape = Corners.pill,
    )
    ChipTone.Dashed, ChipTone.TonalDashed -> this.drawBehind {
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
    selectable: Boolean,
    selected: Boolean,
    tentative: Boolean,
    onClick: () -> Unit,
): Modifier = when (interaction) {
    ChipInteraction.Inert -> when {
        selectable -> semantics { this.selected = selected }

        tentative -> semantics { toggleableState = ToggleableState.Indeterminate }

        else -> this
    }
    ChipInteraction.Clickable, ChipInteraction.Disabled -> {
        val enabled = interaction == ChipInteraction.Clickable

        this
            .let { base -> if (enabled) base else base.alpha(DISABLED_ALPHA) }
            .let { base ->
                if (selectable) {
                    base.tactileToggleable(
                        value = selected,
                        onValueChange = { onClick() },
                        role = Role.Checkbox,
                        enabled = enabled,
                        hoverShape = Corners.pill,
                    )
                } else if (tentative) {
                    base
                        .tactileClickable(
                            onClick = onClick,
                            role = Role.Checkbox,
                            enabled = enabled,
                            hoverShape = Corners.pill,
                        )
                        .semantics { toggleableState = ToggleableState.Indeterminate }
                } else {
                    base.tactileClickable(
                        onClick = onClick,
                        enabled = enabled,
                        hoverShape = Corners.pill,
                    )
                }
            }
    }
}

@Composable
private fun ChipLeadingSlot(
    leading: ChipLeading?,
    style: ChipStyle,
    padding: PaddingValues,
    onToggle: () -> Unit,
) {
    val start = padding.calculateLeftPadding(LayoutDirection.Ltr)

    when (leading) {
        null -> Spacer(modifier = Modifier.width(start))

        is ChipLeading.Icon -> {
            ChipIconSlot(
                icon = leading.icon,
                contentDescription = null,
                tint = style.ink,
                size = 18.dp,
                insets = PaddingValues(
                    start = start,
                    top = padding.calculateTopPadding(),
                    end = Spacing.xs,
                    bottom = padding.calculateBottomPadding(),
                ),
                onClick = null,
            )
        }

        is ChipLeading.SpoilerToggle -> {
            ChipIconSlot(
                icon = if (leading.marked) SoftcoverIcon.VisibilityOff else SoftcoverIcon.Visibility,
                contentDescription = leading.label,
                tint = if (leading.marked) MaterialTheme.colorScheme.primary else style.ink,
                size = 18.dp,
                insets = PaddingValues(
                    start = start,
                    top = padding.calculateTopPadding(),
                    end = Spacing.xs,
                    bottom = padding.calculateBottomPadding(),
                ),
                onClick = onToggle,
            )
        }
    }
}

@Composable
private fun ChipLabel(
    model: ChipUiModel,
    style: ChipStyle,
    padding: PaddingValues,
) {
    val eyebrow = model.face == ChipFace.Eyebrow
    val textStyle = if (eyebrow) {
        MaterialTheme.editorialTypography.eyebrowSmall
    } else {
        MaterialTheme.typography.labelMedium.copy(
            fontWeight = if (model.face == ChipFace.Bold) FontWeight.Bold else style.weight,
            fontStyle = if (model.face == ChipFace.Italic) FontStyle.Italic else FontStyle.Normal,
            textDecoration = if (model.tone == ChipTone.Excluded) TextDecoration.LineThrough else null,
        )
    }
    val spoilerWash = (model.leading as? ChipLeading.SpoilerToggle)?.marked == true

    SoftcoverText(
        text = if (eyebrow) model.label.uppercase() else model.label,
        style = textStyle,
        color = style.ink,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = if (spoilerWash) {
            Modifier
                .padding(vertical = padding.calculateTopPadding())
                .clip(Corners.xxs)
                .background(MaterialTheme.colorScheme.spoilerEditorHighlight)
                .padding(
                    horizontal = Spacing.xxxs,
                    vertical = Spacing.xxxs,
                )
        } else {
            Modifier.padding(vertical = padding.calculateTopPadding())
        },
    )
}

@Composable
private fun ChipTrailingSlot(
    trailing: ChipTrailing?,
    style: ChipStyle,
    padding: PaddingValues,
    onDismiss: () -> Unit,
) {
    val end = padding.calculateRightPadding(LayoutDirection.Ltr)
    val insets = PaddingValues(
        start = Spacing.xs,
        top = padding.calculateTopPadding(),
        end = end,
        bottom = padding.calculateBottomPadding(),
    )

    when (trailing) {
        null -> Spacer(modifier = Modifier.width(end))

        is ChipTrailing.Icon -> {
            ChipIconSlot(
                icon = trailing.icon,
                contentDescription = trailing.description,
                tint = style.ink,
                size = 18.dp,
                insets = insets,
                onClick = null,
            )
        }

        is ChipTrailing.Disclosure -> {
            val playMotion = playDecorativeMotion()
            val spatialSpec = MaterialTheme.motionScheme.defaultSpatialSpec<Float>()
            val rotation by animateFloatAsState(
                targetValue = if (trailing.expanded) 180f else 0f,
                animationSpec = if (playMotion) spatialSpec else snap(),
                label = "ChipDisclosureRotation",
            )

            ChipIconSlot(
                icon = SoftcoverIcon.ArrowDropDown,
                contentDescription = trailing.label,
                tint = style.ink,
                size = 18.dp,
                insets = insets,
                onClick = null,
                rotation = rotation,
            )
        }

        is ChipTrailing.Dismiss -> {
            ChipIconSlot(
                icon = SoftcoverIcon.Close,
                contentDescription = trailing.label,
                tint = style.ink,
                size = 16.dp,
                insets = insets,
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
    insets: PaddingValues,
    onClick: (() -> Unit)?,
    rotation: Float = 0f,
) {
    val resource = drawableIconResource(
        icon = icon,
        contentDescription = contentDescription.orEmpty(),
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .let { base ->
                if (onClick != null) {
                    base
                        .fillMaxHeight()
                        .tactileClickable(
                            onClick = onClick,
                            hoverShape = Corners.pill,
                        )
                } else {
                    base
                }
            }
            .padding(insets),
    ) {
        Icon(
            painter = resource.getIconPainter(),
            contentDescription = resource.contentDescription,
            tint = tint,
            modifier = Modifier
                .size(size)
                .graphicsLayer { rotationZ = rotation },
        )
    }
}
