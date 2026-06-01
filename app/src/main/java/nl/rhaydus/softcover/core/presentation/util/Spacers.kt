package nl.rhaydus.softcover.core.presentation.util

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.presentation.screen.LocalBottomBarPadding
import nl.rhaydus.softcover.core.presentation.screen.LocalThemeConfiguration
import nl.rhaydus.softcover.core.domain.model.BottomBarStyle

@Composable
fun BottomNavigationSpacer() {
    Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
}

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