package nl.rhaydus.softcover.feature.personal.di

import nl.rhaydus.softcover.core.data.database.SoftcoverDatabase
import nl.rhaydus.softcover.feature.personal.data.datasource.HighlightLocalDataSource
import nl.rhaydus.softcover.feature.personal.data.datasource.HighlightLocalDataSourceImpl
import nl.rhaydus.softcover.feature.personal.data.datasource.ReadingLogLocalDataSource
import nl.rhaydus.softcover.feature.personal.data.datasource.ReadingLogLocalDataSourceImpl
import nl.rhaydus.softcover.feature.personal.data.datasource.ReadingSessionLocalDataSource
import nl.rhaydus.softcover.feature.personal.data.datasource.ReadingSessionLocalDataSourceImpl
import nl.rhaydus.softcover.feature.personal.data.repository.HighlightRepositoryImpl
import nl.rhaydus.softcover.feature.personal.data.repository.ReadingLogRepositoryImpl
import nl.rhaydus.softcover.feature.personal.data.repository.ReadingSessionRepositoryImpl
import nl.rhaydus.softcover.feature.personal.domain.repository.HighlightRepository
import nl.rhaydus.softcover.feature.personal.domain.repository.ReadingLogRepository
import nl.rhaydus.softcover.feature.personal.domain.repository.ReadingSessionRepository
import nl.rhaydus.softcover.feature.personal.domain.usecase.AddHighlightUseCase
import nl.rhaydus.softcover.feature.personal.domain.usecase.AddReadingLogEntryUseCase
import nl.rhaydus.softcover.feature.personal.domain.usecase.DeleteHighlightUseCase
import nl.rhaydus.softcover.feature.personal.domain.usecase.DeleteReadingLogEntryUseCase
import nl.rhaydus.softcover.feature.personal.domain.usecase.DeleteReadingSessionUseCase
import nl.rhaydus.softcover.feature.personal.domain.usecase.ObserveActiveSessionUseCase
import nl.rhaydus.softcover.feature.personal.domain.usecase.ObserveAllHighlightsUseCase
import nl.rhaydus.softcover.feature.personal.domain.usecase.ObserveAllSessionsUseCase
import nl.rhaydus.softcover.feature.personal.domain.usecase.ObserveHighlightsForBookUseCase
import nl.rhaydus.softcover.feature.personal.domain.usecase.ObserveReadingLogCountUseCase
import nl.rhaydus.softcover.feature.personal.domain.usecase.ObserveReadingLogForBookUseCase
import nl.rhaydus.softcover.feature.personal.domain.usecase.ObserveSessionsForBookUseCase
import nl.rhaydus.softcover.feature.personal.domain.usecase.StartReadingSessionUseCase
import nl.rhaydus.softcover.feature.personal.domain.usecase.StopReadingSessionUseCase
import nl.rhaydus.softcover.feature.personal.domain.usecase.UpdateHighlightUseCase
import nl.rhaydus.softcover.feature.personal.domain.usecase.UpdateReadingLogEntryUseCase
import org.koin.dsl.module

val personalModule = module {
    single { get<SoftcoverDatabase>().highlightDao() }
    single { get<SoftcoverDatabase>().readingSessionDao() }
    single { get<SoftcoverDatabase>().readingLogDao() }

    single<HighlightLocalDataSource> { HighlightLocalDataSourceImpl(dao = get()) }
    single<ReadingSessionLocalDataSource> { ReadingSessionLocalDataSourceImpl(dao = get()) }
    single<ReadingLogLocalDataSource> { ReadingLogLocalDataSourceImpl(dao = get()) }

    single<HighlightRepository> { HighlightRepositoryImpl(localDataSource = get()) }
    single<ReadingSessionRepository> { ReadingSessionRepositoryImpl(localDataSource = get()) }
    single<ReadingLogRepository> { ReadingLogRepositoryImpl(localDataSource = get()) }

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
    factory { DeleteReadingSessionUseCase(repository = get()) }

    factory { ObserveReadingLogForBookUseCase(repository = get()) }
    factory { ObserveReadingLogCountUseCase(repository = get()) }
    factory { AddReadingLogEntryUseCase(repository = get()) }
    factory { UpdateReadingLogEntryUseCase(repository = get()) }
    factory { DeleteReadingLogEntryUseCase(repository = get()) }
}
