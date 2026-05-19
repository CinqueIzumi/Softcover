package nl.rhaydus.softcover.feature.reading.di

import nl.rhaydus.softcover.feature.reading.presentation.initializer.BookDeadlinesCollector
import nl.rhaydus.softcover.feature.reading.presentation.initializer.CurrentlyReadingBooksCollector
import nl.rhaydus.softcover.feature.reading.presentation.initializer.DateStyleCollector
import nl.rhaydus.softcover.feature.reading.presentation.initializer.PlanTodayDismissalsCollector
import nl.rhaydus.softcover.feature.reading.presentation.initializer.ReadingInitializer
import nl.rhaydus.softcover.feature.reading.presentation.initializer.TrendingBooksLoader
import nl.rhaydus.softcover.feature.reading.presentation.initializer.WantToReadCollector
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenScreenModel
import nl.rhaydus.softcover.feature.reading.presentation.util.UpdateBookProgress
import org.koin.dsl.bind
import org.koin.dsl.module

val readingModule = module {
    factory {
        ReadingScreenScreenModel(
            getCurrentlyReadingBooksUseCase = get(),
            updateBookProgressUseCase = get(),
            markBookAsReadUseCase = get(),
            refreshUserBooksUseCase = get(),
            updateBookProgress = get(),
            observeAllBookDeadlinesUseCase = get(),
            getDateStyleAsFlowUseCase = get(),
            observePlanTodayDismissalsUseCase = get(),
            dismissPlanTodayUseCase = get(),
            getWantToReadUserBooksUseCase = get(),
            getTrendingBooksUseCase = get(),
            appDispatchers = get(),
            flows = getAll(),
        )
    }

    factory {
        UpdateBookProgress(
            markBookAsReadUseCase = get(),
            updateBookProgressUseCase = get(),
        )
    }

    factory { CurrentlyReadingBooksCollector() } bind ReadingInitializer::class
    factory { BookDeadlinesCollector() } bind ReadingInitializer::class
    factory { DateStyleCollector() } bind ReadingInitializer::class
    factory { PlanTodayDismissalsCollector() } bind ReadingInitializer::class
    factory { WantToReadCollector() } bind ReadingInitializer::class
    factory { TrendingBooksLoader() } bind ReadingInitializer::class
}