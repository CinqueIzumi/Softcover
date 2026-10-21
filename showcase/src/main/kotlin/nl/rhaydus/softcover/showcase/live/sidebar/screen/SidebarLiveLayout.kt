package nl.rhaydus.softcover.showcase.live.sidebar.screen

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.softcover.core.component.chrome.ChromeIconButton
import nl.rhaydus.softcover.core.component.sidebar.Sidebar
import nl.rhaydus.softcover.core.component.sidebar.SidebarEvent
import nl.rhaydus.softcover.core.component.topbar.TopBar
import nl.rhaydus.softcover.core.component.topbar.TopBarEvent
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.showcase.appearance.action.OnAppearanceClickedAction
import nl.rhaydus.softcover.showcase.appearance.action.OnBackClickedAction
import nl.rhaydus.softcover.showcase.appearance.screen.AppearanceSheetHost
import nl.rhaydus.softcover.showcase.live.sidebar.action.OnRowLongPressedAction
import nl.rhaydus.softcover.showcase.live.sidebar.action.OnRowSelectedAction
import nl.rhaydus.softcover.showcase.live.sidebar.event.SidebarLiveEvent
import nl.rhaydus.softcover.showcase.live.sidebar.screenmodel.SidebarLiveDependencies
import nl.rhaydus.softcover.showcase.live.sidebar.state.SidebarLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.sidebar.state.SidebarLiveUiState
import nl.rhaydus.toad.UiAction

@Composable
internal fun SidebarLiveLayout(
    state: SidebarLiveUiState,
    runAction: (UiAction<SidebarLiveDependencies, SidebarLiveUiState, SidebarLiveEvent, SidebarLiveLocalVariables>) -> Unit,
) {
    Scaffold(
        contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
        topBar = {
            TopBar(
                model = state.topBar,
                onEvent = { event ->
                    when (event) {
                        TopBarEvent.BackClicked -> runAction(OnBackClickedAction(backEvent = SidebarLiveEvent.NavigateBack))
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
        Sidebar(
            model = state.sidebar,
            onEvent = { event ->
                when (event) {
                    is SidebarEvent.Clicked -> runAction(OnRowSelectedAction(key = event.key))
                    is SidebarEvent.LongPressed -> runAction(OnRowLongPressedAction())
                }
            },
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding(),
        )
    }

    AppearanceSheetHost(
        appearance = state.appearance,
        runAction = runAction,
    )
}
