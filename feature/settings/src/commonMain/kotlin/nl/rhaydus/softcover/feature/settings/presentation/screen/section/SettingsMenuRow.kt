package nl.rhaydus.softcover.feature.settings.presentation.screen.section

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.designsystem.modifier.pressScale
import nl.rhaydus.designsystem.motion.playDecorativeMotion
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

/**
 * The borderless, hairline-separated row shared by the mobile Settings menu and the About screen: a
 * top `outlineVariant` hairline drawn on every row (so a run reads as one continuous list, never a
 * boxed card), a 42dp `surfaceContainerHigh` icon tile holding a `primary`-tinted glyph, an Inter
 * `titleMedium` label over a one-line italic Fraunces `bodySmall` [gloss], and [accessory] as the
 * trailing element. [onClick] doubles as the row's selectability switch: `null` renders a
 * non-interactive row with [title] wrapped in a [SelectionContainer] instead of the press/hover
 * treatment, for a row whose only affordance is copying its text (the Discord-username row).
 */
@Composable
internal fun SettingsMenuRow(
    title: String,
    gloss: String,
    icon: SoftcoverIcon,
    accessory: SettingsRowAccessory,
    onClick: (() -> Unit)?,
) {
    val hairlineColor = MaterialTheme.colorScheme.outlineVariant

    val interactionModifier = if (onClick != null) {
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val playMotion = playDecorativeMotion()

        val rowBackground by animateColorAsState(
            targetValue = if (isPressed && playMotion) {
                MaterialTheme.colorScheme.surfaceContainer
            } else {
                Color.Transparent
            },
            label = "settingsMenuRowPress",
        )

        Modifier
            .pointerHandCursor()
            .pressScale(interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            )
            .background(rowBackground)
    } else {
        Modifier
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(interactionModifier)
            .drawBehind {
                drawLine(
                    color = hairlineColor,
                    start = Offset(
                        x = 0f,
                        y = 0f,
                    ),
                    end = Offset(
                        x = size.width,
                        y = 0f,
                    ),
                    strokeWidth = 1.dp.toPx(),
                )
            }
            .padding(
                horizontal = 4.dp,
                vertical = 17.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val leadingIcon = drawableIconResource(
            icon = icon,
            contentDescription = "",
        )

        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = leadingIcon.getIconPainter(),
                contentDescription = leadingIcon.contentDescription,
                modifier = Modifier.size(22.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            val titleText: @Composable () -> Unit = {
                Text(
                    text = title,
                    style = MaterialTheme.editorialTypography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            if (onClick == null) {
                SelectionContainer {
                    titleText()
                }
            } else {
                titleText()
            }

            Spacer(modifier = Modifier.height(1.dp))

            Text(
                text = gloss,
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        when (accessory) {
            SettingsRowAccessory.Chevron -> SettingsRowAccessoryIcon(
                icon = SoftcoverIcon.KeyboardArrowRight,
                contentDescription = "",
            )

            SettingsRowAccessory.External -> SettingsRowAccessoryIcon(
                icon = SoftcoverIcon.OpenInNew,
                contentDescription = "Opens in your browser",
            )

            SettingsRowAccessory.None -> Unit
        }
    }
}

@Composable
private fun SettingsRowAccessoryIcon(
    icon: SoftcoverIcon,
    contentDescription: String,
) {
    val accessoryIcon = drawableIconResource(
        icon = icon,
        contentDescription = contentDescription,
    )

    Icon(
        painter = accessoryIcon.getIconPainter(),
        contentDescription = accessoryIcon.contentDescription,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(20.dp),
    )
}
