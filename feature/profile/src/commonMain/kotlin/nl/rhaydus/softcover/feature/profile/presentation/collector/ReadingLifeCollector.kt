package nl.rhaydus.softcover.feature.profile.presentation.collector

import kotlinx.coroutines.flow.filterNotNull
import nl.rhaydus.softcover.feature.profile.presentation.event.ProfileEvent
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.ProfileDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.LocalProfileVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.ProfileUiState
import nl.rhaydus.toad.ActionScope

internal class ReadingLifeCollector : ProfileCollector {
    override suspend fun onLaunch(
        scope: ActionScope<ProfileUiState, ProfileEvent, LocalProfileVariables>,
        dependencies: ProfileDependencies,
    ) {
        dependencies.observeReadingLifeUseCase()
            .filterNotNull()
            .collect { readingLife ->
                scope.setState { it.copy(readingLife = readingLife) }
            }
    }
}
