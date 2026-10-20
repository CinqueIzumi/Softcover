package nl.rhaydus.softcover.orchestration.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.TabNavigator
import nl.rhaydus.designsystem.layout.BottomBarPlacement
import nl.rhaydus.designsystem.layout.BottomBarScaffold
import nl.rhaydus.designsystem.layout.TwoPaneScaffold
import nl.rhaydus.designsystem.layout.WindowWidthClass
import nl.rhaydus.designsystem.layout.rememberWindowSizeClass
import nl.rhaydus.softcover.core.component.state.EmptyState
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.domain.model.BottomBarStyle
import nl.rhaydus.softcover.core.presentation.navigation.AppNavigator
import nl.rhaydus.softcover.core.presentation.navigation.BookDetailPresenter
import nl.rhaydus.softcover.core.presentation.navigation.ExternalNavigationRequest
import nl.rhaydus.softcover.core.presentation.navigation.ExternalNavigationRequests
import nl.rhaydus.softcover.core.presentation.navigation.LocalBookDetailPresenter
import nl.rhaydus.softcover.core.presentation.navigation.ScreenDestination
import nl.rhaydus.softcover.core.presentation.theme.LocalThemeConfiguration
import nl.rhaydus.softcover.feature.reading.presentation.screen.ReadingTab
import nl.rhaydus.softcover.feature.session.presentation.component.SessionPeekBar
import nl.rhaydus.softcover.orchestration.presentation.screenmodel.BottomBarScreenModel
import org.koin.compose.koinInject

// Expanded two-pane list-pane sizing: a fraction of the available body width, clamped so a desktop
// list surface stays roomy while the detail pane never collapses on a smaller expanded window.
private const val EXPANDED_LIST_PANE_FRACTION = 0.46f

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
        val screenModel = koinScreenModel<BottomBarScreenModel>()
        val state by screenModel.state.collectAsStateWithLifecycle()
        val rootNavigator = LocalNavigator.currentOrThrow
        val appNavigator = koinInject<AppNavigator>()
        val widthClass = rememberWindowSizeClass().widthClass

        // The book selected into the expanded-width detail pane. Plain `remember`: it survives
        // recomposition and window resize (the cases adaptive UI cares about); it is a brand-new
        // surface, so dropping it on full process recreation regresses no existing behaviour.
        val paneBook = remember { mutableStateOf<ScreenDestination.BookDetail?>(null) }

        // Bumped on every open from a list so the pane restarts at that book even when it is already
        // the pane's root.
        val paneOpenToken = remember { mutableIntStateOf(0) }

        // The pane stack as destinations, reported by the host so it outlives the pane leaving composition.
        val paneStack = remember { mutableStateOf<List<ScreenDestination.BookDetail>>(emptyList()) }

        // Leaving the two-pane while a book is open carries the whole pane stack over as pushed screens
        // so the reader doesn't lose it; growing back leaves an already-pushed detail alone (no resize
        // thrash).
        LaunchedEffect(widthClass) {
            if (widthClass != WindowWidthClass.EXPANDED) {
                paneBook.value?.let { carried ->
                    val carriedStack = paneStack.value
                        .ifEmpty { listOf(carried) }
                        .map { appNavigator.screen(it) }

                    paneBook.value = null
                    paneStack.value = emptyList()
                    rootNavigator.push(carriedStack)
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
                        paneOpenToken.intValue++
                        paneBook.value = destination
                    } else {
                        rootNavigator.push(appNavigator.screen(destination))
                    }
                }
            }

            ConsumeExternalNavigationRequests(
                tabNavigator = tabNavigator,
                bookDetailPresenter = bookDetailPresenter,
            )

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
                                        440.dp,
                                        680.dp,
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
                                                    openToken = paneOpenToken.intValue,
                                                    onStackChanged = { paneStack.value = it },
                                                )
                                            } else {
                                                EmptyState(
                                                    model = state.emptyDetailPane,
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .padding(
                                                            horizontal = Spacing.gutter,
                                                            vertical = Spacing.xl,
                                                        ),
                                                )
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
 * Acts on the tab and book requests an external surface made, then consumes them. Focus Mode
 * requests belong to the root navigator and are left pending here.
 */
@Composable
private fun ConsumeExternalNavigationRequests(
    tabNavigator: TabNavigator,
    bookDetailPresenter: BookDetailPresenter,
) {
    val externalNavigationRequests = koinInject<ExternalNavigationRequests>()
    val appNavigator = koinInject<AppNavigator>()
    val currentPresenter by rememberUpdatedState(bookDetailPresenter)

    LaunchedEffect(externalNavigationRequests, appNavigator, tabNavigator) {
        externalNavigationRequests.pending.collect { request ->
            when (request) {
                is ExternalNavigationRequest.OpenTab -> {
                    tabNavigator.current = appNavigator.tab(request.destination)

                    externalNavigationRequests.consume(request = request)
                }

                is ExternalNavigationRequest.OpenBook -> {
                    currentPresenter.open(ScreenDestination.BookDetail(id = request.bookId))

                    externalNavigationRequests.consume(request = request)
                }

                is ExternalNavigationRequest.OpenBookVerdict -> {
                    currentPresenter.open(
                        ScreenDestination.BookDetail(
                            id = request.bookId,
                            showVerdictSheet = true,
                        ),
                    )

                    externalNavigationRequests.consume(request = request)
                }

                ExternalNavigationRequest.OpenFocusMode, null -> Unit
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
        bottomBar = {
            Box(modifier = Modifier.reportBottomChromeTop()) {
                CompactBottomChrome(style = themeConfig.bottomBarStyle)
            }
        },
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
            SessionPeekBar(modifier = Modifier.padding(bottom = Spacing.xs))

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
                SessionPeekBar(modifier = Modifier.padding(bottom = Spacing.xs))

                BottomFloatingBar(
                    modifier = Modifier.padding(
                        horizontal = Spacing.xs,
                        vertical = Spacing.xxs,
                    ),
                )

                Spacer(modifier = Modifier.height(Spacing.md))
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun TabRootHost() {
    val tabNavigator = LocalTabNavigator.current
    val stateHolder = rememberSaveableStateHolder()
    val density = LocalDensity.current
    val driftPx = remember(density) { with(density) { 12.dp.roundToPx() } }
    val effectsSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()
    val spatialSpec = MaterialTheme.motionScheme.defaultSpatialSpec<IntOffset>()

    AnimatedContent(
        targetState = tabNavigator.current,
        contentKey = { it.key },
        transitionSpec = {
            navFadeThrough(
                effectsSpec = effectsSpec,
                enterMotion = slideInVertically(
                    animationSpec = spatialSpec,
                    initialOffsetY = { driftPx },
                ),
            )
        },
        label = "TabRootCrossfade",
    ) { tab ->
        stateHolder.SaveableStateProvider(tab.key) {
            tab.Content()
        }
    }
}
