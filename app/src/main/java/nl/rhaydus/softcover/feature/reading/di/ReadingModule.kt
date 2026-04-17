package nl.rhaydus.softcover.feature.reading.di

import nl.rhaydus.softcover.feature.reading.presentation.initializer.CurrentlyReadingBooksCollector
import nl.rhaydus.softcover.feature.reading.presentation.initializer.ReadingInitializer
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
            initializeUserBooksUseCase = get(),
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
}