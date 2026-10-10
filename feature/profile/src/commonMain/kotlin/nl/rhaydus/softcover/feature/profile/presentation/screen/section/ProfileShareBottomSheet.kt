package nl.rhaydus.softcover.feature.profile.presentation.screen.section

import androidx.compose.runtime.Composable
import nl.rhaydus.softcover.core.component.share.ShareCardSheetContent
import nl.rhaydus.softcover.core.component.share.ShareCardSheetEvent
import nl.rhaydus.softcover.core.component.share.ShareCardSheetUiModel
import nl.rhaydus.softcover.feature.profile.presentation.action.OnShareCaptureEventAction
import nl.rhaydus.softcover.feature.profile.presentation.action.OnShareSheetDismissAction
import nl.rhaydus.softcover.feature.profile.presentation.action.ProfileAction

@Composable
internal fun ProfileShareBottomSheet(
    model: ShareCardSheetUiModel,
    runAction: (ProfileAction) -> Unit,
) {
    ShareCardSheetContent(
        model = model,
        onEvent = { event ->
            when (event) {
                ShareCardSheetEvent.Dismissed -> runAction(OnShareSheetDismissAction())

                else -> runAction(OnShareCaptureEventAction(captureEvent = event))
            }
        },
    )
}
