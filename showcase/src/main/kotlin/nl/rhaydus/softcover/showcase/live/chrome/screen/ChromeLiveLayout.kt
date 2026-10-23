package nl.rhaydus.softcover.showcase.live.chrome.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import nl.rhaydus.softcover.core.component.chrome.ChromeIconButton
import nl.rhaydus.softcover.core.component.chrome.overMediaIconButtonColors
import nl.rhaydus.softcover.core.component.cover.Cover
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.showcase.appearance.action.OnAppearanceClickedAction
import nl.rhaydus.softcover.showcase.appearance.action.OnBackClickedAction
import nl.rhaydus.softcover.showcase.appearance.screen.AppearanceSheetHost
import nl.rhaydus.softcover.showcase.live.chrome.action.OnChromeButtonClickedAction
import nl.rhaydus.softcover.showcase.live.chrome.event.ChromeLiveEvent
import nl.rhaydus.softcover.showcase.live.chrome.model.ChromeButtonKey
import nl.rhaydus.softcover.showcase.live.chrome.screenmodel.ChromeLiveDependencies
import nl.rhaydus.softcover.showcase.live.chrome.state.ChromeLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.chrome.state.ChromeLiveUiState
import nl.rhaydus.toad.UiAction

@Composable
internal fun ChromeLiveLayout(
    state: ChromeLiveUiState,
    runAction: (UiAction<ChromeLiveDependencies, ChromeLiveUiState, ChromeLiveEvent, ChromeLiveLocalVariables>) -> Unit,
) {
    Scaffold(
        contentWindowInsets = WindowInsets(
            0,
            0,
            0,
            0,
        ),
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(
                bottom = Spacing.xl + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
            ),
            verticalArrangement = Arrangement.spacedBy(Spacing.xl),
        ) {
            item {
                Box {
                    Cover(
                        model = state.cover,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = Spacing.xs),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ChromeIconButton(
                            icon = SoftcoverIcon.ArrowBack,
                            contentDescription = "Navigate back",
                            tooltip = "Back",
                            onClick = { runAction(OnBackClickedAction(backEvent = ChromeLiveEvent.NavigateBack)) },
                            colors = overMediaIconButtonColors(),
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        state.buttons.forEach { button ->
                            ChromeIconButton(
                                icon = button.icon,
                                contentDescription = button.label,
                                tooltip = button.label,
                                onClick = {
                                    when (button.key) {
                                        ChromeButtonKey.Appearance -> runAction(OnAppearanceClickedAction())
                                        else -> runAction(OnChromeButtonClickedAction(key = button.key))
                                    }
                                },
                                colors = overMediaIconButtonColors(),
                            )
                        }
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier.padding(horizontal = Spacing.xl),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md),
                ) {
                    SectionHeader(model = state.plainHeader)

                    Row {
                        state.buttons.forEach { button ->
                            ChromeIconButton(
                                icon = button.icon,
                                contentDescription = button.label,
                                tooltip = button.label,
                                onClick = {
                                    when (button.key) {
                                        ChromeButtonKey.Appearance -> runAction(OnAppearanceClickedAction())
                                        else -> runAction(OnChromeButtonClickedAction(key = button.key))
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    AppearanceSheetHost(
        appearance = state.appearance,
        runAction = runAction,
    )
}
