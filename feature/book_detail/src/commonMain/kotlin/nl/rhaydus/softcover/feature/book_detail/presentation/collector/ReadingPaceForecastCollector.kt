package nl.rhaydus.softcover.feature.book_detail.presentation.collector

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
                    currentPage = it.book?.currentRead?.currentPage ?: 0,
                    totalSeconds = edition?.audioSeconds,
                    currentSeconds = it.book?.currentRead?.currentSeconds ?: 0,
                    startedAt = it.book?.currentRead?.startedAt,
                )
            }
            .distinctUntilChanged()

        val journalHistoryFlow = readingBookIdFlow.flatMapLatest { bookId ->
            if (bookId == null) {
                flowOf(emptyList())
            } else {
                dependencies.observeReadingJournalHistoryUseCase(bookId = bookId)
            }
        }

        coroutineScope {
            launch {
                readingBookIdFlow.filterNotNull().collectLatest { bookId ->
                    dependencies.refreshReadingJournalHistoryUseCase(bookId = bookId)
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
