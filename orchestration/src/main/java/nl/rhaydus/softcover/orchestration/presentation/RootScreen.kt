package nl.rhaydus.softcover.orchestration.presentation

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.ScreenTransition
import kotlinx.coroutines.flow.combine
import org.koin.compose.koinInject
import nl.rhaydus.softcover.core.designsystem.presentation.component.ConnectivityBanner
import nl.rhaydus.softcover.core.designsystem.presentation.session.ActiveSessionController
import nl.rhaydus.softcover.core.designsystem.presentation.transition.LocalNavAnimatedVisibilityScope
import nl.rhaydus.softcover.core.designsystem.presentation.transition.LocalSharedTransitionScope
import nl.rhaydus.softcover.core.domain.connectivity.NetworkAvailabilityProvider
import nl.rhaydus.softcover.feature.session.presentation.screen.FocusModeScreen

object RootScreen : Screen {
    @OptIn(ExperimentalSharedTransitionApi::class)
    @Composable
    override fun Content() {
        val networkAvailabilityProvider = koinInject<NetworkAvailabilityProvider>()
        val isOnline by networkAvailabilityProvider.isOnline.collectAsState()

        val activeSessionController = koinInject<ActiveSessionController>()

        Scaffold(
            contentWindowInsets = WindowInsets(0),
        ) { innerPadding ->
            Surface(
                modifier = Modifier
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding),
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
                            LaunchedEffect(navigator) {
                                // Push Focus Mode only once a session is actually present. On a cold
                                // start the deep-link flag is set before the session flow has loaded,
                                // so we wait for both rather than consuming the request against a
                                // not-yet-loaded (null) session.
                                combine(
                                    activeSessionController.pendingFocusMode,
                                    activeSessionController.activeSession,
                                ) { pending, active -> pending && active != null }
                                    .collect { ready ->
                                        if (ready.not()) return@collect

                                        if (navigator.lastItem !is FocusModeScreen) {
                                            navigator.push(item = FocusModeScreen)
                                        }

                                        activeSessionController.consumeFocusModeRequest()
                                    }
                            }

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
