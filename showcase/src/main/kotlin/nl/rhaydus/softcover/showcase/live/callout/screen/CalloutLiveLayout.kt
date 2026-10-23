package nl.rhaydus.softcover.showcase.live.callout.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.designsystem.component.RhaydusButton
import nl.rhaydus.designsystem.model.ButtonStyle
import nl.rhaydus.softcover.core.component.callout.Banner
import nl.rhaydus.softcover.core.component.callout.BannerEvent
import nl.rhaydus.softcover.core.component.callout.Callout
import nl.rhaydus.softcover.core.component.callout.CalloutEvent
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.showcase.appearance.action.OnAppearanceClickedAction
import nl.rhaydus.softcover.showcase.appearance.action.OnBackClickedAction
import nl.rhaydus.softcover.showcase.appearance.screen.AppearanceSheetHost
import nl.rhaydus.softcover.showcase.live.callout.action.OnBannerDismissedAction
import nl.rhaydus.softcover.showcase.live.callout.action.OnCalloutDismissedAction
import nl.rhaydus.softcover.showcase.live.callout.action.OnCalloutsRestoredAction
import nl.rhaydus.softcover.showcase.live.callout.event.CalloutLiveEvent
import nl.rhaydus.softcover.showcase.live.callout.screenmodel.CalloutLiveDependencies
import nl.rhaydus.softcover.showcase.live.callout.state.CalloutLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.callout.state.CalloutLiveUiState
import nl.rhaydus.softcover.showcase.live.overlaypage.screen.OverlayPage
import nl.rhaydus.toad.UiAction

@Composable
internal fun CalloutLiveLayout(
    state: CalloutLiveUiState,
    runAction: (UiAction<CalloutLiveDependencies, CalloutLiveUiState, CalloutLiveEvent, CalloutLiveLocalVariables>) -> Unit,
) {
    OverlayPage(
        topBar = state.topBar,
        onBackClick = { runAction(OnBackClickedAction(backEvent = CalloutLiveEvent.NavigateBack)) },
        onAppearanceClick = { runAction(OnAppearanceClickedAction()) },
    ) {
        state.banners.forEach { banner ->
            Banner(
                model = banner,
                onEvent = { event ->
                    when (event) {
                        BannerEvent.DismissClicked -> runAction(OnBannerDismissedAction(variant = banner.variant))
                    }
                },
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.xl),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            state.callouts.forEach { callout ->
                Callout(
                    model = callout,
                    onEvent = { event ->
                        if (event == CalloutEvent.DismissClicked) {
                            runAction(OnCalloutDismissedAction(callout = callout))
                        }
                    },
                )
            }

            RhaydusButton(
                label = "Bring them back",
                style = ButtonStyle.TONAL,
                onClick = { runAction(OnCalloutsRestoredAction()) },
            )
        }
    }

    AppearanceSheetHost(
        appearance = state.appearance,
        runAction = runAction,
    )
}
