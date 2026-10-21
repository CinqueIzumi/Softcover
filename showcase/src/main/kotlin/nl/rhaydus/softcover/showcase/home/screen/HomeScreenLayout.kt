package nl.rhaydus.softcover.showcase.home.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.softcover.core.component.chrome.ChromeIconButton
import nl.rhaydus.softcover.core.component.header.PageMasthead
import nl.rhaydus.softcover.core.component.sheet.modelOrNull
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.showcase.appearance.event.AppearanceSheetEvent
import nl.rhaydus.softcover.showcase.appearance.screen.AppearanceSheet
import nl.rhaydus.softcover.showcase.home.action.HomeAction
import nl.rhaydus.softcover.showcase.home.action.OnAppearanceClickedAction
import nl.rhaydus.softcover.showcase.home.action.OnAppearanceDismissedAction
import nl.rhaydus.softcover.showcase.home.action.OnBrightnessSelectedAction
import nl.rhaydus.softcover.showcase.home.action.OnFamilyClickedAction
import nl.rhaydus.softcover.showcase.home.action.OnPaletteSelectedAction
import nl.rhaydus.softcover.showcase.home.state.HomeUiState

@Composable
internal fun HomeScreenLayout(
    state: HomeUiState,
    runAction: (HomeAction) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        PageMasthead(
            model = state.masthead,
            modifier = Modifier
                .statusBarsPadding()
                .padding(start = Spacing.gutter, end = Spacing.sm, top = Spacing.md),
        ) {
            ChromeIconButton(
                icon = SoftcoverIcon.Palette,
                contentDescription = "Appearance",
                tooltip = "Appearance",
                onClick = { runAction(OnAppearanceClickedAction()) },
            )
        }

        LazyColumn(contentPadding = WindowInsets.navigationBars.asPaddingValues()) {
            items(items = state.families, key = { it.family }) { row ->
                FamilyRow(
                    model = row,
                    onClick = { runAction(OnFamilyClickedAction(family = row.family)) },
                )
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
