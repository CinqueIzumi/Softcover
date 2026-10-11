package nl.rhaydus.softcover.feature.profile.presentation.action

import nl.rhaydus.softcover.core.component.share.ShareCaptureState
import nl.rhaydus.softcover.feature.profile.presentation.event.ProfileEvent
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.ProfileDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.LocalProfileVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.ProfileUiState
import nl.rhaydus.toad.ActionScope

internal class OnShareSheetOpenAction : ProfileAction {
    override suspend fun execute(
        dependencies: ProfileDependencies,
        scope: ActionScope<ProfileUiState, ProfileEvent, LocalProfileVariables>,
    ) {
        scope.setState {
            if (it.readingLife == null || it.userProfileData == null) return@setState it

            it.copy(
                isShareSheetRequested = true,
                shareCapture = ShareCaptureState(),
            )
        }
    }
}
