package nl.rhaydus.softcover.orchestration.presentation

import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator

/**
 * The medium-width navigation chrome: a vertical rail that replaces the bottom bar between 600 and
 * 840dp. Renders the same destinations, selection, badge, and library pulse as the bottom bars by
 * consuming [rememberNavItems].
 */
@Composable
internal fun NavigationRailBar(modifier: Modifier = Modifier) {
    val navItems = rememberNavItems()
    val tabNavigator = LocalTabNavigator.current

    NavigationRail(modifier = modifier) {
        navItems.forEach { item ->
            val iconPainter = item.tab.options.icon ?: return@forEach

            NavigationRailItem(
                selected = item.selected,
                onClick = { tabNavigator.current = item.tab },
                icon = {
                    NavItemIcon(
                        item = item,
                        painter = iconPainter,
                    )
                },
                label = { Text(text = item.tab.options.title) },
            )
        }
    }
}
