package nl.rhaydus.softcover.orchestration.presentation

import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.softcover.core.component.text.SoftcoverText

/**
 * The medium-width navigation chrome: a vertical rail that replaces the bottom bar between 600 and
 * 840dp. Renders the same destinations, selection, badge, and icon pulse as the bottom bars by
 * consuming [rememberNavItems].
 */
@Composable
internal fun NavigationRailBar(modifier: Modifier = Modifier) {
    val navItems = rememberNavItems()
    val onNavItemClick = rememberNavItemClick()

    NavigationRail(modifier = modifier) {
        navItems.forEach { item ->
            val iconPainter = item.tab.options.icon ?: return@forEach

            NavigationRailItem(
                selected = item.selected,
                onClick = { onNavItemClick(item) },
                icon = {
                    NavItemIcon(
                        item = item,
                        painter = iconPainter,
                    )
                },
                label = { SoftcoverText(text = item.tab.options.title) },
            )
        }
    }
}
