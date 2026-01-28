package nl.rhaydus.softcover.core.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import nl.rhaydus.softcover.core.presentation.component.BottomFloatingBar
import nl.rhaydus.softcover.core.presentation.util.SnackBarManager
import nl.rhaydus.softcover.feature.reading.presentation.screen.ReadingTab

object BottomBarScreen : Screen {
    @Composable
    override fun Content() {
        val snackBarState by SnackBarManager.snackBarState.collectAsStateWithLifecycle()

        TabNavigator(ReadingTab) {
            Scaffold(
                contentWindowInsets = WindowInsets.statusBars,
                snackbarHost = { SnackbarHost(hostState = snackBarState) },
                bottomBar = {
                    // TODO: It's not really a floating bar like this, as content is unable to go behind it...
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        BottomFloatingBar(
                            modifier = Modifier
                                .padding(bottom = 16.dp)
                                .align(Alignment.Center)
                        )
                    }
                },
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