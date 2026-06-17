package nl.rhaydus.softcover.feature.reading.di

import nl.rhaydus.softcover.feature.reading.presentation.collector.BookDeadlinesCollector
import nl.rhaydus.softcover.feature.reading.presentation.collector.CurrentlyReadingBooksCollector
import nl.rhaydus.softcover.feature.reading.presentation.collector.DateStyleCollector
import nl.rhaydus.softcover.feature.reading.presentation.collector.PlanTodayDismissalsCollector
import nl.rhaydus.softcover.feature.reading.presentation.collector.ReadingActivityCollector
import nl.rhaydus.softcover.feature.reading.presentation.collector.ReadingCollector
import nl.rhaydus.softcover.feature.reading.presentation.collector.TrendingBooksLoader
import nl.rhaydus.softcover.feature.reading.presentation.collector.WantToReadCollector
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenScreenModel
import org.koin.dsl.bind
import org.koin.dsl.module

val readingModule = module {
    factory {
        ReadingScreenScreenModel(
            getCurrentlyReadingBooksUseCase = get(),
            updateBookProgressUseCase = get(),
            markBookAsReadUseCase = get(),
            refreshLibraryUseCase = get(),
            recordBookProgressUseCase = get(),
            observeAllBookDeadlinesUseCase = get(),
            getDateStyleAsFlowUseCase = get(),
            observePlanTodayDismissalsUseCase = get(),
            dismissPlanTodayUseCase = get(),
            getWantToReadUserBooksUseCase = get(),
            getTrendingBooksUseCase = get(),
            observeRecentReadingActivityUseCase = get(),
            refreshUserProfileDataUseCase = get(),
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
}
