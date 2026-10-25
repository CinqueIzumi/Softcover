package nl.rhaydus.softcover.core.personal.di

import nl.rhaydus.softcover.core.database.SoftcoverDatabase
import nl.rhaydus.softcover.core.database.di.databaseModule
import nl.rhaydus.softcover.core.domain.di.dispatcherModule
import nl.rhaydus.softcover.core.identity.di.identityModule
import nl.rhaydus.softcover.core.network.di.apolloModule
import nl.rhaydus.softcover.core.personal.data.datasource.QuoteLocalDataSource
import nl.rhaydus.softcover.core.personal.data.datasource.QuoteLocalDataSourceImpl
import nl.rhaydus.softcover.core.personal.data.datasource.QuoteRemoteDataSource
import nl.rhaydus.softcover.core.personal.data.datasource.QuoteRemoteDataSourceImpl
import nl.rhaydus.softcover.core.personal.data.datasource.ReadingJournalHistoryLocalDataSource
import nl.rhaydus.softcover.core.personal.data.datasource.ReadingJournalHistoryLocalDataSourceImpl
import nl.rhaydus.softcover.core.personal.data.datasource.ReadingJournalHistoryRemoteDataSource
import nl.rhaydus.softcover.core.personal.data.datasource.ReadingJournalHistoryRemoteDataSourceImpl
import nl.rhaydus.softcover.core.personal.data.datasource.ReadingSessionLocalDataSource
import nl.rhaydus.softcover.core.personal.data.datasource.ReadingSessionLocalDataSourceImpl
import nl.rhaydus.softcover.core.personal.data.repository.QuoteRepositoryImpl
import nl.rhaydus.softcover.core.personal.data.repository.ReadingJournalHistoryRepositoryImpl
import nl.rhaydus.softcover.core.personal.data.repository.ReadingSessionRepositoryImpl
import nl.rhaydus.softcover.core.personal.domain.repository.QuoteRepository
import nl.rhaydus.softcover.core.personal.domain.repository.ReadingJournalHistoryRepository
import nl.rhaydus.softcover.core.personal.domain.repository.ReadingSessionRepository
import nl.rhaydus.softcover.core.personal.domain.usecase.AddQuoteUseCase
import nl.rhaydus.softcover.core.personal.domain.usecase.DeleteQuoteUseCase
import nl.rhaydus.softcover.core.personal.domain.usecase.DeleteReadingSessionUseCase
import nl.rhaydus.softcover.core.personal.domain.usecase.ObserveActiveSessionUseCase
import nl.rhaydus.softcover.core.personal.domain.usecase.ObserveAllQuotesUseCase
import nl.rhaydus.softcover.core.personal.domain.usecase.ObserveAllSessionsUseCase
import nl.rhaydus.softcover.core.personal.domain.usecase.ObserveBookQuotesUseCase
import nl.rhaydus.softcover.core.personal.domain.usecase.ObserveReadingJournalHistoryUseCase
import nl.rhaydus.softcover.core.personal.domain.usecase.ObserveSessionsForBookUseCase
import nl.rhaydus.softcover.core.personal.domain.usecase.PauseReadingSessionUseCase
import nl.rhaydus.softcover.core.personal.domain.usecase.RefreshAllQuotesUseCase
import nl.rhaydus.softcover.core.personal.domain.usecase.RefreshAllReadingJournalHistoryUseCase
import nl.rhaydus.softcover.core.personal.domain.usecase.RefreshBookQuotesUseCase
import nl.rhaydus.softcover.core.personal.domain.usecase.RefreshReadingJournalHistoryUseCase
import nl.rhaydus.softcover.core.personal.domain.usecase.ResumeReadingSessionUseCase
import nl.rhaydus.softcover.core.personal.domain.usecase.StartReadingSessionUseCase
import nl.rhaydus.softcover.core.personal.domain.usecase.StopReadingSessionUseCase
import org.koin.dsl.module
import kotlin.time.Clock

val personalModule = module {
    includes(
        dispatcherModule,
        databaseModule,
        apolloModule,
        identityModule,
    )

    single { get<SoftcoverDatabase>().readingSessionDao() }

    single<ReadingSessionLocalDataSource> { ReadingSessionLocalDataSourceImpl(dao = get()) }

    single<ReadingSessionRepository> { ReadingSessionRepositoryImpl(localDataSource = get()) }

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
    factory { RefreshAllReadingJournalHistoryUseCase(repository = get()) }

    single<QuoteLocalDataSource> {
        QuoteLocalDataSourceImpl(dao = get<SoftcoverDatabase>().quoteDao())
    }

    single<QuoteRemoteDataSource> {
        QuoteRemoteDataSourceImpl(
            apolloClient = get(),
            getUserIdUseCase = get(),
        )
    }

    single<QuoteRepository> {
        QuoteRepositoryImpl(
            localDataSource = get(),
            remoteDataSource = get(),
            drainer = get(),
            clock = Clock.System,
        )
    }

    factory { ObserveBookQuotesUseCase(repository = get()) }
    factory { RefreshBookQuotesUseCase(repository = get()) }
    factory { ObserveAllQuotesUseCase(repository = get()) }
    factory { RefreshAllQuotesUseCase(repository = get()) }
    factory { AddQuoteUseCase(repository = get()) }
    factory { DeleteQuoteUseCase(repository = get()) }
}
