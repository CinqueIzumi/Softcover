package nl.rhaydus.softcover.core.deadlines.di

import nl.rhaydus.softcover.core.database.SoftcoverDatabase
import nl.rhaydus.softcover.core.deadlines.data.datasource.BookDeadlineLocalDataSource
import nl.rhaydus.softcover.core.deadlines.data.datasource.BookDeadlineLocalDataSourceImpl
import nl.rhaydus.softcover.core.deadlines.data.repository.BookDeadlineRepositoryImpl
import nl.rhaydus.softcover.core.deadlines.domain.repository.BookDeadlineRepository
import nl.rhaydus.softcover.core.deadlines.domain.usecase.ClearBookDeadlineUseCase
import nl.rhaydus.softcover.core.deadlines.domain.usecase.ObserveAllBookDeadlinesUseCase
import nl.rhaydus.softcover.core.deadlines.domain.usecase.ObserveBookDeadlineUseCase
import nl.rhaydus.softcover.core.deadlines.domain.usecase.SetBookDeadlineUseCase
import org.koin.dsl.module

val deadlinesModule = module {
    single { get<SoftcoverDatabase>().bookDeadlineDao() }

    single<BookDeadlineLocalDataSource> {
        BookDeadlineLocalDataSourceImpl(dao = get())
    }

    single<BookDeadlineRepository> {
        BookDeadlineRepositoryImpl(localDataSource = get())
    }

    factory { ObserveBookDeadlineUseCase(repository = get()) }
    factory { ObserveAllBookDeadlinesUseCase(repository = get()) }
    factory { SetBookDeadlineUseCase(repository = get()) }
    factory { ClearBookDeadlineUseCase(repository = get()) }
}
