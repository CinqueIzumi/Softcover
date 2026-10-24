package nl.rhaydus.softcover.feature.profile.presentation.collector

import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import nl.rhaydus.softcover.feature.profile.presentation.event.StatsAtlasEvent
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.StatsAtlasDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasLocalVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasUiState
import nl.rhaydus.toad.ActionScope

/**
 * Observes only: Profile's `UserInformationCollector` owns the once-per-session refresh. The page keeps
 * loading until the library is backfilled, so an unfilled library never reads as an empty one.
 */
internal class AtlasProfileDataCollector : StatsAtlasCollector {
    override suspend fun onLaunch(
        scope: ActionScope<StatsAtlasUiState, StatsAtlasEvent, StatsAtlasLocalVariables>,
        dependencies: StatsAtlasDependencies,
    ) {
        combine(
            dependencies.observeUserProfileDataUseCase().filterNotNull(),
            dependencies.observeAuthorDemographicsBackfilledUseCase(),
        ) { profileData, isBackfilled ->
            profileData to isBackfilled
        }.collect { (profileData, isBackfilled) ->
            scope.setState {
                it.copy(
                    userProfileData = profileData,
                    isLoading = isBackfilled.not(),
                )
            }
        }
    }
}
