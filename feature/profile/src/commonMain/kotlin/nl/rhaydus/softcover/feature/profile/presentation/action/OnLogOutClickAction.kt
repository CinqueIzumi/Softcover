package nl.rhaydus.softcover.feature.profile.presentation.action

import nl.rhaydus.common.AppLog
import nl.rhaydus.designsystem.util.SnackBarManager
import nl.rhaydus.softcover.feature.profile.presentation.event.ProfileEvent
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.ProfileDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.LocalProfileVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.ProfileUiState
import nl.rhaydus.toad.ActionScope

internal class OnLogOutClickAction : ProfileAction {
    override suspend fun execute(
        dependencies: ProfileDependencies,
        scope: ActionScope<ProfileUiState, ProfileEvent, LocalProfileVariables>,
    ) {
        dependencies.resetUserDataUseCase()
            .onFailure {
                AppLog.e("$it")

                SnackBarManager.showSnackbar(title = "Couldn't log out — try again.")

                scope.sendEvent(event = ProfileEvent.LogOutFailedEvent())
            }
            .onSuccess { scope.sendEvent(event = ProfileEvent.LogOutUserEvent()) }
    }
}
