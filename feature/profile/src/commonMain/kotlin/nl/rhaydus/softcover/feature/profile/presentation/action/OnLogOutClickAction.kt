package nl.rhaydus.softcover.feature.profile.presentation.action

import nl.rhaydus.softcover.core.component.sheet.requestClose
import nl.rhaydus.softcover.feature.profile.presentation.event.ProfileEvent
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.ProfileDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.LocalProfileVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.LogOut
import nl.rhaydus.softcover.feature.profile.presentation.state.ProfileUiState
import nl.rhaydus.toad.ActionScope

/** Records that the reader confirmed the log-out; [OnLogOutSheetDismissedAction] performs it after the sheet hides. */
internal class OnLogOutClickAction : ProfileAction {
    override suspend fun execute(
        dependencies: ProfileDependencies,
        scope: ActionScope<ProfileUiState, ProfileEvent, LocalProfileVariables>,
    ) {
        scope.setState {
            it.copy(logOutSheet = it.logOutSheet.requestClose(pending = LogOut))
        }
    }
}
