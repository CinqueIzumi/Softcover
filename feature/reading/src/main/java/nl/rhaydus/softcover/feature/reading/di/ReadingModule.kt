package nl.rhaydus.softcover.feature.reading.di

import nl.rhaydus.softcover.feature.reading.presentation.flows.BookDeadlinesCollector
import nl.rhaydus.softcover.feature.reading.presentation.flows.CurrentlyReadingBooksCollector
import nl.rhaydus.softcover.feature.reading.presentation.flows.DateStyleCollector
import nl.rhaydus.softcover.feature.reading.presentation.flows.PlanTodayDismissalsCollector
import nl.rhaydus.softcover.feature.reading.presentation.flows.ReadingActivityCollector
import nl.rhaydus.softcover.feature.reading.presentation.flows.ReadingInitializer
import nl.rhaydus.softcover.feature.reading.presentation.flows.TrendingBooksLoader
import nl.rhaydus.softcover.feature.reading.presentation.flows.WantToReadCollector
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

    factory { CurrentlyReadingBooksCollector() } bind ReadingInitializer::class
    factory { BookDeadlinesCollector() } bind ReadingInitializer::class
    factory { DateStyleCollector() } bind ReadingInitializer::class
    factory { PlanTodayDismissalsCollector() } bind ReadingInitializer::class
    factory { WantToReadCollector() } bind ReadingInitializer::class
    factory { TrendingBooksLoader() } bind ReadingInitializer::class
    factory { ReadingActivityCollector() } bind ReadingInitializer::class
}
