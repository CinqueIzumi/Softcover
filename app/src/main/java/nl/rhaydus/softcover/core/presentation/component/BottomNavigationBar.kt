package nl.rhaydus.softcover.core.presentation.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import nl.rhaydus.softcover.core.presentation.model.IconToggleButtonStyle
import nl.rhaydus.softcover.core.presentation.model.SoftcoverIconResource
import nl.rhaydus.softcover.feature.library.presentation.screen.LibraryTab
import nl.rhaydus.softcover.feature.reading.presentation.screen.ReadingTab
import nl.rhaydus.softcover.feature.search.presentation.screen.SearchScreen
import nl.rhaydus.softcover.feature.settings.presentation.screen.SettingsTab

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BottomFloatingBar(modifier: Modifier = Modifier) {
    val screens = remember {
        listOf(
            ReadingTab,
            LibraryTab,
            SettingsTab,
        )
    }

    val navigator = LocalNavigator.currentOrThrow

    HorizontalFloatingToolbar(
        expanded = true,
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navigator.parent?.push(SearchScreen()) },
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "",
                )
            }
        }
    ) {
        val tabNavigator = LocalTabNavigator.current

        screens.forEach { tab: Tab ->
            val isSelected = tabNavigator.current == tab
            val iconPainter = tab.options.icon ?: return@forEach

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
        }
    }
}