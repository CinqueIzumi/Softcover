package nl.rhaydus.softcover.core.corpus.di

import nl.rhaydus.softcover.core.corpus.data.datasource.BookTraitsLocalDataSource
import nl.rhaydus.softcover.core.corpus.data.datasource.BookTraitsLocalDataSourceImpl
import nl.rhaydus.softcover.core.corpus.data.repository.BookTraitsRepositoryImpl
import nl.rhaydus.softcover.core.corpus.domain.repository.BookTraitsRepository
import nl.rhaydus.softcover.core.corpus.domain.usecase.GetBookTraitsUseCase
import nl.rhaydus.softcover.core.corpus.domain.usecase.ObserveBookTraitsUseCase
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
    factory { GetBookTraitsUseCase(repository = get()) }
    factory { SaveBookTraitsUseCase(repository = get()) }
}
