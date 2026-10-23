package nl.rhaydus.softcover.showcase.live.share.screen

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
import nl.rhaydus.softcover.core.component.share.ShareCardSheetContent
import nl.rhaydus.softcover.core.component.share.ShareCardSheetEvent
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
import nl.rhaydus.softcover.showcase.live.share.action.OnDemoSelectedAction
import nl.rhaydus.softcover.showcase.live.share.action.OnShareCaptureEventAction
import nl.rhaydus.softcover.showcase.live.share.action.OnShareDismissedAction
import nl.rhaydus.softcover.showcase.live.share.action.OnShareSharedAction
import nl.rhaydus.softcover.showcase.live.share.event.ShareLiveEvent
import nl.rhaydus.softcover.showcase.live.share.screenmodel.ShareLiveDependencies
import nl.rhaydus.softcover.showcase.live.share.state.ShareLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.share.state.ShareLiveUiState
import nl.rhaydus.toad.UiAction

@Composable
internal fun ShareLiveLayout(
    state: ShareLiveUiState,
    runAction: (UiAction<ShareLiveDependencies, ShareLiveUiState, ShareLiveEvent, ShareLiveLocalVariables>) -> Unit,
) {
    Scaffold(
        contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
        topBar = {
            TopBar(
                model = state.topBar,
                onEvent = { event ->
                    when (event) {
                        TopBarEvent.BackClicked -> runAction(OnBackClickedAction(backEvent = ShareLiveEvent.NavigateBack))
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
        ShareCardSheetContent(
            model = model,
            onEvent = { event ->
                when (event) {
                    ShareCardSheetEvent.ShareStarted,
                    ShareCardSheetEvent.SaveStarted,
                    ShareCardSheetEvent.CaptureFinished -> runAction(OnShareCaptureEventAction(event = event))
                    ShareCardSheetEvent.Shared -> runAction(OnShareSharedAction())
                    ShareCardSheetEvent.Dismissed -> runAction(OnShareDismissedAction())
                }
            },
        )
    }

    AppearanceSheetHost(
        appearance = state.appearance,
        runAction = runAction,
    )
}
