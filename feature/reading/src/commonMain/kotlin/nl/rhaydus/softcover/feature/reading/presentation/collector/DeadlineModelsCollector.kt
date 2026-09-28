package nl.rhaydus.softcover.feature.reading.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import nl.rhaydus.softcover.core.component.deadline.DeadlineSummaryTone
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookDeadline
import nl.rhaydus.softcover.core.domain.model.DeadlineProgress
import nl.rhaydus.softcover.core.domain.model.DeadlineUnit
import nl.rhaydus.softcover.core.uibinding.deadline.toCoverOverlayUiModel
import nl.rhaydus.softcover.core.uibinding.deadline.toDeadlineSummaryUiModel
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.mapper.planTodayNudgeFor
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.toad.ActionScope

/**
 * Maps every deadline surface on the Reading screen off the composition (R9): each book's cover
 * overlay badge, its summary line, and — for the featured book only — the plan-today nudge, which
 * is suppressed once [ReadingScreenUiState.dismissedPlanTodayByBook] carries today's date for it.
 *
 * [ReadingScreenUiState.featuredDeadlineSummary] is a separate field rather than a second entry in
 * [ReadingScreenUiState.deadlineSummaries] because the featured book's summary line sits on the
 * blurred-cover hero backdrop and takes a different ink ([DeadlineSummaryTone.OnHeroBackdrop]) than
 * every other summary line, which sits on the surface ([DeadlineSummaryTone.OnSurface]) — the same
 * book yields two different UI models depending on which surface renders it. Both layouts already
 * treat `books.firstOrNull()` as the featured book for [ReadingScreenUiState.featuredCover],
 * [ReadingScreenUiState.featuredBackdropCover] and [ReadingScreenUiState.featuredBookPace]; this
 * follows the same convention.
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
                    book.deadlineProgress(snapshot.deadlines)?.let { progress -> book.id to progress }
                }.toMap()

                val featuredId = snapshot.featuredBook?.id
                val isFeaturedPlanTodayDismissed =
                    snapshot.dismissedPlanTodayByBook[featuredId] == snapshot.today.toString()

                scope.setState {
                    it.copy(
                        deadlineCoverOverlays = progressByBook.mapValues { (_, progress) ->
                            progress.toCoverOverlayUiModel()
                        },
                        deadlineSummaries = progressByBook.mapValues { (_, progress) ->
                            progress.toDeadlineSummaryUiModel(
                                dateStyle = snapshot.dateStyle,
                                tone = DeadlineSummaryTone.OnSurface,
                            )
                        },
                        featuredDeadlineSummary = progressByBook[featuredId]?.toDeadlineSummaryUiModel(
                            dateStyle = snapshot.dateStyle,
                            tone = DeadlineSummaryTone.OnHeroBackdrop,
                        ),
                        featuredPlanTodayMessage = planTodayNudgeFor(progressByBook[featuredId])
                            .takeIf { isFeaturedPlanTodayDismissed.not() },
                    )
                }
            }
    }
}

private fun Book.deadlineProgress(deadlines: Map<Int, BookDeadline>): DeadlineProgress? {
    val deadline = deadlines[id] ?: return null
    val edition = currentEdition ?: return null

    val current = when (deadline.unit) {
        DeadlineUnit.PAGES -> userBookRead?.currentPage ?: 0
        DeadlineUnit.SECONDS -> userBookRead?.currentSeconds ?: 0
    }
    val total = when (deadline.unit) {
        DeadlineUnit.PAGES -> edition.pages ?: 0
        DeadlineUnit.SECONDS -> edition.audioSeconds ?: 0
    }

    return DeadlineProgress.compute(
        deadline = deadline,
        current = current,
        total = total,
    )
}
