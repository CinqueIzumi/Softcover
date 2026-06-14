package nl.rhaydus.softcover.orchestration.presentation

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
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.TabNavigator
import nl.rhaydus.softcover.core.designsystem.presentation.layout.TwoPaneScaffold
import nl.rhaydus.softcover.core.designsystem.presentation.layout.WindowWidthClass
import nl.rhaydus.softcover.core.designsystem.presentation.layout.rememberWindowSizeClass
import nl.rhaydus.softcover.core.designsystem.presentation.navigation.AppNavigator
import nl.rhaydus.softcover.core.designsystem.presentation.navigation.BookDetailPresenter
import nl.rhaydus.softcover.core.designsystem.presentation.navigation.LocalBookDetailPresenter
import nl.rhaydus.softcover.core.designsystem.presentation.navigation.ScreenDestination
import nl.rhaydus.softcover.core.designsystem.presentation.theme.LocalThemeConfiguration
import nl.rhaydus.softcover.core.designsystem.presentation.util.LocalBottomBarPadding
import nl.rhaydus.softcover.core.designsystem.presentation.util.playDecorativeMotion
import nl.rhaydus.softcover.core.domain.model.BottomBarStyle
import nl.rhaydus.softcover.feature.reading.presentation.screen.ReadingTab
import nl.rhaydus.softcover.feature.session.presentation.component.SessionPeekBar
import org.koin.compose.koinInject

private const val TAB_ROOT_TRANSITION_DURATION_MS = 200
private val TAB_ROOT_DRIFT = 12.dp

/**
 * The tabbed application shell. All width-adaptive navigation chrome — bottom bar, rail, sidebar,
 * and the expanded two-pane detail — lives **here and only here**. Surfaces pushed onto the parent
 * navigator (focus mode, the barcode scanner, the full-screen cover) and onboarding (hosted by the
 * root navigator, further out still) therefore render outside this shell and stay full-bleed single
 * surfaces with no rail/sidebar/pane — the placement of the adaptive logic is itself the gate.
 */
internal object BottomBarScreen : Screen {
    @Composable
    override fun Content() {
        val rootNavigator = LocalNavigator.currentOrThrow
        val appNavigator = koinInject<AppNavigator>()
        val widthClass = rememberWindowSizeClass().widthClass

        // The book selected into the expanded-width detail pane. Plain `remember`: it survives
        // recomposition and window resize (the cases adaptive UI cares about); it is a brand-new
        // surface, so dropping it on full process recreation regresses no existing behaviour.
        val paneBook = remember { mutableStateOf<ScreenDestination.BookDetail?>(null) }

        // The shell owns the push-vs-pane decision: on expanded a tap fills the detail pane; at every
        // narrower width it pushes the detail full-screen exactly as before. Re-created only when the
        // width bucket changes.
        val bookDetailPresenter = remember(widthClass, rootNavigator, appNavigator) {
            BookDetailPresenter { destination ->
                if (widthClass == WindowWidthClass.EXPANDED) {
                    paneBook.value = destination
                } else {
                    rootNavigator.push(appNavigator.screen(destination))
                }
            }
        }

        // Leaving the two-pane while a book is open carries it over as a pushed screen so the reader
        // doesn't lose it; growing back leaves an already-pushed detail alone (no resize thrash).
        LaunchedEffect(widthClass) {
            if (widthClass != WindowWidthClass.EXPANDED) {
                paneBook.value?.let { carried ->
                    paneBook.value = null
                    rootNavigator.push(appNavigator.screen(carried))
                }
            }
        }

        // The tab navigator and the tab-root host are deliberately instantiated ONCE, above the
        // width-class branch below: only the navigation chrome (bottom bar / rail / sidebar) around
        // the body changes when the window crosses a breakpoint. The body is wrapped in
        // movableContentOf so its state — the tab-root SaveableStateHolder and the live tab's UI
        // state — moves intact between the chrome layouts instead of being recreated on resize.
        CompositionLocalProvider(LocalBookDetailPresenter provides bookDetailPresenter) {
            TabNavigator(ReadingTab) {
                val tabNavigator = LocalTabNavigator.current

                // A book opened in one tab's pane must not bleed into another tab's view.
                LaunchedEffect(tabNavigator.current.key) {
                    paneBook.value = null
                }

                val tabBody = remember {
                    movableContentOf { bottomPadding: Dp ->
                        CompositionLocalProvider(
                            LocalBottomBarPadding provides bottomPadding,
                        ) {
                            TabRootHost()
                        }
                    }
                }

                when (widthClass) {
                    WindowWidthClass.COMPACT -> CompactNavShell(body = tabBody)

                    WindowWidthClass.MEDIUM -> WideNavShell(
                        leading = { NavigationRailBar() },
                        body = tabBody,
                    )

                    WindowWidthClass.EXPANDED -> WideNavShell(
                        leading = { EditorialSidebar() },
                        body = { bottomPadding ->
                            if (tabNavigator.current.isDetailCapable()) {
                                TwoPaneScaffold(
                                    list = { tabBody(bottomPadding) },
                                    detail = {
                                        val destination = paneBook.value

                                        if (destination != null) {
                                            BookDetailPaneHost(
                                                destination = destination,
                                                onClose = { paneBook.value = null },
                                                overlayNavigator = rootNavigator,
                                            )
                                        } else {
                                            EmptyDetailPane()
                                        }
                                    },
                                )
                            } else {
                                tabBody(bottomPadding)
                            }
                        },
                    )
                }
            }
        }
    }
}

