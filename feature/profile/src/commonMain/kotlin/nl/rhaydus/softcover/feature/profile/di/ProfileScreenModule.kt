package nl.rhaydus.softcover.feature.profile.di

import nl.rhaydus.softcover.core.domain.di.dispatcherModule
import nl.rhaydus.softcover.core.preferences.di.preferencesModule
import nl.rhaydus.softcover.core.presentation.di.presentationModule
import nl.rhaydus.softcover.core.profile.di.profileModule
import nl.rhaydus.softcover.feature.profile.presentation.collector.HideUntaggedAuthorsCollector
import nl.rhaydus.softcover.feature.profile.presentation.collector.ProfileChartsCollector
import nl.rhaydus.softcover.feature.profile.presentation.collector.ProfileCollector
import nl.rhaydus.softcover.feature.profile.presentation.collector.ReadingLifeCollector
import nl.rhaydus.softcover.feature.profile.presentation.collector.RecentlyLovedCardsCollector
import nl.rhaydus.softcover.feature.profile.presentation.collector.UserInformationCollector
import nl.rhaydus.softcover.feature.profile.presentation.collector.YearHistoryCollector
import nl.rhaydus.softcover.feature.profile.presentation.collector.YearMetricToggleCollector
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.ProfileScreenScreenModel
import org.koin.dsl.bind
import org.koin.dsl.module

val profileScreenModule = module {
    includes(
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
            getHideUntaggedAuthorsAsFlowUseCase = get(),
            setHideUntaggedAuthorsUseCase = get(),
        )
    }

    factory { UserInformationCollector() } bind ProfileCollector::class
    factory { ReadingLifeCollector() } bind ProfileCollector::class
    factory { RecentlyLovedCardsCollector() } bind ProfileCollector::class
    factory { ProfileChartsCollector() } bind ProfileCollector::class
    factory { HideUntaggedAuthorsCollector() } bind ProfileCollector::class
    factory { YearMetricToggleCollector() } bind ProfileCollector::class
    factory { YearHistoryCollector() } bind ProfileCollector::class
}
