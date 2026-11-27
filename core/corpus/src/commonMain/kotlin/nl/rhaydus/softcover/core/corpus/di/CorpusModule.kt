package nl.rhaydus.softcover.core.corpus.di

import nl.rhaydus.softcover.core.corpus.data.datasource.BookCopyLocalDataSource
import nl.rhaydus.softcover.core.corpus.data.datasource.BookCopyLocalDataSourceImpl
import nl.rhaydus.softcover.core.corpus.data.datasource.BookTraitsLocalDataSource
import nl.rhaydus.softcover.core.corpus.data.datasource.BookTraitsLocalDataSourceImpl
import nl.rhaydus.softcover.core.corpus.data.repository.BookCopyRepositoryImpl
import nl.rhaydus.softcover.core.corpus.data.repository.BookTraitsRepositoryImpl
import nl.rhaydus.softcover.core.corpus.domain.repository.BookCopyRepository
import nl.rhaydus.softcover.core.corpus.domain.repository.BookTraitsRepository
import nl.rhaydus.softcover.core.corpus.domain.usecase.GetBookCopyUseCase
import nl.rhaydus.softcover.core.corpus.domain.usecase.GetBookTraitsUseCase
import nl.rhaydus.softcover.core.corpus.domain.usecase.ObserveAllBookCopiesUseCase
import nl.rhaydus.softcover.core.corpus.domain.usecase.ObserveAllBookTraitsUseCase
import nl.rhaydus.softcover.core.corpus.domain.usecase.ObserveBookCopyUseCase
import nl.rhaydus.softcover.core.corpus.domain.usecase.ObserveBookTraitsUseCase
import nl.rhaydus.softcover.core.corpus.domain.usecase.SaveBookCopyUseCase
import nl.rhaydus.softcover.core.corpus.domain.usecase.SaveBookTraitsUseCase
import nl.rhaydus.softcover.core.database.SoftcoverDatabase
import nl.rhaydus.softcover.core.database.di.databaseModule
import nl.rhaydus.softcover.core.domain.di.dispatcherModule
import org.koin.dsl.module

val corpusModule = module {
    includes(
        dispatcherModule,
        databaseModule,
    )

    single { get<SoftcoverDatabase>().bookTraitsDao() }

    single<BookTraitsLocalDataSource> {
        BookTraitsLocalDataSourceImpl(dao = get())
    }

    single<BookTraitsRepository> {
        BookTraitsRepositoryImpl(localDataSource = get())
    }

    factory { ObserveBookTraitsUseCase(repository = get()) }
    factory { ObserveAllBookTraitsUseCase(repository = get()) }
    factory { GetBookTraitsUseCase(repository = get()) }
    factory { SaveBookTraitsUseCase(repository = get()) }

    single { get<SoftcoverDatabase>().bookCopyDao() }

    single<BookCopyLocalDataSource> {
        BookCopyLocalDataSourceImpl(dao = get())
    }

    single<BookCopyRepository> {
        BookCopyRepositoryImpl(localDataSource = get())
    }

    factory { ObserveBookCopyUseCase(repository = get()) }
    factory { ObserveAllBookCopiesUseCase(repository = get()) }
    factory { GetBookCopyUseCase(repository = get()) }
    factory { SaveBookCopyUseCase(repository = get()) }
}
