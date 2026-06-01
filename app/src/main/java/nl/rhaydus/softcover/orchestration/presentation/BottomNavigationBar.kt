package nl.rhaydus.softcover.orchestration.presentation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import kotlinx.coroutines.flow.drop
import nl.rhaydus.softcover.core.presentation.component.SoftcoverIconToggleButton
import nl.rhaydus.softcover.core.presentation.model.IconToggleButtonStyle
import nl.rhaydus.softcover.core.presentation.model.SoftcoverIconResource
import nl.rhaydus.softcover.core.presentation.util.BottomBarPulseManager
import nl.rhaydus.softcover.core.presentation.util.LocalAppUpdateState
import nl.rhaydus.softcover.core.presentation.util.playDecorativeMotion
import nl.rhaydus.softcover.feature.app_update.domain.model.AppUpdateState
import nl.rhaydus.softcover.feature.explore.presentation.screen.ExploreTab
import nl.rhaydus.softcover.feature.library.presentation.screen.LibraryTab
import nl.rhaydus.softcover.feature.reading.presentation.screen.ReadingTab
import nl.rhaydus.softcover.feature.settings.presentation.screen.SettingsTab

private val bottomBarScreens = listOf(
    ReadingTab,
    LibraryTab,
    ExploreTab,
    SettingsTab,
)

private const val PULSE_PEAK_MS = 180
private const val PULSE_SETTLE_MS = 240
private const val PULSE_PEAK_SCALE = 1.22f

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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DockedBottomNavigationBar() {
    val screens = remember { bottomBarScreens }
    val showSettingsBadge = LocalAppUpdateState.current != AppUpdateState.Idle

    val libraryPulseScale = rememberLibraryPulseScale()

    NavigationBar {
        val tabNavigator = LocalTabNavigator.current

        screens.forEach { tab: Tab ->
            val isSelected = tabNavigator.current == tab
            val iconPainter = tab.options.icon ?: return@forEach

            val iconModifier = if (tab == LibraryTab) {
                Modifier.graphicsLayer {
                    scaleX = libraryPulseScale
                    scaleY = libraryPulseScale
                }
            } else {
                Modifier
            }

            NavigationBarItem(
                selected = isSelected,
                onClick = { tabNavigator.current = tab },
                icon = {
                    BadgedBox(
                        badge = {
                            if (tab == SettingsTab && showSettingsBadge) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.inversePrimary,
                                )
                            }
                        },
                    ) {
                        Icon(
                            painter = iconPainter,
                            contentDescription = "${tab.options.title} icon",
                            modifier = iconModifier,
                        )
                    }
                },
                label = { Text(text = tab.options.title) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BottomFloatingBar(modifier: Modifier = Modifier) {
    val screens = remember { bottomBarScreens }
    val showSettingsBadge = LocalAppUpdateState.current != AppUpdateState.Idle

    val libraryPulseScale = rememberLibraryPulseScale()

    HorizontalFloatingToolbar(
        expanded = true,
        modifier = modifier,
    ) {
        val tabNavigator = LocalTabNavigator.current

        screens.forEach { tab: Tab ->
            val isSelected = tabNavigator.current == tab
            val iconPainter = tab.options.icon ?: return@forEach

            val buttonModifier = if (tab == LibraryTab) {
                Modifier
                    .padding(horizontal = 4.dp)
                    .graphicsLayer {
                        scaleX = libraryPulseScale
                        scaleY = libraryPulseScale
                    }
            } else {
                Modifier.padding(horizontal = 4.dp)
            }

            Box {
                SoftcoverIconToggleButton(
                    checked = isSelected,
                    onCheckedChange = { tabNavigator.current = tab },
                    icon = SoftcoverIconResource.SoftcoverPainter(
                        painter = iconPainter,
                        contentDescription = tab.options.title,
                    ),
                    style = IconToggleButtonStyle.FILLED,
                    modifier = buttonModifier,
                )

                if (tab == SettingsTab && showSettingsBadge) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.inversePrimary,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (-6).dp, y = 2.dp)
                            .size(10.dp),
                    )
                }
            }
        }
    }
}
