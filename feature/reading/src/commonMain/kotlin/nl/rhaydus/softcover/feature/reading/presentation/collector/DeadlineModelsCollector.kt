package nl.rhaydus.softcover.feature.reading.presentation.collector

import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import nl.rhaydus.softcover.core.uibinding.deadline.toCoverOverlayUiModel
import nl.rhaydus.softcover.core.uibinding.deadline.toDeadlineSummaryUiModel
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.mapper.planTodayNudgeFor
import nl.rhaydus.softcover.feature.reading.presentation.mapper.toDeadlineProgress
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.toad.ActionScope

/**
 * Maps every deadline surface on the Reading screen off the composition (R9): each book's cover
 * overlay badge and — for the featured book only — its summary line and the plan-today nudge, which
 * is suppressed once [ReadingScreenUiState.dismissedPlanTodayByBook] carries today's date for it.
 */
internal class DeadlineModelsCollector : ReadingCollector {
    override suspend fun onLaunch(
        scope: ActionScope<ReadingScreenUiState, ReadingScreenEvent, ReadingLocalVariables>,
        dependencies: ReadingScreenDependencies,
    ) {
        combine(
            scope.state,
            dependencies.observeCurrentDateUseCase(),
        ) { state, today ->
            DeadlineModelsSnapshot(
                featuredBook = state.books.firstOrNull(),
                books = state.books,
                deadlines = state.deadlines,
                dateStyle = state.dateStyle,
                dismissedPlanTodayByBook = state.dismissedPlanTodayByBook,
                today = today,
            )
        }
            .distinctUntilChanged()
            .collectLatest { snapshot ->
                val progressByBook = snapshot.books.mapNotNull { book ->
                    book.toDeadlineProgress(snapshot.deadlines[book.id])?.let { progress -> book.id to progress }
                }.toMap()

                val featuredId = snapshot.featuredBook?.id
                val isFeaturedPlanTodayDismissed =
                    snapshot.dismissedPlanTodayByBook[featuredId] == snapshot.today.toString()

                scope.setState {
                    it.copy(
                        deadlineCoverOverlays = progressByBook.mapValues { (_, progress) ->
                            progress.toCoverOverlayUiModel()
                        }.toImmutableMap(),
                        featuredDeadlineSummary = progressByBook[featuredId]?.toDeadlineSummaryUiModel(
                            dateStyle = snapshot.dateStyle,
                        ),
                        featuredPaceNudge = planTodayNudgeFor(
                            progress = progressByBook[featuredId],
                            visible = isFeaturedPlanTodayDismissed.not(),
                        )
                            ?: it.featuredPaceNudge?.copy(visible = false),
                    )
                }
            }
    }
}
