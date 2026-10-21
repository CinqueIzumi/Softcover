package nl.rhaydus.softcover.showcase.family.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.softcover.core.component.chrome.ChromeIconButton
import nl.rhaydus.softcover.core.component.sheet.modelOrNull
import nl.rhaydus.softcover.core.component.topbar.TopBar
import nl.rhaydus.softcover.core.component.topbar.TopBarEvent
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.showcase.appearance.event.AppearanceSheetEvent
import nl.rhaydus.softcover.showcase.appearance.screen.AppearanceSheet
import nl.rhaydus.softcover.showcase.family.action.FamilyAction
import nl.rhaydus.softcover.showcase.family.action.OnAppearanceClickedAction
import nl.rhaydus.softcover.showcase.family.action.OnAppearanceDismissedAction
import nl.rhaydus.softcover.showcase.family.action.OnBackClickedAction
import nl.rhaydus.softcover.showcase.family.action.OnBrightnessSelectedAction
import nl.rhaydus.softcover.showcase.family.action.OnPaletteSelectedAction
import nl.rhaydus.softcover.showcase.family.state.FamilyUiState

@Composable
internal fun FamilyScreenLayout(
    state: FamilyUiState,
    runAction: (FamilyAction) -> Unit,
) {
    Scaffold(
        contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
        topBar = {
            TopBar(
                model = state.topBar,
                onEvent = { event ->
                    when (event) {
                        TopBarEvent.BackClicked -> runAction(OnBackClickedAction())
                    }
                },
            ) { colors ->
                ChromeIconButton(
                    icon = SoftcoverIcon.Palette,
                    contentDescription = "Appearance",
                    tooltip = "Appearance",
                    onClick = { runAction(OnAppearanceClickedAction()) },
                    colors = colors,
                )
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(
                start = Spacing.xl,
                top = Spacing.xl,
                end = Spacing.xl,
                bottom = Spacing.xl + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
            ),
        ) {
            items(items = state.entries, key = { it.name }) { entry ->
                EntrySection(entry = entry)

                Spacer(modifier = Modifier.height(Spacing.xxxl))
            }
        }
    }

    state.appearance.sheet.modelOrNull?.let { sheet ->
        AppearanceSheet(
            model = sheet,
            paletteChips = state.appearance.paletteChips,
            brightnessChips = state.appearance.brightnessChips,
            onEvent = { event ->
                when (event) {
                    is AppearanceSheetEvent.PaletteSelected -> runAction(OnPaletteSelectedAction(palette = event.palette))
                    is AppearanceSheetEvent.BrightnessSelected -> runAction(
                        OnBrightnessSelectedAction(brightness = event.brightness),
                    )
                    AppearanceSheetEvent.Dismissed -> runAction(OnAppearanceDismissedAction())
                }
            },
        )
    }
}
