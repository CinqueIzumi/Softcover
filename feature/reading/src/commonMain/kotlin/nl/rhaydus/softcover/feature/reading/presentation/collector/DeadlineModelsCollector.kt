package nl.rhaydus.softcover.feature.reading.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.component.badge.DeadlineSummaryTone
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookDeadline
import nl.rhaydus.softcover.core.domain.model.DeadlineProgress
import nl.rhaydus.softcover.core.domain.model.DeadlineUnit
import nl.rhaydus.softcover.core.uibinding.deadline.toCoverOverlayUiModel
import nl.rhaydus.softcover.core.uibinding.deadline.toDeadlineSummaryUiModel
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.toad.ActionScope

/**
 * Maps every deadline surface on the Reading screen off the composition (R9): each book's raw
 * [DeadlineProgress] (used by `planTodayNudgeFor`, which is why [ReadingScreenUiState
 * .deadlineProgressByBook] stays a domain type rather than a UI model), its cover overlay badge,
 * and its summary line.
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
        scope.state
            .map {
                DeadlineModelsSnapshot(
                    featuredBook = it.books.firstOrNull(),
                    books = it.books,
                    deadlines = it.deadlines,
                    dateStyle = it.dateStyle,
                )
            }
            .distinctUntilChanged()
            .collectLatest { snapshot ->
                val progressByBook = snapshot.books.mapNotNull { book ->
                    book.deadlineProgress(snapshot.deadlines)?.let { progress -> book.id to progress }
                }.toMap()

                scope.setState {
                    it.copy(
                        deadlineProgressByBook = progressByBook,
                        deadlineCoverOverlays = progressByBook.mapValues { (_, progress) ->
                            progress.toCoverOverlayUiModel()
                        },
                        deadlineSummaries = progressByBook.mapValues { (_, progress) ->
                            progress.toDeadlineSummaryUiModel(
                                dateStyle = snapshot.dateStyle,
                                tone = DeadlineSummaryTone.OnSurface,
                            )
                        },
                        featuredDeadlineSummary = progressByBook[snapshot.featuredBook?.id]?.toDeadlineSummaryUiModel(
                            dateStyle = snapshot.dateStyle,
                            tone = DeadlineSummaryTone.OnHeroBackdrop,
                        ),
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
