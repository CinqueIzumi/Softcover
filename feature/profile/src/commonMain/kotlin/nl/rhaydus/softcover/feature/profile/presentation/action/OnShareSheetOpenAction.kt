package nl.rhaydus.softcover.feature.profile.presentation.action

import nl.rhaydus.softcover.core.component.share.ShareCaptureState
import nl.rhaydus.softcover.feature.profile.presentation.event.ProfileEvent
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.ProfileDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.LocalProfileVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.ProfileUiState
import nl.rhaydus.softcover.feature.profile.presentation.state.withShareCapture
import nl.rhaydus.toad.ActionScope

/** Opens the share sheet fresh, with no capture running and no close pending. */
internal class OnShareSheetOpenAction : ProfileAction {
    override suspend fun execute(
        dependencies: ProfileDependencies,
        scope: ActionScope<ProfileUiState, ProfileEvent, LocalProfileVariables>,
    ) {
        scope.setState {
            it.copy(isShareSheetVisible = true).withShareCapture(ShareCaptureState())
        }
    }
}
