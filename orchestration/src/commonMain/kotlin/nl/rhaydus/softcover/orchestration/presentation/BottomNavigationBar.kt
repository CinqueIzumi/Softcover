package nl.rhaydus.softcover.orchestration.presentation

import nl.rhaydus.designsystem.icon.RhaydusIconResource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import nl.rhaydus.softcover.core.designsystem.presentation.component.SoftcoverIconToggleButton
import nl.rhaydus.softcover.core.designsystem.presentation.model.IconToggleButtonStyle

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun DockedBottomNavigationBar() {
    val navItems = rememberNavItems()
    val tabNavigator = LocalTabNavigator.current

    NavigationBar {
        navItems.forEach { item ->
            val iconPainter = item.tab.options.icon ?: return@forEach

            NavigationBarItem(
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun BottomFloatingBar(modifier: Modifier = Modifier) {
    val navItems = rememberNavItems()
    val tabNavigator = LocalTabNavigator.current

    HorizontalFloatingToolbar(
        expanded = true,
        modifier = modifier,
    ) {
        navItems.forEach { item ->
            val iconPainter = item.tab.options.icon ?: return@forEach

            Box {
                SoftcoverIconToggleButton(
                    checked = item.selected,
                    onCheckedChange = { tabNavigator.current = item.tab },
                    icon = RhaydusIconResource.Custom(
                        painter = iconPainter,
                        contentDescription = item.tab.options.title,
                    ),
                    style = IconToggleButtonStyle.FILLED,
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .graphicsLayer {
                            scaleX = item.iconScale
                            scaleY = item.iconScale
                        },
                )

                if (item.showBadge) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.inversePrimary,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (-6).dp, y = 2.dp)
                            .size(10.dp),
                    )
                }
            }
        }
    }
}
