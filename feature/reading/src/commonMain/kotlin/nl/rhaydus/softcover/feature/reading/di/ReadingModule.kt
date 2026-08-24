package nl.rhaydus.softcover.feature.reading.di

import org.koin.dsl.bind
import org.koin.dsl.module
import nl.rhaydus.softcover.core.book.di.bookModule
import nl.rhaydus.softcover.core.deadlines.di.deadlinesModule
import nl.rhaydus.softcover.core.designsystem.presentation.di.designSystemModule
import nl.rhaydus.softcover.core.domain.di.dispatcherModule
import nl.rhaydus.softcover.core.notification.di.notificationModule
import nl.rhaydus.softcover.core.personal.di.personalModule
import nl.rhaydus.softcover.core.preferences.di.preferencesModule
import nl.rhaydus.softcover.core.profile.di.profileModule
import nl.rhaydus.softcover.feature.reading.presentation.collector.BookDeadlinesCollector
import nl.rhaydus.softcover.feature.reading.presentation.collector.CurrentlyReadingBooksCollector
import nl.rhaydus.softcover.feature.reading.presentation.collector.DateStyleCollector
import nl.rhaydus.softcover.feature.reading.presentation.collector.LastUsedProgressUnitCollector
import nl.rhaydus.softcover.feature.reading.presentation.collector.PlanTodayDismissalsCollector
import nl.rhaydus.softcover.feature.reading.presentation.collector.ReadingActivityCollector
import nl.rhaydus.softcover.feature.reading.presentation.collector.ReadingCollector
import nl.rhaydus.softcover.feature.reading.presentation.collector.ReadingPaceForecastCollector
import nl.rhaydus.softcover.feature.reading.presentation.collector.TrendingBooksLoader
import nl.rhaydus.softcover.feature.reading.presentation.collector.WantToReadCollector
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenScreenModel

val readingModule = module {
    includes(
        dispatcherModule,
        bookModule,
        deadlinesModule,
        personalModule,
        preferencesModule,
        profileModule,
        notificationModule,
        designSystemModule,
    )

    factory {
        ReadingScreenScreenModel(
            getCurrentlyReadingBooksUseCase = get(),
            updateBookProgressUseCase = get(),
            markBookAsReadUseCase = get(),
            saveBookVerdictUseCase = get(),
            refreshLibraryUseCase = get(),
            recordBookProgressUseCase = get(),
            observeAllBookDeadlinesUseCase = get(),
            getDateStyleAsFlowUseCase = get(),
            observePlanTodayDismissalsUseCase = get(),
            dismissPlanTodayUseCase = get(),
            getWantToReadUserBooksUseCase = get(),
            getTrendingBooksUseCase = get(),
            observeRecentReadingActivityUseCase = get(),
            refreshReadingActivityUseCase = get(),
            getReadingStreakEnabledAsFlowUseCase = get(),
            getLastUsedProgressUnitAsFlowUseCase = get(),
            setLastUsedProgressUnitUseCase = get(),
            getReadingJournalHistoryUseCase = get(),
            appDispatchers = get(),
            flows = getAll(),
        )
    }

    factory { CurrentlyReadingBooksCollector() } bind ReadingCollector::class
    factory { BookDeadlinesCollector() } bind ReadingCollector::class
    factory { DateStyleCollector() } bind ReadingCollector::class
    factory { PlanTodayDismissalsCollector() } bind ReadingCollector::class
    factory { WantToReadCollector() } bind ReadingCollector::class
    factory { TrendingBooksLoader() } bind ReadingCollector::class
    factory { ReadingActivityCollector() } bind ReadingCollector::class
    factory { LastUsedProgressUnitCollector() } bind ReadingCollector::class
    factory { ReadingPaceForecastCollector() } bind ReadingCollector::class
}
