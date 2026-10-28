package nl.rhaydus.softcover.feature.profile.presentation.screen.section

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource

@Composable
internal fun TrailingChevron(tint: Color) {
    val chevron = drawableIconResource(
        icon = SoftcoverIcon.KeyboardArrowRight,
        contentDescription = "",
    )

    Icon(
        painter = chevron.getIconPainter(),
        contentDescription = chevron.contentDescription,
        tint = tint,
    )
}
