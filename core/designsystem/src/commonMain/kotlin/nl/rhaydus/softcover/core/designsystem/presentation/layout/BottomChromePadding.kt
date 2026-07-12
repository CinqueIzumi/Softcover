package nl.rhaydus.softcover.core.designsystem.presentation.layout

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.max
import nl.rhaydus.designsystem.layout.rememberBottomBarPadding

/**
 * The trailing padding a scrolling surface reserves for whatever bottom chrome sits over it: the bottom
 * bar / session peek bar when the surface is hosted in a `BottomBarScaffold`, and the bare system
 * navigation-bar inset when it is not.
 *
 * Reach for it only on a surface that renders in **both** positions — today that is book detail, which is
 * pushed full-screen on compact (outside the shell) and hosted in the expanded two-pane detail slot
 * (inside it). A surface that only ever renders inside the shell reads `rememberBottomBarPadding()`
 * directly; one that never does needs neither.
 *
 * The two values are complementary, not additive: outside the shell the bar footprint is `0.dp` so the
 * navigation-bar inset wins, and inside an overlay shell the measured footprint already bakes that inset
 * in and dominates. Taking the maximum is therefore exact in every hosting case — summing them would
 * double-count the inset in the pane.
 */
@Composable
fun bottomChromePadding(): Dp = max(
    rememberBottomBarPadding(),
    WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
)
