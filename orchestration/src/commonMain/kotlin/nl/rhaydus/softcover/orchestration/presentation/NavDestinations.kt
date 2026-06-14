package nl.rhaydus.softcover.orchestration.presentation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import kotlinx.coroutines.flow.drop
import nl.rhaydus.softcover.core.designsystem.presentation.util.BottomBarPulseManager
import nl.rhaydus.softcover.core.designsystem.presentation.util.LocalAppUpdateState
import nl.rhaydus.softcover.core.designsystem.presentation.util.playDecorativeMotion
import nl.rhaydus.softcover.core.domain.model.AppUpdateState
import nl.rhaydus.softcover.feature.explore.presentation.screen.ExploreTab
import nl.rhaydus.softcover.feature.library.presentation.screen.LibraryTab
import nl.rhaydus.softcover.feature.reading.presentation.screen.ReadingTab
import nl.rhaydus.softcover.feature.settings.presentation.screen.SettingsTab

private const val PULSE_PEAK_MS = 180
private const val PULSE_SETTLE_MS = 240
private const val PULSE_PEAK_SCALE = 1.22f

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
 * Whether this root tab opens book detail — and therefore earns the expanded-width two-pane layout
 * (list + detail). Settings navigates only to pushed sub-pages, so it stays single-surface.
 */
internal fun Tab.isDetailCapable(): Boolean =
    this == ReadingTab || this == LibraryTab || this == ExploreTab

/**
 * Resolves [appNavTabs] into render-ready [NavItemUi]s: current selection (from the tab navigator),
 * the settings app-update badge, and the per-frame library pulse scale. Every chrome renderer calls
 * this so the four bars/rail/sidebar share one selection/badge/pulse implementation.
 */
@Composable
internal fun rememberNavItems(): List<NavItemUi> {
    val tabNavigator = LocalTabNavigator.current
    val showSettingsBadge = LocalAppUpdateState.current != AppUpdateState.Idle
    val libraryPulseScale = rememberLibraryPulseScale()

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

@Composable
private fun rememberLibraryPulseScale(): Float {
    val playMotion = playDecorativeMotion()
    val scale = remember { Animatable(initialValue = 1f) }

    LaunchedEffect(playMotion) {
        if (playMotion.not()) return@LaunchedEffect

        snapshotFlow { BottomBarPulseManager.libraryPulseKey.intValue }
            .drop(count = 1)
            .collect {
                scale.snapTo(targetValue = 1f)
                scale.animateTo(
                    targetValue = PULSE_PEAK_SCALE,
                    animationSpec = tween(durationMillis = PULSE_PEAK_MS),
                )
                scale.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = PULSE_SETTLE_MS),
                )
            }
    }

    return scale.value
}
