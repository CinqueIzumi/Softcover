package nl.rhaydus.softcover.feature.profile.presentation.action

import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.feature.profile.presentation.event.LogOutUserEvent
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
            .onFailure { AppLog.e("$it") }
            .onSuccess { scope.sendEvent(event = LogOutUserEvent()) }
    }
}
