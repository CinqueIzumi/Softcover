package nl.rhaydus.softcover.core.corpus.di

import nl.rhaydus.softcover.core.corpus.data.datasource.BookCopyLocalDataSource
import nl.rhaydus.softcover.core.corpus.data.datasource.BookCopyLocalDataSourceImpl
import nl.rhaydus.softcover.core.corpus.data.datasource.BookMoodsLocalDataSource
import nl.rhaydus.softcover.core.corpus.data.datasource.BookMoodsLocalDataSourceImpl
import nl.rhaydus.softcover.core.corpus.data.datasource.BookTraitsLocalDataSource
import nl.rhaydus.softcover.core.corpus.data.datasource.BookTraitsLocalDataSourceImpl
import nl.rhaydus.softcover.core.corpus.data.datasource.DidNotFinishReasonsLocalDataSource
import nl.rhaydus.softcover.core.corpus.data.datasource.DidNotFinishReasonsLocalDataSourceImpl
import nl.rhaydus.softcover.core.corpus.data.datasource.MoodMomentsLocalDataSource
import nl.rhaydus.softcover.core.corpus.data.datasource.MoodMomentsLocalDataSourceImpl
import nl.rhaydus.softcover.core.corpus.data.repository.BookCopyRepositoryImpl
import nl.rhaydus.softcover.core.corpus.data.repository.BookMoodsRepositoryImpl
import nl.rhaydus.softcover.core.corpus.data.repository.BookTraitsRepositoryImpl
import nl.rhaydus.softcover.core.corpus.data.repository.DidNotFinishReasonsRepositoryImpl
import nl.rhaydus.softcover.core.corpus.data.repository.MoodMomentsRepositoryImpl
import nl.rhaydus.softcover.core.corpus.domain.repository.BookCopyRepository
import nl.rhaydus.softcover.core.corpus.domain.repository.BookMoodsRepository
import nl.rhaydus.softcover.core.corpus.domain.repository.BookTraitsRepository
import nl.rhaydus.softcover.core.corpus.domain.repository.DidNotFinishReasonsRepository
import nl.rhaydus.softcover.core.corpus.domain.repository.MoodMomentsRepository
import nl.rhaydus.softcover.core.corpus.domain.usecase.AddMoodMomentUseCase
import nl.rhaydus.softcover.core.corpus.domain.usecase.DeleteMoodMomentUseCase
import nl.rhaydus.softcover.core.corpus.domain.usecase.GetBookCopyUseCase
import nl.rhaydus.softcover.core.corpus.domain.usecase.GetBookTraitsUseCase
import nl.rhaydus.softcover.core.corpus.domain.usecase.GetDidNotFinishReasonsUseCase
import nl.rhaydus.softcover.core.corpus.domain.usecase.ObserveAllBookCopiesUseCase
import nl.rhaydus.softcover.core.corpus.domain.usecase.ObserveAllBookMoodsUseCase
import nl.rhaydus.softcover.core.corpus.domain.usecase.ObserveAllBookTraitsUseCase
import nl.rhaydus.softcover.core.corpus.domain.usecase.ObserveAllDidNotFinishReasonsUseCase
import nl.rhaydus.softcover.core.corpus.domain.usecase.ObserveAllMoodMomentsUseCase
import nl.rhaydus.softcover.core.corpus.domain.usecase.ObserveBookCopyUseCase
import nl.rhaydus.softcover.core.corpus.domain.usecase.ObserveBookMoodsUseCase
import nl.rhaydus.softcover.core.corpus.domain.usecase.ObserveBookTraitsUseCase
import nl.rhaydus.softcover.core.corpus.domain.usecase.ObserveDidNotFinishReasonsUseCase
import nl.rhaydus.softcover.core.corpus.domain.usecase.ObserveMoodMomentsUseCase
import nl.rhaydus.softcover.core.corpus.domain.usecase.ObserveOwnMoodWordsUseCase
import nl.rhaydus.softcover.core.corpus.domain.usecase.SaveBookCopyUseCase
import nl.rhaydus.softcover.core.corpus.domain.usecase.SaveBookMoodsUseCase
import nl.rhaydus.softcover.core.corpus.domain.usecase.SaveBookTraitsUseCase
import nl.rhaydus.softcover.core.corpus.domain.usecase.SaveDidNotFinishReasonsUseCase
import nl.rhaydus.softcover.core.corpus.domain.usecase.UpdateMoodMomentUseCase
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

    single { get<SoftcoverDatabase>().didNotFinishReasonsDao() }

    single<DidNotFinishReasonsLocalDataSource> {
        DidNotFinishReasonsLocalDataSourceImpl(dao = get())
    }

    single<DidNotFinishReasonsRepository> {
        DidNotFinishReasonsRepositoryImpl(localDataSource = get())
    }

    factory { ObserveDidNotFinishReasonsUseCase(repository = get()) }
    factory { ObserveAllDidNotFinishReasonsUseCase(repository = get()) }
    factory { GetDidNotFinishReasonsUseCase(repository = get()) }
    factory { SaveDidNotFinishReasonsUseCase(repository = get()) }

    single { get<SoftcoverDatabase>().bookMoodsDao() }

    single { get<SoftcoverDatabase>().moodMomentsDao() }

    single<BookMoodsLocalDataSource> {
        BookMoodsLocalDataSourceImpl(dao = get())
    }

    single<MoodMomentsLocalDataSource> {
        MoodMomentsLocalDataSourceImpl(dao = get())
    }

    single<BookMoodsRepository> {
        BookMoodsRepositoryImpl(localDataSource = get())
    }

    single<MoodMomentsRepository> {
        MoodMomentsRepositoryImpl(localDataSource = get())
    }

    factory { ObserveBookMoodsUseCase(repository = get()) }
    factory { ObserveAllBookMoodsUseCase(repository = get()) }
    factory { SaveBookMoodsUseCase(repository = get()) }
    factory { ObserveMoodMomentsUseCase(repository = get()) }
    factory { ObserveAllMoodMomentsUseCase(repository = get()) }
    factory { AddMoodMomentUseCase(repository = get()) }
    factory { UpdateMoodMomentUseCase(repository = get()) }
    factory { DeleteMoodMomentUseCase(repository = get()) }

    factory {
        ObserveOwnMoodWordsUseCase(
            bookMoodsRepository = get(),
            moodMomentsRepository = get(),
        )
    }
}
