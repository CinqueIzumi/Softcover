package nl.rhaydus.softcover.orchestration.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.TabNavigator
import org.koin.compose.koinInject
import nl.rhaydus.designsystem.layout.BottomBarPlacement
import nl.rhaydus.designsystem.layout.BottomBarScaffold
import nl.rhaydus.designsystem.layout.TwoPaneScaffold
import nl.rhaydus.designsystem.layout.WindowWidthClass
import nl.rhaydus.designsystem.layout.rememberWindowSizeClass
import nl.rhaydus.designsystem.motion.playDecorativeMotion
import nl.rhaydus.softcover.core.domain.model.BottomBarStyle
import nl.rhaydus.softcover.core.presentation.navigation.AppNavigator
import nl.rhaydus.softcover.core.presentation.navigation.BookDetailPresenter
import nl.rhaydus.softcover.core.presentation.navigation.LocalBookDetailPresenter
import nl.rhaydus.softcover.core.presentation.navigation.ScreenDestination
import nl.rhaydus.softcover.core.presentation.theme.LocalThemeConfiguration
import nl.rhaydus.softcover.feature.reading.presentation.screen.ReadingTab
import nl.rhaydus.softcover.feature.session.presentation.component.SessionPeekBar

private const val TAB_ROOT_TRANSITION_DURATION_MS = 200
private val TAB_ROOT_DRIFT = 12.dp

// Expanded two-pane list-pane sizing: a fraction of the available body width, clamped so a desktop
// list surface stays roomy while the detail pane never collapses on a smaller expanded window.
private const val EXPANDED_LIST_PANE_FRACTION = 0.46f
private val EXPANDED_LIST_PANE_MIN = 440.dp
private val EXPANDED_LIST_PANE_MAX = 680.dp

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
        TabNavigator(ReadingTab) {
            val tabNavigator = LocalTabNavigator.current

            // A book opened in one tab's pane must not bleed into another tab's view.
            LaunchedEffect(tabNavigator.current.key) {
                paneBook.value = null
            }

            // The shell owns the push-vs-pane decision, now also consulting the current tab: only a
            // detail-capable tab on an expanded window fills the detail pane. Every other case —
            // narrower widths, and the desktop Library/Explore (full-width, no pane) — pushes full-screen.
            val bookDetailPresenter = remember(
                widthClass,
                tabNavigator.current,
                rootNavigator,
                appNavigator,
            ) {
                BookDetailPresenter { destination ->
                    if (widthClass == WindowWidthClass.EXPANDED && tabNavigator.current.isDetailCapable()) {
                        paneBook.value = destination
                    } else {
                        rootNavigator.push(appNavigator.screen(destination))
                    }
                }
            }

            CompositionLocalProvider(LocalBookDetailPresenter provides bookDetailPresenter) {
                val tabBody = remember {
                    movableContentOf { TabRootHost() }
                }

                when (widthClass) {
                    WindowWidthClass.COMPACT -> CompactNavShell(body = tabBody)

                    WindowWidthClass.MEDIUM -> WideNavShell(
                        leading = { NavigationRailBar() },
                        body = tabBody,
                    )

                    WindowWidthClass.EXPANDED -> WideNavShell(
                        leading = { EditorialSidebar() },
                        body = {
                            // TwoPaneScaffold is rendered for EVERY expanded tab so the shared
                            // `tabBody` movable content stays in one stable slot (the list pane) —
                            // switching tabs never relocates it (a relocation tears down and rebuilds
                            // the tab host, which would strand the previous tab's content on screen).
                            // Detail-capable tabs (Reading, and Library/Explore on mobile/tablet)
                            // supply the book-detail pane; the desktop Library/Explore and Settings
                            // pass a null detail, collapsing to a full-width single pane. The list
                            // pane is sized to the available body
                            // width (clamped) so a wide desktop list surface gets real room while the
                            // detail pane never collapses on a smaller expanded window.
                            BoxWithConstraints {
                                val listPaneWidth = (maxWidth * EXPANDED_LIST_PANE_FRACTION)
                                    .coerceIn(
                                        EXPANDED_LIST_PANE_MIN,
                                        EXPANDED_LIST_PANE_MAX,
                                    )

                                TwoPaneScaffold(
                                    listPaneWidth = listPaneWidth,
                                    list = { tabBody() },
                                    detail = if (tabNavigator.current.isDetailCapable()) {
                                        {
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
                                        }
                                    } else {
                                        null
                                    },
                                )
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
 * per the user's [BottomBarStyle] preference — with the session peek bar riding above it. The
 * foundation `BottomBarScaffold` hosts both styles, so flipping the preference swaps a [placement]
 * rather than relocating the body between two hosts, and the measure-and-provide of the padding
 * contract is not re-derived here.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CompactNavShell(body: @Composable () -> Unit) {
    val themeConfig = LocalThemeConfiguration.current

    BottomBarScaffold(
        placement = themeConfig.bottomBarStyle.toPlacement(),
        bottomBar = { CompactBottomChrome(style = themeConfig.bottomBarStyle) },
        content = body,
    )
}

/**
 * The compact bottom chrome: the session peek bar riding above the navigation bar, in whichever of the
 * two bar styles the user prefers. Rendered into the `BottomBarScaffold` bar slot, so it neither measures
 * itself nor applies the navigation-bar inset — the host does both.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CompactBottomChrome(style: BottomBarStyle) {
    when (style) {
        BottomBarStyle.DOCKED -> Column {
            SessionPeekBar(modifier = Modifier.padding(bottom = 8.dp))

            DockedBottomNavigationBar()
        }

        BottomBarStyle.FLOATING -> {
            // The floating bar hovers over live content, so it swallows taps that would otherwise fall
            // through the gaps around the pill onto whatever scrolls beneath it.
            val shieldInteractionSource = remember { MutableInteractionSource() }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable(
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

/**
 * The wide shell (`>= 600dp`): a leading navigation [leading] (rail at medium, sidebar at expanded)
 * beside the tab content. There is no bottom bar, but the flush session peek bar is bottom-anchored
 * chrome overlaying the content, so it takes the same host with no breathing gap: content reserves the
 * peek bar's footprint when a session is live, and the bare navigation-bar inset when it is not.
 *
 * The peek bar spans the full width of [body] — on an expanded window that is *both* panes of the
 * two-pane layout — so the host provides `LocalBottomBarPadding` to the whole body and the detail pane
 * clears the bar just as the list pane does.
 */
@Composable
private fun WideNavShell(
    leading: @Composable () -> Unit,
    body: @Composable () -> Unit,
) {
    Row(modifier = Modifier.fillMaxSize()) {
        leading()

        BottomBarScaffold(
            // The wide shell has no bottom bar for a floating pill to ride over, so the session bar
            // docks as a flush, edge-to-edge strip across the content pane.
            bottomBar = {
                SessionPeekBar(
                    modifier = Modifier.fillMaxWidth(),
                    flush = true,
                )
            },
            barSpacing = 0.dp,
            content = body,
        )
    }
}

private fun BottomBarStyle.toPlacement(): BottomBarPlacement = when (this) {
    BottomBarStyle.DOCKED -> BottomBarPlacement.DOCKED
    BottomBarStyle.FLOATING -> BottomBarPlacement.OVERLAY
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
