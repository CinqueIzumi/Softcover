package nl.rhaydus.softcover.core.presentation.screen

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.ScreenTransition
import nl.rhaydus.softcover.core.domain.connectivity.NetworkAvailabilityProvider
import nl.rhaydus.softcover.core.presentation.transition.LocalNavAnimatedVisibilityScope
import nl.rhaydus.softcover.core.presentation.transition.LocalSharedTransitionScope
import nl.rhaydus.softcover.feature.connectivity.presentation.component.ConnectivityBanner
import org.koin.compose.koinInject

object RootScreen : Screen {
    @OptIn(ExperimentalSharedTransitionApi::class)
    @Composable
    override fun Content() {
        val networkAvailabilityProvider = koinInject<NetworkAvailabilityProvider>()
        val isOnline by networkAvailabilityProvider.isOnline.collectAsState()

        Scaffold(
            contentWindowInsets = WindowInsets(0),
        ) { innerPadding ->
            Surface(
                modifier = Modifier
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
            ) {
                Column {
                    ConnectivityBanner(modifier = Modifier.statusBarsPadding())

                    Box(
                        modifier = if (isOnline.not()) {
                            Modifier.consumeWindowInsets(WindowInsets.statusBars)
                        } else {
                            Modifier
                        },
                    ) {
                        Navigator(BottomBarScreen) { navigator ->
                            SharedTransitionLayout {
                                val sharedTransitionScope = this

                                ScreenTransition(
                                    navigator = navigator,
                                    transition = {
                                        EnterTransition.None togetherWith ExitTransition.None
                                    },
                                ) { screen ->
                                    CompositionLocalProvider(
                                        LocalSharedTransitionScope provides sharedTransitionScope,
                                        LocalNavAnimatedVisibilityScope provides this,
                                    ) {
                                        screen.Content()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}