/**
 * The phone-shaped shell (`< 600dp`): today's bottom bar — docked (attached) or floating (overlay)
 * per the user's [BottomBarStyle] preference — with the session peek bar riding above it.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CompactNavShell(body: @Composable (bottomPadding: Dp) -> Unit) {
    var bottomBarHeight by remember { mutableStateOf(0.dp) }
    val localDensity = LocalDensity.current

    val bottomBarPadding = bottomBarHeight + 16.dp + WindowInsets.navigationBars
        .asPaddingValues()
        .calculateBottomPadding()

    val themeConfig = LocalThemeConfiguration.current

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (themeConfig.bottomBarStyle == BottomBarStyle.DOCKED) {
                Column {
                    SessionPeekBar(modifier = Modifier.padding(bottom = 8.dp))

                    DockedBottomNavigationBar()
                }
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
        ) {
            body(bottomBarPadding)

            if (themeConfig.bottomBarStyle == BottomBarStyle.FLOATING) {
                val shieldInteractionSource = remember { MutableInteractionSource() }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
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
                        ),
                ) {
                    SessionPeekBar(modifier = Modifier.padding(bottom = 8.dp))

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

/**
 * The wide shell (`>= 600dp`): a leading navigation [leading] (rail at medium, sidebar at expanded)
 * beside the tab content. There is no bottom bar, so the session peek bar is bottom-anchored over
 * the content and the body reserves only the navigation-bar inset (plus the peek bar when a session
 * is live) at its foot.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WideNavShell(
    leading: @Composable () -> Unit,
    body: @Composable (bottomPadding: Dp) -> Unit,
) {
    var sessionPeekHeight by remember { mutableStateOf(0.dp) }
    val localDensity = LocalDensity.current

    val navBarsInset = WindowInsets.navigationBars
        .asPaddingValues()
        .calculateBottomPadding()

    Row(modifier = Modifier.fillMaxSize()) {
        leading()

        Scaffold(contentWindowInsets = WindowInsets(0)) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding),
            ) {
                body(sessionPeekHeight + navBarsInset)

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .onSizeChanged {
                            sessionPeekHeight = with(localDensity) { it.height.toDp() }
                        },
                ) {
                    SessionPeekBar(
                        modifier = Modifier.padding(
                            horizontal = 8.dp,
                            vertical = 8.dp,
                        ),
                    )
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
