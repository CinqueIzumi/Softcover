package nl.rhaydus.softcover.core.component.topbar

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.component.DesktopTooltip
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

/**
 * The desktop page bar: a back button, a start-aligned editorial headline, and a trailing slot for
 * the screen's own actions. Always leads back out, so unlike [TopBar] it has no navigation switch.
 */
@Composable
fun EditorialTopBar(
    model: EditorialTopBarUiModel,
    onEvent: (EditorialTopBarEvent) -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = 8.dp,
                end = 8.dp,
                top = 12.dp,
                bottom = 4.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DesktopTooltip(text = "Back") {
            IconButton(
                onClick = { onEvent(EditorialTopBarEvent.BackClicked) },
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

        model.title?.let { title ->
            Spacer(modifier = Modifier.width(4.dp))

            SoftcoverText(
                text = title,
                style = MaterialTheme.editorialTypography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        actions()
    }
}
