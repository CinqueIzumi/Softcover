package nl.rhaydus.softcover.orchestration.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

private val SIDEBAR_WIDTH = 240.dp
private val SIDEBAR_ITEM_SHAPE = RoundedCornerShape(16.dp)

/**
 * The expanded-width navigation chrome (`>= 840dp`): a permanent editorial sidebar that replaces
 * the navigation rail. The app wordmark sits in the editorial voice over the four destinations,
 * each a selectable row that mirrors the app's selected-chip colours. Renders the same selection,
 * badge, and library pulse as every other chrome by consuming [rememberNavItems].
 */
@Composable
internal fun EditorialSidebar(modifier: Modifier = Modifier) {
    val navItems = rememberNavItems()
    val tabNavigator = LocalTabNavigator.current

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
            .fillMaxHeight()
            .width(SIDEBAR_WIDTH),
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(
                    horizontal = 12.dp,
                    vertical = 24.dp,
                ),
        ) {
            Text(
                text = "Softcover",
                style = MaterialTheme.editorialTypography.pageTitle,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(
                    start = 12.dp,
                    bottom = 24.dp,
                ),
            )

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                navItems.forEach { item ->
                    val iconPainter = item.tab.options.icon ?: return@forEach

                    SidebarItem(
                        item = item,
                        painter = iconPainter,
                        onClick = { tabNavigator.current = item.tab },
                    )
                }
            }
        }
    }
}

@Composable
private fun SidebarItem(
    item: NavItemUi,
    painter: Painter,
    onClick: () -> Unit,
) {
    val containerColor = if (item.selected) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        Color.Transparent
    }

    val contentColor = if (item.selected) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(SIDEBAR_ITEM_SHAPE)
            .background(containerColor)
            .clickable(onClick = onClick)
            .padding(
                horizontal = 16.dp,
                vertical = 12.dp,
            ),
    ) {
        BadgedBox(
            badge = {
                if (item.showBadge) {
                    Badge(containerColor = MaterialTheme.colorScheme.inversePrimary)
                }
            },
        ) {
            Icon(
                painter = painter,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.graphicsLayer {
                    scaleX = item.iconScale
                    scaleY = item.iconScale
                },
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = item.tab.options.title,
            style = MaterialTheme.editorialTypography.titleMedium,
            color = contentColor,
        )
    }
}
