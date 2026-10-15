package nl.rhaydus.softcover.feature.reading.presentation.collector

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.domain.model.DeadlineUnit
import nl.rhaydus.softcover.core.personal.domain.model.ReadingJournalEntry
import nl.rhaydus.softcover.core.personal.domain.model.ReadingPaceForecast
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.mapper.toPaceLine
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
                    currentPage = book?.currentRead?.currentPage ?: 0,
                    totalSeconds = edition?.audioSeconds,
                    currentSeconds = book?.currentRead?.currentSeconds ?: 0,
                    startedAt = book?.currentRead?.startedAt,
                )
            }
            .distinctUntilChanged()

        val journalHistoryFlow = featuredBookIdFlow.flatMapLatest { bookId ->
            if (bookId == null) {
                flowOf(emptyList())
            } else {
                dependencies.observeReadingJournalHistoryUseCase(bookId = bookId)
            }
        }

        coroutineScope {
            launch {
                featuredBookIdFlow.filterNotNull().collectLatest { bookId ->
                    dependencies.refreshReadingJournalHistoryUseCase(bookId = bookId)
                }
            }

            combine(journalHistoryFlow, progressSnapshotFlow) { history, snapshot ->
                computeForecast(
                    history = history,
                    snapshot = snapshot,
                )
            }.collectLatest { forecast ->
                scope.setState { it.copy(featuredBookPace = forecast?.toPaceLine()) }
            }
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
            history = history,
            unit = unit,
            current = current,
            total = total,
            since = snapshot.startedAt?.let { runCatching { LocalDate.parse(it) }.getOrNull() },
        )
    }
}
