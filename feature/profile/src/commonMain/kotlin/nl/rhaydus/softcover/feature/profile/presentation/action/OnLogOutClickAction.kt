package nl.rhaydus.softcover.feature.profile.presentation.action

import nl.rhaydus.softcover.core.designsystem.presentation.toad.ActionScope
import nl.rhaydus.softcover.core.domain.logging.AppLog
import nl.rhaydus.softcover.feature.profile.presentation.event.LogOutUserEvent
import nl.rhaydus.softcover.feature.profile.presentation.event.ProfileEvent
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.ProfileDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.LocalProfileVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.ProfileUiState

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
