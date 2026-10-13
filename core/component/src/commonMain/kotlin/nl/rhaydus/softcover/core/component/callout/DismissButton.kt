package nl.rhaydus.softcover.core.component.callout

import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.component.chrome.ChromeIconButton
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon

/** The close glyph [Banner] and [Callout] share, tinted [contentColor]. */
@Composable
internal fun DismissButton(
    contentColor: Color,
    onClick: () -> Unit,
) {
    ChromeIconButton(
        icon = SoftcoverIcon.Close,
        contentDescription = "Dismiss",
        tooltip = "Dismiss",
        onClick = onClick,
        colors = IconButtonDefaults.iconButtonColors(contentColor = contentColor),
        iconSize = 16.dp,
    )
}
