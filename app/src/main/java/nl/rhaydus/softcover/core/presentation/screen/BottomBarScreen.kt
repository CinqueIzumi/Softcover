package nl.rhaydus.softcover.core.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import nl.rhaydus.softcover.core.presentation.component.BottomFloatingBar
import nl.rhaydus.softcover.core.presentation.util.SnackBarManager
import nl.rhaydus.softcover.feature.reading.presentation.screen.ReadingTab

val LocalBottomBarPadding = compositionLocalOf { 0.dp }

object BottomBarScreen : Screen {
    @Composable
    override fun Content() {
        val snackBarState by SnackBarManager.snackBarState.collectAsStateWithLifecycle()

        var bottomBarHeight by remember { mutableStateOf(0.dp) }
        val localDensity = LocalDensity.current

        val bottomBarPadding = bottomBarHeight + 16.dp + WindowInsets.navigationBars
            .asPaddingValues()
            .calculateBottomPadding()

        TabNavigator(ReadingTab) {
            Scaffold(
                contentWindowInsets = WindowInsets.statusBars,
                snackbarHost = {
                    SnackbarHost(
                        hostState = snackBarState,
                        modifier = Modifier.padding(bottom = bottomBarPadding)
                    )
                },
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .consumeWindowInsets(innerPadding)
                ) {
                    CompositionLocalProvider(
                        LocalBottomBarPadding provides bottomBarPadding
                    ) {
                        CurrentTab()
                    }

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .windowInsetsPadding(WindowInsets.navigationBars)
                            .onSizeChanged {
                                bottomBarHeight = with(localDensity) { it.height.toDp() }
                            }
                    ) {
                        BottomFloatingBar()

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}