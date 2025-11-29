package nl.rhaydus.softcover.core.presentation.screen

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import nl.rhaydus.softcover.core.presentation.component.BottomNavigationBar
import nl.rhaydus.softcover.feature.reading.presentation.screen.ReadingTab

object BottomBarScreen : Screen {
    @Composable
    override fun Content() {
        TabNavigator(ReadingTab) {
            Scaffold(
                contentWindowInsets = WindowInsets.statusBars,
                bottomBar = { BottomNavigationBar() }
            ) { innerPadding ->
                Surface(
                    modifier = Modifier
                        .padding(innerPadding)
                        .consumeWindowInsets(innerPadding)
                ) {
                    CurrentTab()
                }
            }
        }
    }
}