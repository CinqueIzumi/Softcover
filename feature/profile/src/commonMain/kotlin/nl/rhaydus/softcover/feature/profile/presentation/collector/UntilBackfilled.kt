package nl.rhaydus.softcover.feature.profile.presentation.collector

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Re-emits [this] as `null` until [isBackfilled] is true: Room answers a query over an unfilled library
 * with empty aggregates at once, and those must not be shown as the reader's real, empty stats.
 */
internal fun <T> Flow<T>.untilBackfilled(
    isBackfilled: Flow<Boolean>,
): Flow<T?> = combine(
    this,
    isBackfilled,
) { value, backfilled ->
    value.takeIf { backfilled }
}
