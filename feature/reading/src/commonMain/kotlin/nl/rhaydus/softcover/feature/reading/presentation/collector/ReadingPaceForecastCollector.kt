package nl.rhaydus.softcover.feature.reading.presentation.collector

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.core.domain.model.DeadlineUnit
import nl.rhaydus.softcover.core.personal.domain.model.ReadingJournalEntry
import nl.rhaydus.softcover.core.personal.domain.model.ReadingPaceForecast
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.toad.ActionScope

/**
 * Mirrors book_detail's `ReadingPaceForecastCollector`, but for the Reading screen's featured/hero
 * book only ([ReadingScreenUiState.books]`.firstOrNull()`) — secondary rows never carry a pace.
 * Collectors are per-feature (they aren't shared across TOAD screens), so this duplicates the
 * forecast-computation logic rather than importing the book_detail collector.
 */
internal class ReadingPaceForecastCollector : ReadingCollector {
    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun onLaunch(
        scope: ActionScope<ReadingScreenUiState, ReadingScreenEvent, ReadingLocalVariables>,
        dependencies: ReadingScreenDependencies,
    ) {
        val featuredBookIdFlow = scope.state
            .map { it.books.firstOrNull()?.id }
            .distinctUntilChanged()

        val progressSnapshotFlow = scope.state
            .map { state ->
                val book = state.books.firstOrNull()
                val edition = book?.currentEdition
                ProgressSnapshot(
                    bookId = book?.id,
                    totalPages = edition?.pages,
                    currentPage = book?.userBookRead?.currentPage ?: 0,
                    totalSeconds = edition?.audioSeconds,
                    currentSeconds = book?.userBookRead?.currentSeconds ?: 0,
                )
            }
            .distinctUntilChanged()

        // Keyed on the featured bookId *and* the committed progress snapshot (not just bookId) so a
        // landed progress write — which changes currentPage/currentSeconds in state only once the
        // write actually commits — refetches the journal history instead of leaving it stale.
        val journalHistoryFlow = combine(featuredBookIdFlow, progressSnapshotFlow) { bookId, snapshot ->
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
            scope.setState { it.copy(featuredBookPace = forecast) }
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
     * Journal entries carry a cumulative position, not a per-day amount, so this takes the
     * chronologically-last position logged on each calendar date and diffs it against the previous
     * date's position, clamping to 0 so a correction never reads as negative progress. Mirrors
     * book_detail's `ReadingPaceForecastCollector.dailyUnitsRead`.
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
