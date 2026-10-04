package nl.rhaydus.softcover.core.component.button

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource

private val PillShape = RoundedCornerShape(percent = 50)

/**
 * A fixed-height, fully-rounded action button. A menu it opens belongs to the caller: the pill is only
 * its anchor. Width comes from the caller's [modifier]; the content stays centred.
 */
@Composable
fun PillButton(
    model: PillButtonUiModel,
    onEvent: (PillButtonEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val style = pillButtonStyleFor(tone = model.tone)
    val height = PillButtonDimensions.heightFor(size = model.size)
    val isIconOnly = model.content is PillButtonContent.IconOnly
    val pillModifier = modifier
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
        .pillButtonInteraction(interaction = model.interaction) {
            onEvent(PillButtonEvent.Clicked(key = model.key))
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
                indicator = model.indicator,
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

@Composable
private fun pillButtonStyleFor(tone: PillButtonTone): PillButtonStyle = when (tone) {
    PillButtonTone.Tonal -> PillButtonStyle(
        container = MaterialTheme.colorScheme.surfaceContainerHigh,
        ink = MaterialTheme.colorScheme.onSurface,
    )

    PillButtonTone.Accent -> PillButtonStyle(
        container = MaterialTheme.colorScheme.surfaceContainerHigh,
        ink = MaterialTheme.colorScheme.primary,
    )

    PillButtonTone.Filled -> PillButtonStyle(
        container = MaterialTheme.colorScheme.primary,
        ink = MaterialTheme.colorScheme.onPrimary,
    )

    PillButtonTone.Outlined -> PillButtonStyle(
        container = Color.Transparent,
        ink = MaterialTheme.colorScheme.onSurface,
        iconTint = MaterialTheme.colorScheme.primary,
        border = MaterialTheme.colorScheme.outlineVariant,
    )

    PillButtonTone.Quiet -> PillButtonStyle(
        container = MaterialTheme.colorScheme.surfaceContainer,
        ink = MaterialTheme.colorScheme.onSurfaceVariant,
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
        .alpha(PillButtonDimensions.DISABLED_ALPHA)
        .semantics(mergeDescendants = true) {
            role = Role.Button
            disabled()
        }
}

@Composable
private fun PillButtonLabel(
    content: PillButtonContent.Label,
    style: PillButtonStyle,
    indicator: Boolean,
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
    )

    if (indicator) {
        Spacer(modifier = Modifier.width(PillButtonDimensions.iconGap))

        Box(
            modifier = Modifier
                .size(PillButtonDimensions.indicatorSize)
                .background(
                    color = MaterialTheme.colorScheme.inversePrimary,
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
