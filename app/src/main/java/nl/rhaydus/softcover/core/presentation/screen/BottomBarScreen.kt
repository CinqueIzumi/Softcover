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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
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
import nl.rhaydus.softcover.core.presentation.component.DockedBottomNavigationBar
import nl.rhaydus.softcover.core.presentation.util.SnackBarManager
import nl.rhaydus.softcover.feature.reading.presentation.screen.ReadingTab
import nl.rhaydus.softcover.feature.settings.domain.model.BottomBarStyle

val LocalBottomBarPadding = compositionLocalOf { 0.dp }

object BottomBarScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    override fun Content() {
        val snackBarState by SnackBarManager.snackBarState.collectAsStateWithLifecycle()

        var bottomBarHeight by remember { mutableStateOf(0.dp) }
        val localDensity = LocalDensity.current

        val bottomBarPadding = bottomBarHeight + 16.dp + WindowInsets.navigationBars
            .asPaddingValues()
            .calculateBottomPadding()

        val themeConfig = LocalThemeConfiguration.current

        TabNavigator(ReadingTab) {
            Scaffold(
                contentWindowInsets = WindowInsets(0),
                snackbarHost = {
                    SnackbarHost(
                        hostState = snackBarState,
                        modifier = Modifier.padding(bottom = bottomBarPadding)
                    )
                },
                bottomBar = {
                    if (themeConfig.bottomBarStyle == BottomBarStyle.DOCKED) {
                        DockedBottomNavigationBar()
                    }
                }
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

                    if (themeConfig.bottomBarStyle == BottomBarStyle.FLOATING) {
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
}