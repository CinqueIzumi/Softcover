package nl.rhaydus.softcover.feature.settings.presentation.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
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
 * look those tiles offer. The Display section that follows renders whichever toggle rows
 * [SettingsScreenUiState.displayToggleRows] holds — the floating-bar and shelf-swipe rows are already
 * dropped there on desktop, reading streak always shows. [showUiScaleControl] is desktop-only (hidden
 * on mobile, where the OS handles DPI) and surfaces the "Display scale" picker directly after the two
 * colour pickers, since on desktop it is the appearance control that matters next. The caller supplies
 * the scroll / width [modifier].
 */
@Composable
internal fun AppearanceSettingsContent(
    state: SettingsScreenUiState,
    runAction: (SettingsAction) -> Unit,
    showUiScaleControl: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        SoftcoverText(
            text = "How Softcover looks in your hands, and how it reads dates back to you.",
            style = MaterialTheme.editorialTypography.body,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(Spacing.xxxl))

        ThemeSection(
            state = state,
            runAction = runAction,
        )

        Spacer(modifier = Modifier.height(Spacing.section))

        SpineColourSection(
            state = state,
            runAction = runAction,
        )

        Spacer(modifier = Modifier.height(Spacing.section))

        if (showUiScaleControl) {
            UiScaleSection(
                state = state,
                runAction = runAction,
            )

            Spacer(modifier = Modifier.height(Spacing.section))
        }

        DisplaySection(
            state = state,
            runAction = runAction,
        )

        Spacer(modifier = Modifier.height(Spacing.section))

        DateStyleSection(
            state = state,
            runAction = runAction,
        )
    }
}
