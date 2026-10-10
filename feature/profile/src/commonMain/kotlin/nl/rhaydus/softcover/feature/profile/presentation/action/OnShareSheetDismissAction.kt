package nl.rhaydus.softcover.feature.profile.presentation.action

import nl.rhaydus.softcover.core.component.share.ShareCaptureState
import nl.rhaydus.softcover.feature.profile.presentation.event.ProfileEvent
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.ProfileDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.LocalProfileVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.ProfileUiState
import nl.rhaydus.softcover.feature.profile.presentation.state.withShareCapture
import nl.rhaydus.toad.ActionScope

/** Closes the share sheet and clears its capture and close state for the next opening. */
internal class OnShareSheetDismissAction : ProfileAction {
    override suspend fun execute(
        dependencies: ProfileDependencies,
        scope: ActionScope<ProfileUiState, ProfileEvent, LocalProfileVariables>,
    ) {
        scope.setState {
            it.copy(isShareSheetVisible = false).withShareCapture(ShareCaptureState())
        }
    }
}
