package nl.rhaydus.softcover.feature.profile.presentation.collector

import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.transform
import nl.rhaydus.softcover.core.domain.model.RefreshScope
import nl.rhaydus.softcover.feature.profile.presentation.event.StatsAtlasEvent
import nl.rhaydus.softcover.feature.profile.presentation.mapper.waitingLongestUiModel
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.StatsAtlasDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasLocalVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasUiState
import nl.rhaydus.toad.ActionScope

/**
 * Maps the two books that have waited longest, on Want to Read and on the shelf, to book rows off the
 * composition, held on the loading stand-in until the read dates are backfilled. Owned rows without an `addedAt`
 * trigger one refresh of the Owned list, once per screen model; the stand-in holds while it runs and the
 * mapped model follows whether it succeeds or fails. The pair ignores the selected scope, so it is observed once.
 */
internal class WaitingLongestCollector : StatsAtlasCollector {
    override suspend fun onLaunch(
        scope: ActionScope<StatsAtlasUiState, StatsAtlasEvent, StatsAtlasLocalVariables>,
        dependencies: StatsAtlasDependencies,
    ) {
        var ownedRefreshAttempted = false

        combine(
            dependencies.observeLongestWaitingUseCase(),
            dependencies.observeReadDatesBackfilledUseCase(),
        ) { waiting, readDates -> waiting to readDates }
            .transform { (waiting, readDates) ->
                if (readDates.not()) {
                    emit(waitingLongestUiModel(null))

                    return@transform
                }

                val ownedListId: Int? = waiting.ownedListId

                if (ownedRefreshAttempted.not() && waiting.hasUndatedOwned && ownedListId != null) {
                    ownedRefreshAttempted = true

                    emit(waitingLongestUiModel(null))

                    dependencies.refreshLibraryUseCase(RefreshScope.ByList(listId = ownedListId))
                }

                emit(waitingLongestUiModel(waiting))
            }
            .collect { waiting -> scope.setState { it.copy(waitingLongest = waiting) } }
    }
}
