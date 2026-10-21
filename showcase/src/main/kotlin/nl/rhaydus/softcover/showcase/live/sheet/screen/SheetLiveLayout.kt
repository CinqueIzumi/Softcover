package nl.rhaydus.softcover.showcase.live.sheet.screen

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.softcover.core.component.chrome.ChromeIconButton
import nl.rhaydus.softcover.core.component.divider.Divider
import nl.rhaydus.softcover.core.component.sheet.LazySheetScaffold
import nl.rhaydus.softcover.core.component.sheet.LoadingSheet
import nl.rhaydus.softcover.core.component.sheet.LoadingSheetEvent
import nl.rhaydus.softcover.core.component.sheet.SheetActionRow
import nl.rhaydus.softcover.core.component.sheet.SheetRowEvent
import nl.rhaydus.softcover.core.component.sheet.SheetRowGroup
import nl.rhaydus.softcover.core.component.sheet.SheetScaffold
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldEvent
import nl.rhaydus.softcover.core.component.sheet.modelOrNull
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.component.topbar.TopBar
import nl.rhaydus.softcover.core.component.topbar.TopBarEvent
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.showcase.appearance.action.OnAppearanceClickedAction
import nl.rhaydus.softcover.showcase.appearance.action.OnBackClickedAction
import nl.rhaydus.softcover.showcase.appearance.screen.AppearanceSheetHost
import nl.rhaydus.softcover.showcase.live.sheet.action.OnDemoSelectedAction
import nl.rhaydus.softcover.showcase.live.sheet.action.OnLoadingFinishedAction
import nl.rhaydus.softcover.showcase.live.sheet.action.OnSheetDismissedAction
import nl.rhaydus.softcover.showcase.live.sheet.action.OnSheetPrimaryClickedAction
import nl.rhaydus.softcover.showcase.live.sheet.action.OnSheetRowSelectedAction
import nl.rhaydus.softcover.showcase.live.sheet.action.OnSheetSecondaryClickedAction
import nl.rhaydus.softcover.showcase.live.sheet.event.SheetLiveEvent
import nl.rhaydus.softcover.showcase.live.sheet.model.SheetDemoUiModel
import nl.rhaydus.softcover.showcase.live.sheet.screenmodel.SheetLiveDependencies
import nl.rhaydus.softcover.showcase.live.sheet.state.SheetLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.sheet.state.SheetLiveUiState
import nl.rhaydus.toad.UiAction

@Composable
internal fun SheetLiveLayout(
    state: SheetLiveUiState,
    runAction: (UiAction<SheetLiveDependencies, SheetLiveUiState, SheetLiveEvent, SheetLiveLocalVariables>) -> Unit,
) {
    Scaffold(
        contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
        topBar = {
            TopBar(
                model = state.topBar,
                onEvent = { event ->
                    when (event) {
                        TopBarEvent.BackClicked -> runAction(OnBackClickedAction(backEvent = SheetLiveEvent.NavigateBack))
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
        val onScaffoldEvent: (SheetScaffoldEvent) -> Unit = { event ->
            when (event) {
                SheetScaffoldEvent.PrimaryClicked -> runAction(OnSheetPrimaryClickedAction())
                SheetScaffoldEvent.SecondaryClicked -> runAction(OnSheetSecondaryClickedAction())
                SheetScaffoldEvent.Dismissed -> runAction(OnSheetDismissedAction())
                SheetScaffoldEvent.HeaderActionClicked -> Unit
            }
        }

        when (model) {
            is SheetDemoUiModel.Scrolling -> SheetScaffold(
                model = model.scaffold,
                onEvent = onScaffoldEvent,
            ) {
                SoftcoverText(
                    text = model.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            is SheetDemoUiModel.Pinned -> LazySheetScaffold(
                model = model.scaffold,
                onEvent = onScaffoldEvent,
            ) {
                item {
                    SheetRowGroup(
                        model = model.rows,
                        onEvent = { event ->
                            when (event) {
                                is SheetRowEvent.Selected -> runAction(OnSheetRowSelectedAction(key = event.key))
                            }
                        },
                    )
                }
            }

            is SheetDemoUiModel.Loading -> LoadingSheet(
                model = model.sheet,
                onEvent = { event ->
                    when (event) {
                        LoadingSheetEvent.LoaderFinished -> runAction(OnLoadingFinishedAction())
                        LoadingSheetEvent.Dismissed -> runAction(OnSheetDismissedAction())
                    }
                },
            )
        }
    }

    AppearanceSheetHost(
        appearance = state.appearance,
        runAction = runAction,
    )
}
