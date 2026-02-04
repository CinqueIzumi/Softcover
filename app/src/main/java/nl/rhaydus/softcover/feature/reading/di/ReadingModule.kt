package nl.rhaydus.softcover.feature.reading.di

import nl.rhaydus.softcover.feature.reading.data.datasource.BookRemoteDataSource
import nl.rhaydus.softcover.feature.reading.data.datasource.BookRemoteDataSourceImpl
import nl.rhaydus.softcover.feature.reading.data.repository.BooksRepositoryImpl
import nl.rhaydus.softcover.feature.reading.domain.repository.BooksRepository
import nl.rhaydus.softcover.feature.reading.domain.usecase.MarkBookAsReadUseCase
import nl.rhaydus.softcover.feature.reading.domain.usecase.UpdateBookEditionUseCase
import nl.rhaydus.softcover.feature.reading.domain.usecase.UpdateBookProgressUseCase
import nl.rhaydus.softcover.feature.reading.presentation.initializer.CurrentlyReadingBooksCollector
import nl.rhaydus.softcover.feature.reading.presentation.initializer.ReadingInitializer
import nl.rhaydus.softcover.feature.reading.presentation.util.UpdateBookProgress
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenScreenModel
import org.koin.dsl.bind
import org.koin.dsl.module

val readingModule = module {
    single {
        ReadingScreenScreenModel(
            getCurrentlyReadingBooksUseCase = get(),
            updateBookProgressUseCase = get(),
            markBookAsReadUseCase = get(),
            refreshUserBooksUseCase = get(),
            updateBookEditionUseCase = get(),
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

    single<BookRemoteDataSource> {
        BookRemoteDataSourceImpl(apolloClient = get())
    }

    single<BooksRepository> {
        BooksRepositoryImpl(bookRemoteDataSource = get())
    }

    factory {
        MarkBookAsReadUseCase(
            repository = get(),
            cachingRepository = get()
        )
    }

    factory {
        UpdateBookEditionUseCase(
            repository = get(),
            cachingRepository = get(),
        )
    }

    factory {
        UpdateBookProgressUseCase(
            repository = get(),
            cachingRepository = get()
        )
    }
}