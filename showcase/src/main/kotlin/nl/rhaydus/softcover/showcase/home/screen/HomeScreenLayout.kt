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
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.showcase.appearance.action.OnAppearanceClickedAction
import nl.rhaydus.softcover.showcase.appearance.screen.AppearanceSheetHost
import nl.rhaydus.softcover.showcase.home.action.OnFamilyClickedAction
import nl.rhaydus.softcover.showcase.home.event.HomeEvent
import nl.rhaydus.softcover.showcase.home.screenmodel.HomeDependencies
import nl.rhaydus.softcover.showcase.home.state.HomeLocalVariables
import nl.rhaydus.softcover.showcase.home.state.HomeUiState
import nl.rhaydus.toad.UiAction

@Composable
internal fun HomeScreenLayout(
    state: HomeUiState,
    runAction: (UiAction<HomeDependencies, HomeUiState, HomeEvent, HomeLocalVariables>) -> Unit,
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

    AppearanceSheetHost(
        appearance = state.appearance,
        runAction = runAction,
    )
}
