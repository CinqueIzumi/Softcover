package nl.rhaydus.softcover.showcase.live.progress.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.softcover.core.component.chrome.ChromeIconButton
import nl.rhaydus.softcover.core.component.divider.Divider
import nl.rhaydus.softcover.core.component.progress.ProgressSheetEvent
import nl.rhaydus.softcover.core.component.progress.UpdateProgressBottomSheet
import nl.rhaydus.softcover.core.component.sheet.SheetActionRow
import nl.rhaydus.softcover.core.component.sheet.SheetRowEvent
import nl.rhaydus.softcover.core.component.sheet.modelOrNull
import nl.rhaydus.softcover.core.component.topbar.TopBar
import nl.rhaydus.softcover.core.component.topbar.TopBarEvent
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.showcase.appearance.action.OnAppearanceClickedAction
import nl.rhaydus.softcover.showcase.appearance.action.OnBackClickedAction
import nl.rhaydus.softcover.showcase.appearance.screen.AppearanceSheetHost
import nl.rhaydus.softcover.showcase.live.progress.action.OnDemoSelectedAction
import nl.rhaydus.softcover.showcase.live.progress.action.OnProgressDismissedAction
import nl.rhaydus.softcover.showcase.live.progress.action.OnProgressMarkedAsReadAction
import nl.rhaydus.softcover.showcase.live.progress.action.OnProgressMinutesSteppedAction
import nl.rhaydus.softcover.showcase.live.progress.action.OnProgressSubmittedAction
import nl.rhaydus.softcover.showcase.live.progress.action.OnProgressTabSelectedAction
import nl.rhaydus.softcover.showcase.live.progress.action.OnProgressTimeChangedAction
import nl.rhaydus.softcover.showcase.live.progress.event.ProgressLiveEvent
import nl.rhaydus.softcover.showcase.live.progress.screenmodel.ProgressLiveDependencies
import nl.rhaydus.softcover.showcase.live.progress.state.ProgressLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.progress.state.ProgressLiveUiState
import nl.rhaydus.toad.UiAction

@Composable
internal fun ProgressLiveLayout(
    state: ProgressLiveUiState,
    runAction: (UiAction<ProgressLiveDependencies, ProgressLiveUiState, ProgressLiveEvent, ProgressLiveLocalVariables>) -> Unit,
) {
    Scaffold(
        contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
        topBar = {
            TopBar(
                model = state.topBar,
                onEvent = { event ->
                    when (event) {
                        TopBarEvent.BackClicked -> runAction(OnBackClickedAction(backEvent = ProgressLiveEvent.NavigateBack))
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
                end = Spacing.xl,
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
            ),
        ) {
            items(items = state.demos, key = { it.key }) { demo ->
                Divider()

                SheetActionRow(
                    model = demo,
                    onEvent = { event ->
                        when (event) {
                            is SheetRowEvent.Selected -> runAction(OnDemoSelectedAction(demo = event.key))
                        }
                    },
                )
            }
        }
    }

    state.sheet.modelOrNull?.let { model ->
        UpdateProgressBottomSheet(
            model = model,
            onEvent = { event ->
                when (event) {
                    is ProgressSheetEvent.TabSelected -> runAction(OnProgressTabSelectedAction(tab = event.tab))
                    is ProgressSheetEvent.TimeChanged -> runAction(
                        OnProgressTimeChangedAction(
                            unit = event.unit,
                            text = event.text,
                        ),
                    )
                    is ProgressSheetEvent.MinutesStepped -> runAction(OnProgressMinutesSteppedAction(delta = event.delta))
                    is ProgressSheetEvent.PagesSubmitted,
                    is ProgressSheetEvent.PercentageSubmitted,
                    is ProgressSheetEvent.TimeSubmitted -> runAction(OnProgressSubmittedAction())
                    ProgressSheetEvent.MarkAsReadRequested -> runAction(OnProgressMarkedAsReadAction())
                    ProgressSheetEvent.Dismissed -> runAction(OnProgressDismissedAction())
                    is ProgressSheetEvent.WhenReadShifted,
                    ProgressSheetEvent.WhenReadReset -> Unit
                }
            },
        )
    }

    AppearanceSheetHost(
        appearance = state.appearance,
        runAction = runAction,
    )
}
