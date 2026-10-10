package nl.rhaydus.softcover.feature.profile.presentation.action

import nl.rhaydus.softcover.core.component.share.ShareCardSheetEvent
import nl.rhaydus.softcover.feature.profile.presentation.event.ProfileEvent
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.ProfileDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.LocalProfileVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.ProfileUiState
import nl.rhaydus.softcover.feature.profile.presentation.state.withShareCapture
import nl.rhaydus.toad.ActionScope

/** Folds a share sheet's capture [event] into the held capture state; the sheet stays open until told to close. */
internal class OnShareCaptureEventAction(
    private val captureEvent: ShareCardSheetEvent,
) : ProfileAction {
    override suspend fun execute(
        dependencies: ProfileDependencies,
        scope: ActionScope<ProfileUiState, ProfileEvent, LocalProfileVariables>,
    ) {
        scope.setState { it.withShareCapture(it.shareCapture.reduce(captureEvent)) }
    }
}
