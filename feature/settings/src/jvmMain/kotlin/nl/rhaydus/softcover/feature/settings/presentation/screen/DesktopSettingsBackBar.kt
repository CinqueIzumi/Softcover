package nl.rhaydus.softcover.feature.settings.presentation.screen

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.component.DesktopTooltip
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

/**
 * Static back bar for the standalone desktop sub-setting pages ([AppearanceSettingsScreenLayout],
 * [LibraryVisibilitySettingsScreenLayout]). Those pages are a fallback for a direct push — the
 * primary desktop entry to these settings is the [SettingsScreenLayout] master–detail pane.
 */
@Composable
internal fun DesktopSettingsBackBar(
    title: String,
    onNavigateBack: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 8.dp,
                end = 16.dp,
                top = 12.dp,
                bottom = 4.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DesktopTooltip(text = "Back") {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.pointerHandCursor(),
            ) {
                val backIcon = drawableIconResource(
                    icon = SoftcoverIcon.ArrowBack,
                    contentDescription = "Navigate back",
                )

                Icon(
                    painter = backIcon.getIconPainter(),
                    contentDescription = backIcon.contentDescription,
                )
            }
        }

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = title,
            style = MaterialTheme.editorialTypography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
