package nl.rhaydus.softcover.feature.profile.presentation.collector

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import nl.rhaydus.softcover.core.preferences.domain.usecase.ObserveAuthorDemographicsBackfilledUseCase

/**
 * Re-emits [this] as `null` until the library's author demographics and page counts are backfilled: Room
 * answers a query over an unfilled library with empty aggregates at once, and those must not be shown as
 * the reader's real, empty stats.
 */
internal fun <T> Flow<T>.untilBackfilled(
    observeAuthorDemographicsBackfilledUseCase: ObserveAuthorDemographicsBackfilledUseCase,
): Flow<T?> = combine(
    this,
    observeAuthorDemographicsBackfilledUseCase(),
) { value, isBackfilled ->
    value.takeIf { isBackfilled }
}
