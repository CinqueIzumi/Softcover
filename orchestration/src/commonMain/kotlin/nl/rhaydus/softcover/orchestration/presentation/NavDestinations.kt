package nl.rhaydus.softcover.orchestration.presentation

import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import nl.rhaydus.designsystem.nav.NavPulse
import nl.rhaydus.designsystem.nav.rememberPulseScale
import nl.rhaydus.softcover.core.designsystem.presentation.util.LibraryNavPulseKey
import nl.rhaydus.softcover.core.designsystem.presentation.util.LocalAppUpdateState
import nl.rhaydus.softcover.core.domain.model.AppUpdateState
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
 * Resolves [appNavTabs] into render-ready [NavItemUi]s: current selection (from the tab navigator),
 * the settings app-update badge, and the per-frame library pulse scale. Every chrome renderer calls
 * this so the four bars/rail/sidebar share one selection/badge/pulse implementation.
 */
@Composable
internal fun rememberNavItems(): List<NavItemUi> {
    val tabNavigator = LocalTabNavigator.current
    val showSettingsBadge = LocalAppUpdateState.current != AppUpdateState.Idle
    val navPulse = koinInject<NavPulse>()
    val libraryPulseScale = rememberPulseScale(
        pulse = navPulse,
        key = LibraryNavPulseKey,
    )

    return appNavTabs.map { tab ->
        NavItemUi(
            tab = tab,
            selected = tabNavigator.current == tab,
            showBadge = tab == SettingsTab && showSettingsBadge,
            iconScale = if (tab == LibraryTab) libraryPulseScale else 1f,
        )
    }
}

/**
 * The badged, pulse-scaled destination icon shared by the docked bar and the navigation rail (the
 * floating bar and sidebar style their own surfaces). Draws the app-update badge when
 * [NavItemUi.showBadge] is set and applies [NavItemUi.iconScale] for the library pulse.
 */
@Composable
internal fun NavItemIcon(
    item: NavItemUi,
    painter: Painter,
) {
    BadgedBox(
        badge = {
            if (item.showBadge) {
                Badge(containerColor = MaterialTheme.colorScheme.inversePrimary)
            }
        },
    ) {
        Icon(
            painter = painter,
            contentDescription = "${item.tab.options.title} icon",
            modifier = Modifier.graphicsLayer {
                scaleX = item.iconScale
                scaleY = item.iconScale
            },
        )
    }
}

