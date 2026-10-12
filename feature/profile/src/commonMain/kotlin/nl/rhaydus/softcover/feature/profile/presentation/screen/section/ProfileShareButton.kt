package nl.rhaydus.softcover.feature.profile.presentation.screen.section

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource

@Composable
internal fun ProfileShareButton(
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
    ) {
        val icon = drawableIconResource(
            icon = SoftcoverIcon.Share,
            contentDescription = "Share your profile",
        )

        Icon(
            painter = icon.getIconPainter(),
            contentDescription = icon.contentDescription,
            tint = MaterialTheme.colorScheme.primary,
        )
    }
}
