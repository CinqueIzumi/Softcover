package nl.rhaydus.softcover.feature.profile.presentation.action

import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.feature.profile.presentation.event.ProfileEvent
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.ProfileDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.LocalProfileVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.ProfileUiState
import nl.rhaydus.toad.ActionScope

/**
 * Persists the switch on the author-representation section. The new value reaches the state through
 * `HideUntaggedAuthorsCollector` observing the preference, so this never writes the state itself - a
 * failed write leaves the switch where it was rather than showing a value that isn't stored.
 */
internal class OnHideUntaggedAuthorsToggledAction(val newValue: Boolean) : ProfileAction {
    override suspend fun execute(
        dependencies: ProfileDependencies,
        scope: ActionScope<ProfileUiState, ProfileEvent, LocalProfileVariables>,
    ) {
        dependencies.setHideUntaggedAuthorsUseCase(enabled = newValue).onFailure {
            AppLog.e("$it")
        }
    }
}
