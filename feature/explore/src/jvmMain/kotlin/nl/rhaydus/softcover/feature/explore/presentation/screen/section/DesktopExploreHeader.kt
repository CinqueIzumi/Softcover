package nl.rhaydus.softcover.feature.explore.presentation.screen.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.component.DesktopTooltip
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

@Composable
internal fun DesktopExploreHeader(
    isRefreshing: Boolean,
    onRefreshClick: () -> Unit,
    onScanClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 12.dp, top = 16.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (isRefreshing) "Finding fresh picks…" else "Discover",
                style = MaterialTheme.editorialTypography.eyebrow,
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Explore",
                style = MaterialTheme.editorialTypography.pageTitle,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(end = 16.dp),
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Trending books, the next in your series, and anything you search for.",
                style = MaterialTheme.editorialTypography.body,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 16.dp),
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            TextButton(
                onClick = onRefreshClick,
                enabled = isRefreshing.not(),
                modifier = Modifier.pointerHandCursor(),
            ) {
                Text(text = if (isRefreshing) "Refreshing…" else "Refresh")
            }

            DesktopTooltip(text = "Scan a barcode") {
                IconButton(
                    onClick = onScanClick,
                    modifier = Modifier.pointerHandCursor(),
                ) {
                    val scanIcon = drawableIconResource(
                        icon = SoftcoverIcon.BarcodeScanner,
                        contentDescription = "Scan a book's barcode",
                    )

                    Icon(
                        painter = scanIcon.getIconPainter(),
                        contentDescription = scanIcon.contentDescription,
                    )
                }
            }
        }
    }
}
