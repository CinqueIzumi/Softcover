package nl.rhaydus.softcover.showcase.live.lists.screen

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
import nl.rhaydus.softcover.core.component.lists.ChooseListsBottomSheet
import nl.rhaydus.softcover.core.component.lists.ChooseListsEvent
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
import nl.rhaydus.softcover.showcase.live.lists.action.OnDemoSelectedAction
import nl.rhaydus.softcover.showcase.live.lists.action.OnListsDismissedAction
import nl.rhaydus.softcover.showcase.live.lists.action.OnListsMembershipToggledAction
import nl.rhaydus.softcover.showcase.live.lists.action.OnListsNewListRequestedAction
import nl.rhaydus.softcover.showcase.live.lists.event.ListsLiveEvent
import nl.rhaydus.softcover.showcase.live.lists.screenmodel.ListsLiveDependencies
import nl.rhaydus.softcover.showcase.live.lists.state.ListsLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.lists.state.ListsLiveUiState
import nl.rhaydus.toad.UiAction

@Composable
internal fun ListsLiveLayout(
    state: ListsLiveUiState,
    runAction: (UiAction<ListsLiveDependencies, ListsLiveUiState, ListsLiveEvent, ListsLiveLocalVariables>) -> Unit,
) {
    Scaffold(
        contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
        topBar = {
            TopBar(
                model = state.topBar,
                onEvent = { event ->
                    when (event) {
                        TopBarEvent.BackClicked -> runAction(OnBackClickedAction(backEvent = ListsLiveEvent.NavigateBack))
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
        ChooseListsBottomSheet(
            model = model,
            onEvent = { event ->
                when (event) {
                    is ChooseListsEvent.MembershipToggled -> runAction(
                        OnListsMembershipToggledAction(listId = event.listId),
                    )
                    ChooseListsEvent.NewListRequested -> runAction(OnListsNewListRequestedAction())
                    ChooseListsEvent.Dismissed -> runAction(OnListsDismissedAction())
                }
            },
        )
    }

    AppearanceSheetHost(
        appearance = state.appearance,
        runAction = runAction,
    )
}
