package nl.rhaydus.softcover.feature.profile.presentation.screenmodel

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import nl.rhaydus.softcover.core.book.domain.usecase.ObserveBooksByIdsUseCase
import nl.rhaydus.softcover.core.domain.util.ObserveCurrentDateUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetHideUntaggedAuthorsAsFlowUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.ObserveAuthorDemographicsBackfilledUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.SetHideUntaggedAuthorsUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveHourlyPagesReadUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveMonthlyPagesReadUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveReadingHistoryUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveReadingStatsUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveUserProfileDataUseCase
import nl.rhaydus.toad.ActionDependencies

internal data class StatsAtlasDependencies(
    val observeUserProfileDataUseCase: ObserveUserProfileDataUseCase,
    val observeReadingStatsUseCase: ObserveReadingStatsUseCase,
    val observeMonthlyPagesReadUseCase: ObserveMonthlyPagesReadUseCase,
    val observeHourlyPagesReadUseCase: ObserveHourlyPagesReadUseCase,
    val observeReadingHistoryUseCase: ObserveReadingHistoryUseCase,
    val observeAuthorDemographicsBackfilledUseCase: ObserveAuthorDemographicsBackfilledUseCase,
    val getHideUntaggedAuthorsAsFlowUseCase: GetHideUntaggedAuthorsAsFlowUseCase,
    val setHideUntaggedAuthorsUseCase: SetHideUntaggedAuthorsUseCase,
    val observeBooksByIdsUseCase: ObserveBooksByIdsUseCase,
    val observeCurrentDateUseCase: ObserveCurrentDateUseCase,
    override val coroutineScope: CoroutineScope,
    override val mainDispatcher: CoroutineDispatcher,
) : ActionDependencies()
