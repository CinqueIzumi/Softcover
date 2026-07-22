package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.DeadlineUnit
import nl.rhaydus.softcover.core.personal.domain.model.ReadingJournalEntry
import nl.rhaydus.softcover.core.personal.domain.model.ReadingPaceForecast
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

internal class ReadingPaceForecastCollector : BookDetailCollector {
    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun onLaunch(
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
        dependencies: BookDetailDependencies,
    ) {
        val readingBookIdFlow = scope.state
            .map { state -> state.book?.id.takeIf { state.book?.userBook?.status == BookStatus.Reading } }
            .distinctUntilChanged()

        val progressSnapshotFlow = scope.state
            .map {
                val edition = it.book?.currentEdition
                ProgressSnapshot(
                    bookId = it.book?.id,
                    totalPages = edition?.pages,
                    currentPage = it.book?.userBookRead?.currentPage ?: 0,
                    totalSeconds = edition?.audioSeconds,
                    currentSeconds = it.book?.userBookRead?.currentSeconds ?: 0,
                )
            }
            .distinctUntilChanged()

        // Keyed on the reading-gated bookId *and* the committed progress snapshot (not just
        // bookId) so a landed progress write — which changes currentPage/currentSeconds in
        // state only once the write actually commits, never per keystroke of the update sheet
        // — refetches the journal history instead of leaving it stale (e.g. a session that
        // crosses midnight needs the new day's entry to show up in the next average).
        val journalHistoryFlow = combine(readingBookIdFlow, progressSnapshotFlow) { bookId, snapshot ->
            bookId to snapshot
        }
            .distinctUntilChanged()
            .flatMapLatest { (bookId, _) ->
                if (bookId == null) {
                    flowOf(emptyList())
                } else {
                    flow {
                        val history = dependencies.getReadingJournalHistoryUseCase(bookId = bookId)
                            .onFailure { AppLog.e("$it") }
                            .getOrDefault(emptyList())

                        emit(history)
                    }
                }
            }

        combine(journalHistoryFlow, progressSnapshotFlow) { history, snapshot ->
            computeForecast(
                history = history,
                snapshot = snapshot,
            )
        }.collectLatest { forecast ->
            scope.setState { it.copy(readingPaceForecast = forecast) }
        }
    }

    private fun computeForecast(
        history: List<ReadingJournalEntry>,
        snapshot: ProgressSnapshot,
    ): ReadingPaceForecast? {
        val unit = when {
            snapshot.totalPages != null -> DeadlineUnit.PAGES
            snapshot.totalSeconds != null -> DeadlineUnit.SECONDS
            else -> return null
        }

        val current = when (unit) {
            DeadlineUnit.PAGES -> snapshot.currentPage
            DeadlineUnit.SECONDS -> snapshot.currentSeconds
        }

        val total = when (unit) {
            DeadlineUnit.PAGES -> snapshot.totalPages
            DeadlineUnit.SECONDS -> snapshot.totalSeconds
        } ?: return null

        return ReadingPaceForecast.compute(
            dailyUnitsRead = dailyUnitsRead(
                entries = history,
                unit = unit,
            ),
            current = current,
            total = total,
            unit = unit,
        )
    }

    /**
     * Journal entries carry a cumulative position (mirroring [nl.rhaydus.softcover.core.domain.model.UserBookRead],
     * which also stores the current position rather than a delta), not a per-day amount, so this
     * takes the chronologically-last position logged on each calendar date — entries arrive
     * ordered `updated_at` ascending, so `.last()` per date is the day's final position, not its
     * peak — and diffs it against the previous date's position, clamping to 0 so a correction
     * (an edited-down page number) never reads as negative progress.
     */
    private fun dailyUnitsRead(
        entries: List<ReadingJournalEntry>,
        unit: DeadlineUnit,
    ): List<Int> {
        val positionsByDate = entries
            .mapNotNull { entry ->
                val position = when (unit) {
                    DeadlineUnit.PAGES -> entry.pages
                    DeadlineUnit.SECONDS -> entry.seconds
                }

                position?.let { entry.date to it }
            }
            .groupBy(
                keySelector = { it.first },
                valueTransform = { it.second },
            )
            .mapValues { (_, positions) -> positions.last() }
            .toList()
            .sortedBy { (date, _) -> date }
            .map { (_, position) -> position }

        var previousPosition = 0

        return positionsByDate.map { position ->
            val delta = (position - previousPosition).coerceAtLeast(0)
            previousPosition = position
            delta
        }
    }
}
