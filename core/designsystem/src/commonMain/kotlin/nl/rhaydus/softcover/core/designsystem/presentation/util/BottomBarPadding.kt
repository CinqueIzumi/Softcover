package nl.rhaydus.softcover.core.designsystem.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.layout.LocalBottomBarPadding
import nl.rhaydus.softcover.core.designsystem.presentation.theme.LocalThemeConfiguration
import nl.rhaydus.softcover.core.domain.model.BottomBarStyle

/**
 * The bottom padding a scrolling screen reserves so its last item clears the bottom bar. Resolves the
 * app's [BottomBarStyle]: a floating bar reserves the foundation [LocalBottomBarPadding] footprint, a
 * docked bar a fixed inset. (The neutral [LocalBottomBarPadding] / `BottomNavigationSpacer` live in
 * designsystem-core; this is the Softcover-specific floating-vs-docked policy on top of them.)
 */
@Composable
fun rememberBottomBarPadding(): Dp {
    val bottomBarStyle = LocalThemeConfiguration.current.bottomBarStyle
    val currentBottomBarHeight = LocalBottomBarPadding.current

    return remember(bottomBarStyle, currentBottomBarHeight) {
        when (bottomBarStyle) {
            BottomBarStyle.FLOATING -> currentBottomBarHeight
            BottomBarStyle.DOCKED -> 16.dp
        }
    }
}
