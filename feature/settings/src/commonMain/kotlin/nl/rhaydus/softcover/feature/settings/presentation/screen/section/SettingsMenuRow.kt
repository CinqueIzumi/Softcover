package nl.rhaydus.softcover.feature.settings.presentation.screen.section

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource

/**
 * A borderless [SettingsRow] shared by the mobile Settings menu and the About screen: a 42dp
 * `surfaceContainerHigh` icon tile holding a `primary`-tinted glyph, [title] over [gloss], and
 * [target]'s trailing affordance.
 */
@Composable
internal fun SettingsMenuRow(
    title: String,
    gloss: String,
    icon: SoftcoverIcon,
    target: SettingsMenuRowTarget,
) {
    val leading: @Composable () -> Unit = {
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
    }

    when (target) {
        is SettingsMenuRowTarget.SubPage -> ClickableMenuRow(
            title = title,
            gloss = gloss,
            leading = leading,
            onClick = target.onClick,
            trailingIcon = SoftcoverIcon.KeyboardArrowRight,
            trailingContentDescription = "",
        )

        is SettingsMenuRowTarget.ExternalLink -> ClickableMenuRow(
            title = title,
            gloss = gloss,
            leading = leading,
            onClick = target.onClick,
            trailingIcon = SoftcoverIcon.OpenInNew,
            trailingContentDescription = "Opens in your browser",
        )

        SettingsMenuRowTarget.Copyable -> SettingsRow(
            title = title,
            secondary = gloss,
            leading = leading,
            selectionContainer = true,
        )
    }
}

@Composable
private fun ClickableMenuRow(
    title: String,
    gloss: String,
    leading: @Composable () -> Unit,
    onClick: () -> Unit,
    trailingIcon: SoftcoverIcon,
    trailingContentDescription: String,
) {
    val interactionSource = remember { MutableInteractionSource() }

    SettingsRow(
        title = title,
        secondary = gloss,
        leading = leading,
        trailing = {
            SettingsRowAccessoryIcon(
                icon = trailingIcon,
                contentDescription = trailingContentDescription,
            )
        },
        interactionSource = interactionSource,
        semantics = Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            role = Role.Button,
            onClick = onClick,
        ),
    )
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
