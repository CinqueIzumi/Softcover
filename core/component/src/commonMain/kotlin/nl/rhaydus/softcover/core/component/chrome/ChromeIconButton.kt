package nl.rhaydus.softcover.core.component.chrome

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.component.DesktopTooltip
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource

/**
 * The icon-only button of a bar or strip.
 *
 * Its 48 dp interactive size is a minimum that a parent's smaller max constraint silently clamps, so
 * the container it sits in must leave it 48 dp; [iconSize] only sizes the glyph.
 */
@Composable
internal fun ChromeIconButton(
    icon: SoftcoverIcon,
    contentDescription: String,
    tooltip: String,
    onClick: () -> Unit,
    colors: IconButtonColors = IconButtonDefaults.iconButtonColors(),
    iconSize: Dp = 24.dp,
) {
    DesktopTooltip(text = tooltip) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.pointerHandCursor(),
            colors = colors,
        ) {
            val iconResource = drawableIconResource(
                icon = icon,
                contentDescription = contentDescription,
            )

            Icon(
                painter = iconResource.getIconPainter(),
                contentDescription = iconResource.contentDescription,
                modifier = Modifier.size(iconSize),
            )
        }
    }
}
