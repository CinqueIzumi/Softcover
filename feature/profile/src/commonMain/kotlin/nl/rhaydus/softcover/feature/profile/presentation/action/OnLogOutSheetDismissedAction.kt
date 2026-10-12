package nl.rhaydus.softcover.feature.profile.presentation.action

import nl.rhaydus.common.AppLog
import nl.rhaydus.designsystem.util.SnackBarManager
import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.core.component.sheet.dismissed
import nl.rhaydus.softcover.feature.profile.presentation.event.ProfileEvent
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.ProfileDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.LocalProfileVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.ProfileUiState
import nl.rhaydus.toad.ActionScope

/** Runs the confirmed log-out once the sheet has finished sliding away, so the root swap cannot cut the animation short. */
internal class OnLogOutSheetDismissedAction : ProfileAction {
    override suspend fun execute(
        dependencies: ProfileDependencies,
        scope: ActionScope<ProfileUiState, ProfileEvent, LocalProfileVariables>,
    ) {
        val pending = scope.currentState.logOutSheet.dismissed()

        scope.setState { it.copy(logOutSheet = SheetSession.Closed) }

        if (pending == null) return

        dependencies.resetUserDataUseCase()
            .onFailure {
                AppLog.e("$it")

                SnackBarManager.showSnackbar(title = "Couldn't log out. Try again.")

                scope.sendEvent(event = ProfileEvent.LogOutFailedEvent())
            }
            .onSuccess { scope.sendEvent(event = ProfileEvent.LogOutUserEvent()) }
    }
}
