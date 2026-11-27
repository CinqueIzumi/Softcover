package nl.rhaydus.softcover.feature.profile.presentation.collector

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import nl.rhaydus.softcover.core.domain.model.StatsRange
import nl.rhaydus.softcover.core.preferences.domain.usecase.ObserveReadingStatsBackfilledUseCase
import nl.rhaydus.softcover.core.profile.domain.model.PagesRead
import nl.rhaydus.softcover.core.profile.domain.model.ReadingHistory
import nl.rhaydus.softcover.core.profile.domain.model.ReadingStats
import nl.rhaydus.softcover.core.profile.domain.usecase.ObservePagesReadUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveReadingHistoryUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveReadingStatsUseCase

/** All-time stats, the pages read and the reading history, all read from the same settled library. */
internal data class LoadedReadingLife(
    val stats: ReadingStats,
    val pagesRead: PagesRead,
    val history: ReadingHistory,
)

/** The all-time [LoadedReadingLife], or `null` until the library is backfilled. */
internal fun observeLoadedReadingLife(
    observeReadingStatsUseCase: ObserveReadingStatsUseCase,
    observePagesReadUseCase: ObservePagesReadUseCase,
    observeReadingHistoryUseCase: ObserveReadingHistoryUseCase,
    observeReadingStatsBackfilledUseCase: ObserveReadingStatsBackfilledUseCase,
): Flow<LoadedReadingLife?> = combine(
    observeReadingStatsUseCase(StatsRange.Unbounded),
    observePagesReadUseCase(StatsRange.Unbounded),
    observeReadingHistoryUseCase(),
) { stats, pagesRead, history ->
    LoadedReadingLife(
        stats = stats,
        pagesRead = pagesRead,
        history = history,
    )
}.untilBackfilled(observeReadingStatsBackfilledUseCase())
