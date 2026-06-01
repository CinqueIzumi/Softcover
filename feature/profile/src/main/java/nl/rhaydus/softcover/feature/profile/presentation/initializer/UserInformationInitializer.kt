package nl.rhaydus.softcover.feature.profile.presentation.initializer

import kotlinx.coroutines.flow.filterNotNull
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.profile.presentation.event.ProfileEvent
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.ProfileDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.LocalProfileVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.ProfileUiState
import timber.log.Timber

class UserInformationInitializer() : ProfileInitializer {
    override suspend fun onLaunch(
        scope: ActionScope<ProfileUiState, ProfileEvent, LocalProfileVariables>,
        dependencies: ProfileDependencies,
    ) {
        dependencies.launch {
            dependencies.refreshUserProfileDataUseCase()
                .onFailure { Timber.e("$it") }
        }

        dependencies.observeUserProfileDataUseCase()
            .filterNotNull()
            .collect { profileData ->
                scope.setState { it.copy(userProfileData = profileData, isLoading = false) }
            }
    }
}
