package nl.rhaydus.softcover.core.presentation.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.TabNavigator
import nl.rhaydus.softcover.core.presentation.component.BottomFloatingBar
import nl.rhaydus.softcover.core.presentation.component.DockedBottomNavigationBar
import nl.rhaydus.softcover.core.presentation.util.playDecorativeMotion
import nl.rhaydus.softcover.feature.reading.presentation.screen.ReadingTab
import nl.rhaydus.softcover.feature.settings.domain.model.BottomBarStyle

private const val TAB_ROOT_TRANSITION_DURATION_MS = 200
private val TAB_ROOT_DRIFT = 12.dp

val LocalBottomBarPadding = compositionLocalOf { 0.dp }

object BottomBarScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    override fun Content() {
        var bottomBarHeight by remember { mutableStateOf(0.dp) }
        val localDensity = LocalDensity.current

        val bottomBarPadding = bottomBarHeight + 16.dp + WindowInsets.navigationBars
            .asPaddingValues()
            .calculateBottomPadding()

        val themeConfig = LocalThemeConfiguration.current

        TabNavigator(ReadingTab) {
            Scaffold(
                contentWindowInsets = WindowInsets(0),
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
                        TabRootHost()
                    }

                    if (themeConfig.bottomBarStyle == BottomBarStyle.FLOATING) {
                        val shieldInteractionSource = remember { MutableInteractionSource() }

                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .windowInsetsPadding(WindowInsets.navigationBars)
                                .onSizeChanged {
                                    bottomBarHeight = with(localDensity) { it.height.toDp() }
                                }
                                .clickable(
                                    interactionSource = shieldInteractionSource,
                                    indication = null,
                                    onClick = {},
                                )
                        ) {
                            BottomFloatingBar(
                                modifier = Modifier.padding(
                                    horizontal = 8.dp,
                                    vertical = 6.dp,
                                ),
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TabRootHost() {
    val tabNavigator = LocalTabNavigator.current
    val stateHolder = rememberSaveableStateHolder()
    val density = LocalDensity.current
    val driftPx = remember(density) { with(density) { TAB_ROOT_DRIFT.roundToPx() } }

    if (playDecorativeMotion().not()) {
        CurrentTab()
        return
    }

    AnimatedContent(
        targetState = tabNavigator.current,
        contentKey = { it.key },
        transitionSpec = {
            (fadeIn(animationSpec = tween(TAB_ROOT_TRANSITION_DURATION_MS)) +
                slideInVertically(
                    animationSpec = tween(TAB_ROOT_TRANSITION_DURATION_MS),
                    initialOffsetY = { driftPx },
                ))
                .togetherWith(fadeOut(animationSpec = tween(TAB_ROOT_TRANSITION_DURATION_MS)))
        },
        label = "TabRootCrossfade",
    ) { tab ->
        stateHolder.SaveableStateProvider(tab.key) {
            tab.Content()
        }
    }
}