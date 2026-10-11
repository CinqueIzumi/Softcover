package nl.rhaydus.softcover.core.component.button

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.component.DesktopTooltip
import nl.rhaydus.designsystem.modifier.hoverHighlight
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.designsystem.modifier.pressScale
import nl.rhaydus.softcover.core.component.DISABLED_ALPHA
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Corners
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing

/**
 * A fixed-height, fully-rounded action button. A menu it opens belongs to the caller: the pill is only
 * its anchor. An icon-only pill is a fixed circle; a label pill takes its width from [modifier], with the
 * content centred. The tap area is at least 48dp either way, wider than the painted pill.
 */
@Composable
fun PillButton(
    model: PillButtonUiModel,
    onEvent: (PillButtonEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val container = when (model.tone) {
        PillButtonTone.Tonal, PillButtonTone.Accent -> scheme.surfaceContainerHigh
        PillButtonTone.Filled -> scheme.primary
        PillButtonTone.Outlined -> Color.Transparent
        PillButtonTone.Quiet -> scheme.surfaceContainer
    }
    val ink = when (model.tone) {
        PillButtonTone.Tonal -> scheme.onSurface
        PillButtonTone.Accent -> scheme.primary
        PillButtonTone.Filled -> scheme.onPrimary
        PillButtonTone.Outlined -> scheme.onSurface
        PillButtonTone.Quiet -> scheme.onSurfaceVariant
    }
    val iconTint = if (model.tone == PillButtonTone.Outlined) scheme.primary else ink
    val height = when (model.size) {
        PillButtonSize.Regular -> 38.dp
        PillButtonSize.Large -> 44.dp
    }
    val isIconOnly = model.content is PillButtonContent.IconOnly
    val interactionSource = remember { MutableInteractionSource() }
    val pillModifier = modifier
        .minimumInteractiveComponentSize()
        .pillButtonInteraction(
            enabled = model.enabled,
            interactionSource = interactionSource,
        ) {
            onEvent(PillButtonEvent.Clicked(key = model.key))
        }
        .height(height)
        .let { base -> if (isIconOnly) base.width(height) else base }
        .clip(Corners.pill)
        .background(container)
        .let { base ->
            if (model.enabled) {
                base.hoverHighlight(
                    interactionSource = interactionSource,
                    shape = Corners.pill,
                )
            } else {
                base
            }
        }
        .let { base ->
            if (model.tone == PillButtonTone.Outlined) {
                base.border(
                    width = 1.dp,
                    color = scheme.outlineVariant,
                    shape = Corners.pill,
                )
            } else {
                base
            }
        }

    when (val content = model.content) {
        is PillButtonContent.Label -> Row(
            modifier = pillModifier.padding(horizontal = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            PillButtonLabel(
                content = content,
                ink = ink,
                iconTint = iconTint,
            )
        }

        is PillButtonContent.IconOnly -> DesktopTooltip(text = content.description) {
            Box(
                modifier = pillModifier,
                contentAlignment = Alignment.Center,
            ) {
                PillButtonGlyph(
                    icon = content.icon,
                    description = content.description,
                    tint = iconTint,
                )
            }
        }
    }
}

@Composable
private fun Modifier.pillButtonInteraction(
    enabled: Boolean,
    interactionSource: MutableInteractionSource,
    onClick: () -> Unit,
): Modifier = if (enabled) {
    this
        .pointerHandCursor()
        .pressScale(interactionSource)
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick,
        )
        .semantics { role = Role.Button }
} else {
    this
        .alpha(DISABLED_ALPHA)
        .semantics(mergeDescendants = true) {
            role = Role.Button
            disabled()
        }
}

@Composable
private fun RowScope.PillButtonLabel(
    content: PillButtonContent.Label,
    ink: Color,
    iconTint: Color,
) {
    content.leadingIcon?.let { icon ->
        PillButtonGlyph(
            icon = icon,
            description = null,
            tint = iconTint,
        )

        Spacer(modifier = Modifier.width(Spacing.xs))
    }

    SoftcoverText(
        text = content.text,
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
        color = ink,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.weight(
            weight = 1f,
            fill = false,
        ),
    )

    if (content.indicator) {
        Spacer(modifier = Modifier.width(Spacing.xs))

        Box(
            modifier = Modifier
                .size(6.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape,
                ),
        )
    }
}

@Composable
private fun PillButtonGlyph(
    icon: SoftcoverIcon,
    description: String?,
    tint: Color,
) {
    val resource = drawableIconResource(
        icon = icon,
        contentDescription = description.orEmpty(),
    )

    Icon(
        painter = resource.getIconPainter(),
        contentDescription = description?.let { resource.contentDescription },
        tint = tint,
        modifier = Modifier.size(17.dp),
    )
}
