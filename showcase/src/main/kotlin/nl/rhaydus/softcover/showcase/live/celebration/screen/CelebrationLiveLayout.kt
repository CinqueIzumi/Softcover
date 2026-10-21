package nl.rhaydus.softcover.showcase.live.celebration.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.component.RhaydusButton
import nl.rhaydus.designsystem.model.ButtonStyle
import nl.rhaydus.softcover.core.component.celebration.MarkAsReadBurst
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.showcase.appearance.action.OnAppearanceClickedAction
import nl.rhaydus.softcover.showcase.appearance.action.OnBackClickedAction
import nl.rhaydus.softcover.showcase.appearance.screen.AppearanceSheetHost
import nl.rhaydus.softcover.showcase.live.celebration.action.OnCardBurstClickedAction
import nl.rhaydus.softcover.showcase.live.celebration.action.OnScreenBurstClickedAction
import nl.rhaydus.softcover.showcase.live.celebration.event.CelebrationLiveEvent
import nl.rhaydus.softcover.showcase.live.celebration.screenmodel.CelebrationLiveDependencies
import nl.rhaydus.softcover.showcase.live.celebration.state.CelebrationLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.celebration.state.CelebrationLiveUiState
import nl.rhaydus.softcover.showcase.live.overlaypage.screen.OverlayPage
import nl.rhaydus.toad.UiAction

@Composable
internal fun CelebrationLiveLayout(
    state: CelebrationLiveUiState,
    runAction: (UiAction<CelebrationLiveDependencies, CelebrationLiveUiState, CelebrationLiveEvent, CelebrationLiveLocalVariables>) -> Unit,
) {
    OverlayPage(
        topBar = state.topBar,
        onBackClick = { runAction(OnBackClickedAction(backEvent = CelebrationLiveEvent.NavigateBack)) },
        onAppearanceClick = { runAction(OnAppearanceClickedAction()) },
        overlay = {
            MarkAsReadBurst(
                model = state.screenBurst,
                modifier = Modifier.fillMaxSize(),
            )
        },
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
            contentAlignment = Alignment.Center,
        ) {
            MarkAsReadBurst(
                model = state.cardBurst,
                modifier = Modifier.fillMaxSize(),
            )

            RhaydusButton(
                label = "Card burst",
                style = ButtonStyle.TONAL,
                onClick = { runAction(OnCardBurstClickedAction()) },
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.xl),
            contentAlignment = Alignment.Center,
        ) {
            RhaydusButton(
                label = "Screen burst",
                style = ButtonStyle.FILLED,
                onClick = { runAction(OnScreenBurstClickedAction()) },
            )
        }
    }

    AppearanceSheetHost(
        appearance = state.appearance,
        runAction = runAction,
    )
}
