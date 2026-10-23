package nl.rhaydus.softcover.showcase.live.topbar.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import nl.rhaydus.softcover.core.component.sidebar.Sidebar
import nl.rhaydus.softcover.core.component.sidebar.SidebarEvent
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.component.topbar.EditorialTopBar
import nl.rhaydus.softcover.core.component.topbar.SearchTopBar
import nl.rhaydus.softcover.core.component.topbar.SearchTopBarEvent
import nl.rhaydus.softcover.core.component.topbar.TopBar
import nl.rhaydus.softcover.core.component.topbar.TopBarEvent
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.showcase.appearance.action.OnAppearanceClickedAction
import nl.rhaydus.softcover.showcase.appearance.action.OnBackClickedAction
import nl.rhaydus.softcover.showcase.appearance.screen.AppearanceSheetHost
import nl.rhaydus.softcover.showcase.live.topbar.action.OnBarSelectedAction
import nl.rhaydus.softcover.showcase.live.topbar.action.OnSearchActivatedAction
import nl.rhaydus.softcover.showcase.live.topbar.action.OnSearchClearedAction
import nl.rhaydus.softcover.showcase.live.topbar.action.OnSearchDismissedAction
import nl.rhaydus.softcover.showcase.live.topbar.action.OnSearchQueryChangedAction
import nl.rhaydus.softcover.showcase.live.topbar.event.TopbarLiveEvent
import nl.rhaydus.softcover.showcase.live.topbar.model.TopbarMode
import nl.rhaydus.softcover.showcase.live.topbar.model.TopbarRow
import nl.rhaydus.softcover.showcase.live.topbar.screenmodel.TopbarLiveDependencies
import nl.rhaydus.softcover.showcase.live.topbar.state.TopbarLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.topbar.state.TopbarLiveUiState
import nl.rhaydus.toad.UiAction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TopbarLiveLayout(
    state: TopbarLiveUiState,
    runAction: (UiAction<TopbarLiveDependencies, TopbarLiveUiState, TopbarLiveEvent, TopbarLiveLocalVariables>) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal),
        topBar = {
            when (state.mode) {
                TopbarMode.Standard -> TopBar(
                    model = state.topBar,
                    onEvent = { event ->
                        when (event) {
                            TopBarEvent.BackClicked -> runAction(OnBackClickedAction(backEvent = TopbarLiveEvent.NavigateBack))
                        }
                    },
                    scrollBehavior = scrollBehavior,
                )
                TopbarMode.Search -> SearchTopBar(
                    model = state.search,
                    onEvent = { event ->
                        when (event) {
                            is SearchTopBarEvent.QueryChanged -> runAction(OnSearchQueryChangedAction(query = event.query))
                            SearchTopBarEvent.SearchActivated -> runAction(OnSearchActivatedAction())
                            SearchTopBarEvent.SearchDismissed -> runAction(OnSearchDismissedAction())
                            SearchTopBarEvent.SearchCleared -> runAction(OnSearchClearedAction())
                            SearchTopBarEvent.ScanRequested -> Unit
                        }
                    },
                )
                TopbarMode.Editorial -> EditorialTopBar(
                    model = state.editorialBar,
                    onEvent = { event ->
                        when (event) {
                            TopBarEvent.BackClicked -> runAction(OnBackClickedAction(backEvent = TopbarLiveEvent.NavigateBack))
                        }
                    },
                    modifier = Modifier.statusBarsPadding(),
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
            verticalArrangement = Arrangement.spacedBy(Spacing.xl),
        ) {
            item {
                Sidebar(
                    model = state.sidebar,
                    onEvent = { event ->
                        when (event) {
                            is SidebarEvent.Clicked -> when (val row = event.key) {
                                is TopbarRow.Bar -> runAction(OnBarSelectedAction(mode = row.mode))
                                TopbarRow.Appearance -> runAction(OnAppearanceClickedAction())
                            }
                            is SidebarEvent.LongPressed -> Unit
                        }
                    },
                )
            }

            items(items = state.lines, key = { it }) { line ->
                SoftcoverText(
                    text = line,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Spacing.xs),
                )
            }
        }
    }

    AppearanceSheetHost(
        appearance = state.appearance,
        runAction = runAction,
    )
}
