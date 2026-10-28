package nl.rhaydus.softcover.showcase.live.traits.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.softcover.core.component.chrome.ChromeIconButton
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.component.sheet.modelOrNull
import nl.rhaydus.softcover.core.component.topbar.TopBar
import nl.rhaydus.softcover.core.component.topbar.TopBarEvent
import nl.rhaydus.softcover.core.component.traits.HowItReadBlock
import nl.rhaydus.softcover.core.component.traits.HowItReadBlockEvent
import nl.rhaydus.softcover.core.component.traits.HowItReadSheet
import nl.rhaydus.softcover.core.component.traits.HowItReadSheetEvent
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.showcase.appearance.action.OnAppearanceClickedAction
import nl.rhaydus.softcover.showcase.appearance.action.OnBackClickedAction
import nl.rhaydus.softcover.showcase.appearance.screen.AppearanceSheetHost
import nl.rhaydus.softcover.showcase.live.traits.action.OnBalanceChangedAction
import nl.rhaydus.softcover.showcase.live.traits.action.OnBlockToggledAction
import nl.rhaydus.softcover.showcase.live.traits.action.OnChipClickedAction
import nl.rhaydus.softcover.showcase.live.traits.action.OnEditRequestedAction
import nl.rhaydus.softcover.showcase.live.traits.action.OnHowItReadDismissedAction
import nl.rhaydus.softcover.showcase.live.traits.action.OnHowItReadSavedAction
import nl.rhaydus.softcover.showcase.live.traits.event.HowItReadLiveEvent
import nl.rhaydus.softcover.showcase.live.traits.screenmodel.HowItReadLiveDependencies
import nl.rhaydus.softcover.showcase.live.traits.state.HowItReadLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.traits.state.HowItReadLiveUiState
import nl.rhaydus.toad.UiAction

@Composable
internal fun HowItReadLiveLayout(
    state: HowItReadLiveUiState,
    runAction: (
        UiAction<HowItReadLiveDependencies, HowItReadLiveUiState, HowItReadLiveEvent, HowItReadLiveLocalVariables>,
    ) -> Unit,
) {
    Scaffold(
        contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
        topBar = {
            TopBar(
                model = state.topBar,
                onEvent = { event ->
                    when (event) {
                        TopBarEvent.BackClicked -> runAction(
                            OnBackClickedAction(backEvent = HowItReadLiveEvent.NavigateBack),
                        )
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
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(
                    PaddingValues(
                        start = Spacing.xl,
                        end = Spacing.xl,
                        bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
                    ),
                ),
        ) {
            SectionHeader(model = state.header)

            Spacer(modifier = Modifier.height(Spacing.sm))

            HowItReadBlock(
                model = state.block,
                onEvent = { event ->
                    when (event) {
                        HowItReadBlockEvent.ExpandToggled -> runAction(OnBlockToggledAction())

                        HowItReadBlockEvent.EditRequested -> runAction(OnEditRequestedAction())
                    }
                },
            )
        }
    }

    state.sheet.modelOrNull?.let { model ->
        HowItReadSheet(
            model = model,
            onEvent = { event ->
                when (event) {
                    is HowItReadSheetEvent.ChipClicked -> runAction(OnChipClickedAction(key = event.key))

                    is HowItReadSheetEvent.BalanceChanged -> runAction(OnBalanceChangedAction(value = event.value))

                    HowItReadSheetEvent.BalanceCleared -> runAction(OnBalanceChangedAction(value = null))

                    HowItReadSheetEvent.SaveRequested -> runAction(OnHowItReadSavedAction())

                    HowItReadSheetEvent.Dismissed -> runAction(OnHowItReadDismissedAction())

                    HowItReadSheetEvent.CancelRequested -> Unit
                }
            },
        )
    }

    AppearanceSheetHost(
        appearance = state.appearance,
        runAction = runAction,
    )
}
