package nl.rhaydus.softcover.core.presentation.component

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import nl.rhaydus.softcover.feature.reading.presentation.screen.ReadingTab
import nl.rhaydus.softcover.feature.settings.presentation.screen.SettingsTab

@Composable
fun BottomNavigationBar() {
    val screens = remember {
        listOf(
            ReadingTab,
            SettingsTab,
        )
    }

    NavigationBar {
        val tabNavigator = LocalTabNavigator.current

        screens.forEach { tab: Tab ->
            val isSelected = tabNavigator.current == tab

            NavigationBarItem(
                icon = {
                    val itemContentColor = when (isSelected) {
                        true -> MaterialTheme.colorScheme.onPrimary
                        false -> Color.Gray
                    }

                    tab.options.icon?.let { icon ->
                        Icon(
                            painter = icon,
                            contentDescription = tab.options.title,
                            tint = itemContentColor,
                            modifier = Modifier.size(32.dp),
                        )
                    }
                },
                selected = isSelected,
                onClick = { tabNavigator.current = tab },
                label = {
                    Text(
                        text = tab.options.title,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp)
                    )
                }
            )
        }
    }
}