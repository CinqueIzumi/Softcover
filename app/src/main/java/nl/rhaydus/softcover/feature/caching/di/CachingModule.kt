package nl.rhaydus.softcover.feature.caching.di

import nl.rhaydus.softcover.core.data.database.SoftcoverDatabase
import nl.rhaydus.softcover.feature.caching.data.database.BookDao
import nl.rhaydus.softcover.feature.caching.data.datasource.CachingLocalDataSource
import nl.rhaydus.softcover.feature.caching.data.datasource.CachingLocalDataSourceImpl
import nl.rhaydus.softcover.feature.caching.data.datasource.CachingRemoteDataSource
import nl.rhaydus.softcover.feature.caching.data.datasource.CachingRemoteDataSourceImpl
import nl.rhaydus.softcover.feature.caching.data.repository.CachingRepositoryImpl
import nl.rhaydus.softcover.feature.caching.domain.repository.CachingRepository
import nl.rhaydus.softcover.feature.caching.domain.usecase.GetAllUserBooksUseCase
import nl.rhaydus.softcover.feature.caching.domain.usecase.GetCurrentlyReadingUserBooksUseCase
import nl.rhaydus.softcover.feature.caching.domain.usecase.GetDidNotFinishUserBooksUseCase
import nl.rhaydus.softcover.feature.caching.domain.usecase.GetReadUserBooksUseCase
import nl.rhaydus.softcover.feature.caching.domain.usecase.GetWantToReadUserBooksUseCase
import nl.rhaydus.softcover.feature.caching.domain.usecase.InitializeUserBooksUseCase
import nl.rhaydus.softcover.feature.caching.domain.usecase.RefreshUserBooksUseCase
import org.koin.dsl.factory
import org.koin.dsl.module

val cachingModule = module {
    single<CachingRepository> {
        CachingRepositoryImpl(
            cachingLocalDataSource = get(),
            cachingRemoteDataSource = get()
        )
    }

    single<CachingRemoteDataSource> {
        CachingRemoteDataSourceImpl(apolloClient = get())
    }

    single<CachingLocalDataSource> {
        CachingLocalDataSourceImpl(dao = get())
    }

    single<BookDao> {
        get<SoftcoverDatabase>().bookDao()
    }

    factory {
        InitializeUserBooksUseCase(
            cachingRepository = get(),
            getUserIdUseCase = get()
        )
    }

    factory {
        GetAllUserBooksUseCase(cachingRepository = get())
    }

    factory {
        GetCurrentlyReadingUserBooksUseCase(cachingRepository = get())
    }

    factory {
        GetDidNotFinishUserBooksUseCase(cachingRepository = get())
    }

    factory {
        GetReadUserBooksUseCase(cachingRepository = get())
    }

    factory {
        GetWantToReadUserBooksUseCase(cachingRepository = get())
    }

    factory {
        RefreshUserBooksUseCase(initializeUserBooksUseCase = get())
    }
}