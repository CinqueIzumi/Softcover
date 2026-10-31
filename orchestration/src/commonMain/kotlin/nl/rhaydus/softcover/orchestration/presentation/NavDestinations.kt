package nl.rhaydus.softcover.orchestration.presentation

import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import kotlinx.coroutines.launch
import nl.rhaydus.designsystem.nav.NavPulse
import nl.rhaydus.designsystem.nav.NavPulseKey
import nl.rhaydus.designsystem.nav.rememberPulseScale
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.domain.model.AppUpdateState
import nl.rhaydus.softcover.core.presentation.util.LibraryNavPulseKey
import nl.rhaydus.softcover.core.presentation.util.LocalAppUpdateState
import nl.rhaydus.softcover.core.presentation.util.TabReselectRegistry
import nl.rhaydus.softcover.feature.explore.presentation.screen.ExploreTab
import nl.rhaydus.softcover.feature.library.presentation.screen.LibraryTab
import nl.rhaydus.softcover.feature.reading.presentation.screen.ReadingTab
import nl.rhaydus.softcover.feature.settings.presentation.screen.SettingsTab
import org.koin.compose.koinInject

/**
 * The four root destinations in display order. The single source of the tab set consumed by every
 * navigation-chrome renderer — the bottom bars, the rail, and the sidebar all map over this list.
 */
internal val appNavTabs: List<Tab> = listOf(
    ReadingTab,
    LibraryTab,
    ExploreTab,
    SettingsTab,
)

/**
 * Whether this root tab opens book detail in the **expanded-width two-pane** (list + detail). Settings
 * navigates only to pushed sub-pages, so it stays single-surface. Reading is a detail-style reading
 * list, so it keeps the two-pane on every platform. Library and Explore are wide-grid discovery
 * surfaces, platform-gated via [wideGridTabsUseDetailPane]: on mobile/tablet they keep the two-pane,
 * but desktop gives each a bespoke full-width layout where a side detail pane only starves the grid —
 * there a tapped book pushes the detail screen full-screen instead.
 */
internal fun Tab.isDetailCapable(): Boolean = when (this) {
    ReadingTab -> true
    LibraryTab, ExploreTab -> wideGridTabsUseDetailPane
    else -> false
}

/**
 * Whether the wide-grid discovery tabs (Library, Explore) participate in the expanded two-pane
 * (`true`) or open book detail with a full-screen push (`false`). Mobile/tablet: `true`. Desktop:
 * `false` — their bespoke full-width layouts have no detail pane; see [isDetailCapable].
 */
internal expect val wideGridTabsUseDetailPane: Boolean

/**
 * The `NavPulse` key for [tab]'s icon. Library keeps the shared [LibraryNavPulseKey] that Reading
 * pulses on mark-as-read; every other tab by a [TabNavPulseKey].
 */
internal fun navPulseKeyFor(tab: Tab): NavPulseKey = if (tab == LibraryTab) LibraryNavPulseKey else TabNavPulseKey(tab)

/**
 * The catalog icon [tab] shows in the navigation chrome.
 */
internal fun navIconFor(tab: Tab): SoftcoverIcon = when (tab) {
    ReadingTab -> SoftcoverIcon.Reading
    LibraryTab -> SoftcoverIcon.Shelf
    ExploreTab -> SoftcoverIcon.Explore
    SettingsTab -> SoftcoverIcon.Settings
    else -> error("No navigation icon for $tab")
}

/**
 * Resolves [appNavTabs] into render-ready [NavItemUi]s: current selection (from the tab navigator),
 * the settings app-update badge, and each tab's per-frame icon pulse scale. Every chrome renderer calls
 * this so the bars/rail/sidebar share one selection/badge/pulse implementation.
 */
@Composable
internal fun rememberNavItems(): List<NavItemUi> {
    val tabNavigator = LocalTabNavigator.current
    val showSettingsBadge = LocalAppUpdateState.current != AppUpdateState.Idle
    val navPulse = koinInject<NavPulse>()

    return appNavTabs.map { tab ->
        val pulseScale = rememberPulseScale(
            pulse = navPulse,
            key = navPulseKeyFor(tab),
        )

        NavItemUi(
            tab = tab,
            icon = drawableIconResource(
                icon = navIconFor(tab),
                contentDescription = tab.options.title,
            ),
            selected = tabNavigator.current == tab,
            showBadge = tab == SettingsTab && showSettingsBadge,
            iconScale = pulseScale,
        )
    }
}

internal class NavItemClickResolver(
    private val selectTab: (Tab) -> Unit,
    private val reselect: suspend (Tab) -> Boolean,
    private val pulse: (NavPulseKey) -> Unit,
) {
    fun onClick(
        item: NavItemUi,
        launchReselect: (suspend () -> Unit) -> Unit,
    ) {
        if (item.selected.not()) {
            selectTab(item.tab)
            return
        }

        launchReselect {
            if (reselect(item.tab).not()) pulse(navPulseKeyFor(item.tab))
        }
    }
}

@Composable
internal fun rememberNavItemClick(): (NavItemUi) -> Unit {
    val tabNavigator = LocalTabNavigator.current
    val navPulse = koinInject<NavPulse>()
    val registry = koinInject<TabReselectRegistry>()
    val scope = rememberCoroutineScope()

    val resolver = remember(tabNavigator, navPulse, registry) {
        NavItemClickResolver(
            selectTab = { tabNavigator.current = it },
            reselect = registry::reselect,
            pulse = navPulse::pulse,
        )
    }

    return { item ->
        resolver.onClick(item) { block -> scope.launch { block() } }
    }
}

/**
 * The badged, pulse-scaled destination icon shared by the docked bar and the navigation rail (the
 * floating bar and sidebar style their own surfaces). Draws the app-update badge when
 * [NavItemUi.showBadge] is set and applies [NavItemUi.iconScale].
 */
@Composable
internal fun NavItemIcon(
    item: NavItemUi,
) {
    BadgedBox(
        badge = {
            if (item.showBadge) {
                Badge(containerColor = MaterialTheme.colorScheme.inversePrimary)
            }
        },
    ) {
        Icon(
            painter = item.icon.getIconPainter(),
            contentDescription = item.tab.options.title,
            modifier = Modifier.graphicsLayer {
                scaleX = item.iconScale
                scaleY = item.iconScale
            },
        )
    }
}

