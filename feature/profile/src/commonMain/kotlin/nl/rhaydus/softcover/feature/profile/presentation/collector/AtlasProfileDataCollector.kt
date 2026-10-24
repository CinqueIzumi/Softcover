package nl.rhaydus.softcover.feature.profile.presentation.collector

import kotlinx.coroutines.flow.filterNotNull
import nl.rhaydus.softcover.feature.profile.presentation.event.StatsAtlasEvent
import nl.rhaydus.softcover.feature.profile.presentation.mapper.atlasOpeningUiModel
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.StatsAtlasDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasLocalVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasUiState
import nl.rhaydus.toad.ActionScope

/** Observes only: Profile's `UserInformationCollector` owns the once-per-session refresh. */
internal class AtlasProfileDataCollector : StatsAtlasCollector {
    override suspend fun onLaunch(
        scope: ActionScope<StatsAtlasUiState, StatsAtlasEvent, StatsAtlasLocalVariables>,
        dependencies: StatsAtlasDependencies,
    ) {
        dependencies.observeUserProfileDataUseCase()
            .filterNotNull()
            .collect { profileData ->
                scope.setState {
                    it.copy(
                        userProfileData = profileData,
                        opening = atlasOpeningUiModel(profileData),
                        isLoading = false,
                    )
                }
            }
    }
}
