package nl.rhaydus.softcover.core.component.button

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
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
import nl.rhaydus.designsystem.component.DesktopTooltip
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.designsystem.modifier.pressScaleClickable
import nl.rhaydus.softcover.core.component.DISABLED_ALPHA
import nl.rhaydus.softcover.core.component.shape.PillShape
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource

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
    val style = model.tone.style(scheme = MaterialTheme.colorScheme)
    val height = PillButtonDimensions.heightFor(size = model.size)
    val isIconOnly = model.content is PillButtonContent.IconOnly
    val pillModifier = modifier
        .minimumInteractiveComponentSize()
        .pillButtonInteraction(interaction = model.interaction) {
            onEvent(PillButtonEvent.Clicked(key = model.key))
        }
        .height(height)
        .let { base -> if (isIconOnly) base.width(height) else base }
        .clip(PillShape)
        .background(style.container)
        .let { base ->
            if (style.border != null) {
                base.border(
                    width = PillButtonDimensions.borderWidth,
                    color = style.border,
                    shape = PillShape,
                )
            } else {
                base
            }
        }

    when (val content = model.content) {
        is PillButtonContent.Label -> Row(
            modifier = pillModifier.padding(horizontal = PillButtonDimensions.horizontalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            PillButtonLabel(
                content = content,
                style = style,
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
                    tint = style.iconTint,
                )
            }
        }
    }
}

internal fun PillButtonTone.style(scheme: ColorScheme): PillButtonStyle = when (this) {
    PillButtonTone.Tonal -> PillButtonStyle(
        container = scheme.surfaceContainerHigh,
        ink = scheme.onSurface,
    )

    PillButtonTone.Accent -> PillButtonStyle(
        container = scheme.surfaceContainerHigh,
        ink = scheme.primary,
    )

    PillButtonTone.Filled -> PillButtonStyle(
        container = scheme.primary,
        ink = scheme.onPrimary,
    )

    PillButtonTone.Outlined -> PillButtonStyle(
        container = Color.Transparent,
        ink = scheme.onSurface,
        iconTint = scheme.primary,
        border = scheme.outlineVariant,
    )

    PillButtonTone.Quiet -> PillButtonStyle(
        container = scheme.surfaceContainer,
        ink = scheme.onSurfaceVariant,
    )
}

@Composable
private fun Modifier.pillButtonInteraction(
    interaction: PillButtonInteraction,
    onClick: () -> Unit,
): Modifier = when (interaction) {
    PillButtonInteraction.Clickable -> this
        .pointerHandCursor()
        .pressScaleClickable(onClick = onClick)
        .semantics { role = Role.Button }

    PillButtonInteraction.Disabled -> this
        .alpha(DISABLED_ALPHA)
        .semantics(mergeDescendants = true) {
            role = Role.Button
            disabled()
        }
}

@Composable
private fun RowScope.PillButtonLabel(
    content: PillButtonContent.Label,
    style: PillButtonStyle,
) {
    content.leadingIcon?.let { icon ->
        PillButtonGlyph(
            icon = icon,
            description = null,
            tint = style.iconTint,
        )

        Spacer(modifier = Modifier.width(PillButtonDimensions.iconGap))
    }

    SoftcoverText(
        text = content.text,
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
        color = style.ink,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.weight(
            weight = 1f,
            fill = false,
        ),
    )

    if (content.indicator) {
        Spacer(modifier = Modifier.width(PillButtonDimensions.iconGap))

        Box(
            modifier = Modifier
                .size(PillButtonDimensions.indicatorSize)
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
        modifier = Modifier.size(PillButtonDimensions.iconSize),
    )
}
