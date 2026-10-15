package nl.rhaydus.softcover.core.personal.di

import nl.rhaydus.softcover.core.database.SoftcoverDatabase
import nl.rhaydus.softcover.core.database.di.databaseModule
import nl.rhaydus.softcover.core.domain.di.dispatcherModule
import nl.rhaydus.softcover.core.identity.di.identityModule
import nl.rhaydus.softcover.core.network.di.apolloModule
import nl.rhaydus.softcover.core.personal.data.datasource.HighlightLocalDataSource
import nl.rhaydus.softcover.core.personal.data.datasource.HighlightLocalDataSourceImpl
import nl.rhaydus.softcover.core.personal.data.datasource.ReadingJournalHistoryLocalDataSource
import nl.rhaydus.softcover.core.personal.data.datasource.ReadingJournalHistoryLocalDataSourceImpl
import nl.rhaydus.softcover.core.personal.data.datasource.ReadingJournalHistoryRemoteDataSource
import nl.rhaydus.softcover.core.personal.data.datasource.ReadingJournalHistoryRemoteDataSourceImpl
import nl.rhaydus.softcover.core.personal.data.datasource.ReadingSessionLocalDataSource
import nl.rhaydus.softcover.core.personal.data.datasource.ReadingSessionLocalDataSourceImpl
import nl.rhaydus.softcover.core.personal.data.repository.HighlightRepositoryImpl
import nl.rhaydus.softcover.core.personal.data.repository.ReadingJournalHistoryRepositoryImpl
import nl.rhaydus.softcover.core.personal.data.repository.ReadingSessionRepositoryImpl
import nl.rhaydus.softcover.core.personal.domain.repository.HighlightRepository
import nl.rhaydus.softcover.core.personal.domain.repository.ReadingJournalHistoryRepository
import nl.rhaydus.softcover.core.personal.domain.repository.ReadingSessionRepository
import nl.rhaydus.softcover.core.personal.domain.usecase.AddHighlightUseCase
import nl.rhaydus.softcover.core.personal.domain.usecase.DeleteHighlightUseCase
import nl.rhaydus.softcover.core.personal.domain.usecase.DeleteReadingSessionUseCase
import nl.rhaydus.softcover.core.personal.domain.usecase.ObserveActiveSessionUseCase
import nl.rhaydus.softcover.core.personal.domain.usecase.ObserveAllHighlightsUseCase
import nl.rhaydus.softcover.core.personal.domain.usecase.ObserveAllSessionsUseCase
import nl.rhaydus.softcover.core.personal.domain.usecase.ObserveHighlightsForBookUseCase
import nl.rhaydus.softcover.core.personal.domain.usecase.ObserveReadingJournalHistoryUseCase
import nl.rhaydus.softcover.core.personal.domain.usecase.ObserveSessionsForBookUseCase
import nl.rhaydus.softcover.core.personal.domain.usecase.PauseReadingSessionUseCase
import nl.rhaydus.softcover.core.personal.domain.usecase.RefreshReadingJournalHistoryUseCase
import nl.rhaydus.softcover.core.personal.domain.usecase.ResumeReadingSessionUseCase
import nl.rhaydus.softcover.core.personal.domain.usecase.StartReadingSessionUseCase
import nl.rhaydus.softcover.core.personal.domain.usecase.StopReadingSessionUseCase
import nl.rhaydus.softcover.core.personal.domain.usecase.UpdateHighlightUseCase
import org.koin.dsl.module

val personalModule = module {
    includes(
        dispatcherModule,
        databaseModule,
        apolloModule,
        identityModule,
    )

    single { get<SoftcoverDatabase>().highlightDao() }
    single { get<SoftcoverDatabase>().readingSessionDao() }

    single<HighlightLocalDataSource> { HighlightLocalDataSourceImpl(dao = get()) }
    single<ReadingSessionLocalDataSource> { ReadingSessionLocalDataSourceImpl(dao = get()) }

    single<HighlightRepository> { HighlightRepositoryImpl(localDataSource = get()) }
    single<ReadingSessionRepository> { ReadingSessionRepositoryImpl(localDataSource = get()) }

    factory { ObserveHighlightsForBookUseCase(repository = get()) }
    factory { ObserveAllHighlightsUseCase(repository = get()) }
    factory { AddHighlightUseCase(repository = get()) }
    factory { UpdateHighlightUseCase(repository = get()) }
    factory { DeleteHighlightUseCase(repository = get()) }

    factory { ObserveSessionsForBookUseCase(repository = get()) }
    factory { ObserveAllSessionsUseCase(repository = get()) }
    factory { ObserveActiveSessionUseCase(repository = get()) }
    factory { StartReadingSessionUseCase(repository = get()) }
    factory { StopReadingSessionUseCase(repository = get()) }
    factory { PauseReadingSessionUseCase(repository = get()) }
    factory { ResumeReadingSessionUseCase(repository = get()) }
    factory { DeleteReadingSessionUseCase(repository = get()) }

    single<ReadingJournalHistoryRemoteDataSource> {
        ReadingJournalHistoryRemoteDataSourceImpl(
            apolloClient = get(),
            getUserIdUseCase = get(),
        )
    }

    single<ReadingJournalHistoryLocalDataSource> {
        ReadingJournalHistoryLocalDataSourceImpl(dao = get<SoftcoverDatabase>().progressHistoryDao())
    }

    single<ReadingJournalHistoryRepository> {
        ReadingJournalHistoryRepositoryImpl(
            readingJournalHistoryRemoteDataSource = get(),
            readingJournalHistoryLocalDataSource = get(),
        )
    }

    factory { ObserveReadingJournalHistoryUseCase(repository = get()) }
    factory { RefreshReadingJournalHistoryUseCase(repository = get()) }
}
