package nl.rhaydus.softcover.feature.profile.di

import nl.rhaydus.softcover.core.book.di.bookModule
import nl.rhaydus.softcover.core.domain.di.dispatcherModule
import nl.rhaydus.softcover.core.preferences.di.preferencesModule
import nl.rhaydus.softcover.core.presentation.di.presentationModule
import nl.rhaydus.softcover.core.profile.di.profileModule
import nl.rhaydus.softcover.feature.profile.presentation.collector.AtlasProfileDataCollector
import nl.rhaydus.softcover.feature.profile.presentation.collector.AtlasScopeChipsCollector
import nl.rhaydus.softcover.feature.profile.presentation.collector.AtlasScopedSectionsCollector
import nl.rhaydus.softcover.feature.profile.presentation.collector.DayDetailCollector
import nl.rhaydus.softcover.feature.profile.presentation.collector.GenreRankingCollector
import nl.rhaydus.softcover.feature.profile.presentation.collector.HideUntaggedAuthorsCollector
import nl.rhaydus.softcover.feature.profile.presentation.collector.ProfileCollector
import nl.rhaydus.softcover.feature.profile.presentation.collector.ReadingLifeCollector
import nl.rhaydus.softcover.feature.profile.presentation.collector.ReadingLifeShareCardCollector
import nl.rhaydus.softcover.feature.profile.presentation.collector.ReadingSummaryCollector
import nl.rhaydus.softcover.feature.profile.presentation.collector.RecentlyLovedCardsCollector
import nl.rhaydus.softcover.feature.profile.presentation.collector.StatsAtlasCollector
import nl.rhaydus.softcover.feature.profile.presentation.collector.UserInformationCollector
import nl.rhaydus.softcover.feature.profile.presentation.collector.YearGridCollector
import nl.rhaydus.softcover.feature.profile.presentation.collector.YearHistoryCollector
import nl.rhaydus.softcover.feature.profile.presentation.collector.YearMetricToggleCollector
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.ProfileScreenScreenModel
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.StatsAtlasScreenModel
import org.koin.dsl.bind
import org.koin.dsl.module

val profileScreenModule = module {
    includes(
        bookModule,
        dispatcherModule,
        profileModule,
        preferencesModule,
        presentationModule,
    )

    factory {
        ProfileScreenScreenModel(
            dispatchers = get(),
            initializers = getAll(),
            resetUserDataUseCase = get(),
            observeUserProfileDataUseCase = get(),
            observeReadingStatsUseCase = get(),
            observeReadingHistoryUseCase = get(),
            observeReadingActivityUseCase = get(),
            observeCurrentDateUseCase = get(),
            observeReadingStatsBackfilledUseCase = get(),
            refreshUserProfileStatsUseCase = get(),
        )
    }

    factory { UserInformationCollector() } bind ProfileCollector::class
    factory { ReadingLifeCollector() } bind ProfileCollector::class
    factory { ReadingLifeShareCardCollector() } bind ProfileCollector::class
    factory { ReadingSummaryCollector() } bind ProfileCollector::class

    factory {
        StatsAtlasScreenModel(
            dispatchers = get(),
            initializers = getAll(),
            observeUserProfileDataUseCase = get(),
            observeReadingStatsUseCase = get(),
            observeMonthlyPagesReadUseCase = get(),
            observeHourlyPagesReadUseCase = get(),
            observeReadingActivityUseCase = get(),
            observeReadingDayUseCase = get(),
            observeReadingHistoryUseCase = get(),
            observeReadingStatsBackfilledUseCase = get(),
            observeReadDatesBackfilledUseCase = get(),
            observeTimeToFinishUseCase = get(),
            getHideUntaggedAuthorsAsFlowUseCase = get(),
            setHideUntaggedAuthorsUseCase = get(),
            observeBooksByIdsUseCase = get(),
            observeCurrentDateUseCase = get(),
        )
    }

    factory { AtlasProfileDataCollector() } bind StatsAtlasCollector::class
    factory { AtlasScopeChipsCollector() } bind StatsAtlasCollector::class
    factory { RecentlyLovedCardsCollector() } bind StatsAtlasCollector::class
    factory { AtlasScopedSectionsCollector() } bind StatsAtlasCollector::class
    factory { HideUntaggedAuthorsCollector() } bind StatsAtlasCollector::class
    factory { YearMetricToggleCollector() } bind StatsAtlasCollector::class
    factory { YearHistoryCollector() } bind StatsAtlasCollector::class
    factory { GenreRankingCollector() } bind StatsAtlasCollector::class
    factory { YearGridCollector() } bind StatsAtlasCollector::class
    factory { DayDetailCollector() } bind StatsAtlasCollector::class
}
