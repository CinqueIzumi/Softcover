package nl.rhaydus.softcover.core.presentation.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import nl.rhaydus.softcover.core.presentation.model.IconToggleButtonStyle
import nl.rhaydus.softcover.core.presentation.model.SoftcoverIconResource
import nl.rhaydus.softcover.core.presentation.screen.LocalAppUpdateState
import nl.rhaydus.softcover.feature.app_update.domain.model.AppUpdateState
import nl.rhaydus.softcover.feature.explore.presentation.screen.ExploreTab
import nl.rhaydus.softcover.feature.library.presentation.screen.LibraryTab
import nl.rhaydus.softcover.feature.reading.presentation.screen.ReadingTab
import nl.rhaydus.softcover.feature.settings.presentation.screen.SettingsTab

private val bottomBarScreens = listOf(
    ReadingTab,
    LibraryTab,
    ExploreTab,
    SettingsTab,
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DockedBottomNavigationBar() {
    val screens = remember { bottomBarScreens }
    val showSettingsBadge = LocalAppUpdateState.current != AppUpdateState.Idle

    NavigationBar {
        val tabNavigator = LocalTabNavigator.current

        screens.forEach { tab: Tab ->
            val isSelected = tabNavigator.current == tab
            val iconPainter = tab.options.icon ?: return@forEach

            NavigationBarItem(
                selected = isSelected,
                onClick = { tabNavigator.current = tab },
                icon = {
                    BadgedBox(
                        badge = {
                            if (tab == SettingsTab && showSettingsBadge) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.inversePrimary,
                                )
                            }
                        },
                    ) {
                        Icon(
                            painter = iconPainter,
                            contentDescription = "${tab.options.title} icon"
                        )
                    }
                },
                label = { Text(text = tab.options.title) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BottomFloatingBar(modifier: Modifier = Modifier) {
    val screens = remember { bottomBarScreens }
    val showSettingsBadge = LocalAppUpdateState.current != AppUpdateState.Idle

    HorizontalFloatingToolbar(
        expanded = true,
        modifier = modifier,
    ) {
        val tabNavigator = LocalTabNavigator.current

        screens.forEach { tab: Tab ->
            val isSelected = tabNavigator.current == tab
            val iconPainter = tab.options.icon ?: return@forEach

            Box {
                SoftcoverIconToggleButton(
                    checked = isSelected,
                    onCheckedChange = { tabNavigator.current = tab },
                    icon = SoftcoverIconResource.SoftcoverPainter(
                        painter = iconPainter,
                        contentDescription = tab.options.title
                    ),
                    style = IconToggleButtonStyle.FILLED,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                if (tab == SettingsTab && showSettingsBadge) {
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
