package nl.rhaydus.softcover.feature.settings.presentation.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.feature.settings.presentation.action.SettingsAction
import nl.rhaydus.softcover.feature.settings.presentation.screen.section.DateStyleSection
import nl.rhaydus.softcover.feature.settings.presentation.screen.section.DisplaySection
import nl.rhaydus.softcover.feature.settings.presentation.screen.section.SpineColourSection
import nl.rhaydus.softcover.feature.settings.presentation.screen.section.ThemeSection
import nl.rhaydus.softcover.feature.settings.presentation.screen.section.UiScaleSection
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState
import nl.rhaydus.softcover.feature.settings.presentation.util.supportsDynamicColor

/**
 * The Appearance settings body, shared by the mobile [AppearanceSettingsScreen] page and the desktop
 * Settings master–detail pane. Rows sit flat on the page background, hairline-divided — never boxed
 * cards. The theme picker leads on every platform — it is the one control that repaints the whole app
 * — followed by the spine-colour picker, which carries the dynamic-colour switch beneath its tiles
 * (gated on [supportsDynamicColor], always `false` on desktop) since that switch replaces the very
 * look those tiles offer. The Display section that follows collapses the floating-bar, shelf-swipe,
 * and reading-streak switches into one flat toggle-row stack: [showBottomBarToggle] hides the
 * floating-bottom-bar row on desktop (there is no bottom bar there — it is a compact-only
 * preference), [showShelfSwipeToggle] hides the swipe-between-shelves row on desktop (whose Library
 * switches shelves from a permanent sidebar, not a pager), and reading streak always shows.
 * [showUiScaleControl] is desktop-only (hidden on mobile, where the OS handles DPI) and surfaces the
 * "Display scale" picker directly after the two colour pickers, since on desktop it is the appearance
 * control that matters next. The caller supplies the scroll / width [modifier].
 */
@Composable
internal fun AppearanceSettingsContent(
    state: SettingsScreenUiState,
    runAction: (SettingsAction) -> Unit,
    showBottomBarToggle: Boolean,
    showShelfSwipeToggle: Boolean,
    showUiScaleControl: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = "How Softcover looks in your hands, and how it reads dates back to you.",
            style = MaterialTheme.editorialTypography.body,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(32.dp))

        ThemeSection(
            state = state,
            runAction = runAction,
        )

        Spacer(modifier = Modifier.height(40.dp))

        SpineColourSection(
            state = state,
            runAction = runAction,
        )

        Spacer(modifier = Modifier.height(40.dp))

        if (showUiScaleControl) {
            UiScaleSection(
                state = state,
                runAction = runAction,
            )

            Spacer(modifier = Modifier.height(40.dp))
        }

        DisplaySection(
            state = state,
            showBottomBarToggle = showBottomBarToggle,
            showShelfSwipeToggle = showShelfSwipeToggle,
            runAction = runAction,
        )

        Spacer(modifier = Modifier.height(40.dp))

        DateStyleSection(
            state = state,
            runAction = runAction,
        )

        Spacer(modifier = Modifier.height(48.dp))
    }
}
