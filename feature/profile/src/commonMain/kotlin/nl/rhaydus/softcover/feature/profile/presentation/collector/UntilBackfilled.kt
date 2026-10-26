package nl.rhaydus.softcover.feature.profile.presentation.collector

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import nl.rhaydus.softcover.core.preferences.domain.usecase.ObserveReadingStatsBackfilledUseCase

/**
 * Re-emits [this] as `null` until the library's author demographics, page counts and contributor roles are
 * backfilled: Room answers a query over an unfilled library with empty aggregates at once, and those must
 * not be shown as the reader's real, empty stats.
 */
internal fun <T> Flow<T>.untilBackfilled(
    observeReadingStatsBackfilledUseCase: ObserveReadingStatsBackfilledUseCase,
): Flow<T?> = combine(
    this,
    observeReadingStatsBackfilledUseCase(),
) { value, isBackfilled ->
    value.takeIf { isBackfilled }
}
