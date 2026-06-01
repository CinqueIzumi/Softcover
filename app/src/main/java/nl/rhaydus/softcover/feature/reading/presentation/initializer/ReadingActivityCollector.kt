package nl.rhaydus.softcover.feature.reading.presentation.initializer

import kotlinx.coroutines.flow.collectLatest
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.profile.domain.model.ReadingDayActivity
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import timber.log.Timber

class ReadingActivityCollector : ReadingInitializer {
    override suspend fun onLaunch(
        scope: ActionScope<ReadingScreenUiState, ReadingScreenEvent, ReadingLocalVariables>,
        dependencies: ReadingScreenDependencies,
    ) {
        // Keep the strip fresh even when the user never opens Profile — this is the
        // canonical refresh for reading-activity dates, the same one Profile fires.
        dependencies.launch {
            dependencies.refreshUserProfileDataUseCase()
                .onFailure { Timber.e(it) }
        }

        dependencies.observeRecentReadingActivityUseCase()
            .collectLatest { activity: List<ReadingDayActivity> ->
                scope.setState {
                    it.copy(recentReadingActivity = activity)
                }
            }
    }
}
