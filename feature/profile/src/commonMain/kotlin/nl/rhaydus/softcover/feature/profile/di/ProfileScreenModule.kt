package nl.rhaydus.softcover.feature.profile.di

import nl.rhaydus.softcover.core.book.di.bookModule
import nl.rhaydus.softcover.core.domain.di.dispatcherModule
import nl.rhaydus.softcover.core.preferences.di.preferencesModule
import nl.rhaydus.softcover.core.presentation.di.presentationModule
import nl.rhaydus.softcover.core.profile.di.profileModule
import nl.rhaydus.softcover.feature.profile.presentation.collector.AtlasChartsCollector
import nl.rhaydus.softcover.feature.profile.presentation.collector.AtlasProfileDataCollector
import nl.rhaydus.softcover.feature.profile.presentation.collector.AtlasReadingLifeCollector
import nl.rhaydus.softcover.feature.profile.presentation.collector.HideUntaggedAuthorsCollector
import nl.rhaydus.softcover.feature.profile.presentation.collector.ProfileCollector
import nl.rhaydus.softcover.feature.profile.presentation.collector.ReadingLifeCollector
import nl.rhaydus.softcover.feature.profile.presentation.collector.ReadingLifeShareCardCollector
import nl.rhaydus.softcover.feature.profile.presentation.collector.ReadingSummaryCollector
import nl.rhaydus.softcover.feature.profile.presentation.collector.RecentlyLovedCardsCollector
import nl.rhaydus.softcover.feature.profile.presentation.collector.StatsAtlasCollector
import nl.rhaydus.softcover.feature.profile.presentation.collector.UserInformationCollector
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
            observeReadingLifeUseCase = get(),
            refreshUserProfileDataUseCase = get(),
            clock = get(),
            timeZone = get(),
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
            observeReadingLifeUseCase = get(),
            getHideUntaggedAuthorsAsFlowUseCase = get(),
            setHideUntaggedAuthorsUseCase = get(),
            observeBooksByIdsUseCase = get(),
        )
    }

    factory { AtlasProfileDataCollector() } bind StatsAtlasCollector::class
    factory { AtlasReadingLifeCollector() } bind StatsAtlasCollector::class
    factory { RecentlyLovedCardsCollector() } bind StatsAtlasCollector::class
    factory { AtlasChartsCollector() } bind StatsAtlasCollector::class
    factory { HideUntaggedAuthorsCollector() } bind StatsAtlasCollector::class
    factory { YearMetricToggleCollector() } bind StatsAtlasCollector::class
    factory { YearHistoryCollector() } bind StatsAtlasCollector::class
}
