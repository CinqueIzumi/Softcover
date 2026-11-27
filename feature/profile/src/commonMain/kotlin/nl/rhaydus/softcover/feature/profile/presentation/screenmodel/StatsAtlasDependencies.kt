package nl.rhaydus.softcover.feature.profile.presentation.screenmodel

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import nl.rhaydus.softcover.core.book.domain.usecase.ObserveBooksByIdsUseCase
import nl.rhaydus.softcover.core.domain.account.RefreshLibraryUseCase
import nl.rhaydus.softcover.core.domain.util.ObserveCurrentDateUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetCurrenciesUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetHideBooksWithoutDetailAsFlowUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetUsualCurrencyAsFlowUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.ObserveQuotesSyncedUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.ObserveReadDatesBackfilledUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.ObserveReadingStatsBackfilledUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.SetHideBooksWithoutDetailUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.FillReaderEditionLanguagesUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveDidNotFinishStatsUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveDiversityStatsUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveHourlyPagesReadUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveLongestWaitingUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveMonthlyPagesReadUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.ObservePagesReadUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveReadingActivityUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveReadingDayUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveReadingHistoryUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveReadingListStatsUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveReadingStatsUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveSpendStatsUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveTimeToFinishUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveUserProfileDataUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveWrittenStatsUseCase
import nl.rhaydus.toad.ActionDependencies

internal data class StatsAtlasDependencies(
    val observeUserProfileDataUseCase: ObserveUserProfileDataUseCase,
    val observeReadingStatsUseCase: ObserveReadingStatsUseCase,
    val observeDiversityStatsUseCase: ObserveDiversityStatsUseCase,
    val observeSpendStatsUseCase: ObserveSpendStatsUseCase,
    val getCurrenciesUseCase: GetCurrenciesUseCase,
    val getUsualCurrencyAsFlowUseCase: GetUsualCurrencyAsFlowUseCase,
    val observePagesReadUseCase: ObservePagesReadUseCase,
    val observeDidNotFinishStatsUseCase: ObserveDidNotFinishStatsUseCase,
    val observeMonthlyPagesReadUseCase: ObserveMonthlyPagesReadUseCase,
    val observeHourlyPagesReadUseCase: ObserveHourlyPagesReadUseCase,
    val observeReadingActivityUseCase: ObserveReadingActivityUseCase,
    val observeReadingDayUseCase: ObserveReadingDayUseCase,
    val observeReadingHistoryUseCase: ObserveReadingHistoryUseCase,
    val observeReadingStatsBackfilledUseCase: ObserveReadingStatsBackfilledUseCase,
    val observeReadDatesBackfilledUseCase: ObserveReadDatesBackfilledUseCase,
    val refreshLibraryUseCase: RefreshLibraryUseCase,
    val observeTimeToFinishUseCase: ObserveTimeToFinishUseCase,
    val observeReadingListStatsUseCase: ObserveReadingListStatsUseCase,
    val observeLongestWaitingUseCase: ObserveLongestWaitingUseCase,
    val observeWrittenStatsUseCase: ObserveWrittenStatsUseCase,
    val observeQuotesSyncedUseCase: ObserveQuotesSyncedUseCase,
    val getHideBooksWithoutDetailAsFlowUseCase: GetHideBooksWithoutDetailAsFlowUseCase,
    val setHideBooksWithoutDetailUseCase: SetHideBooksWithoutDetailUseCase,
    val observeBooksByIdsUseCase: ObserveBooksByIdsUseCase,
    val observeCurrentDateUseCase: ObserveCurrentDateUseCase,
    val fillReaderEditionLanguagesUseCase: FillReaderEditionLanguagesUseCase,
    override val coroutineScope: CoroutineScope,
    override val mainDispatcher: CoroutineDispatcher,
) : ActionDependencies()
