package nl.rhaydus.softcover.feature.search.di

import nl.rhaydus.softcover.feature.search.data.datasource.SearchLocalDataSource
import nl.rhaydus.softcover.feature.search.data.datasource.SearchLocalDataSourceImpl
import nl.rhaydus.softcover.feature.search.data.datasource.SearchRemoteDataSource
import nl.rhaydus.softcover.feature.search.data.datasource.SearchRemoteDataSourceImpl
import nl.rhaydus.softcover.feature.search.data.datastore.SearchHistoryDataStore
import nl.rhaydus.softcover.feature.search.data.datastore.searchHistory
import nl.rhaydus.softcover.feature.search.data.repository.SearchRepositoryImpl
import nl.rhaydus.softcover.feature.search.domain.repository.SearchRepository
import nl.rhaydus.softcover.feature.search.domain.usecase.GetPreviousSearchQueriesUseCase
import nl.rhaydus.softcover.feature.search.domain.usecase.GetQueriedBooksUseCase
import nl.rhaydus.softcover.feature.search.domain.usecase.RemoveAllSearchQueriesUseCase
import nl.rhaydus.softcover.feature.search.domain.usecase.RemoveSearchQueryUseCase
import nl.rhaydus.softcover.feature.search.domain.usecase.SearchForNameUseCase
import nl.rhaydus.softcover.feature.search.presentation.flows.PreviousQueriesCollector
import nl.rhaydus.softcover.feature.search.presentation.flows.QueriedBooksCollector
import nl.rhaydus.softcover.feature.search.presentation.flows.SearchInitializer
import nl.rhaydus.softcover.feature.search.presentation.screenmodel.SearchScreenScreenModel
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module

val searchModule = module {
    factory {
        SearchScreenScreenModel(
            getPreviousSearchQueriesUseCase = get(),
            getQueriedBooksUseCase = get(),
            searchForNameUseCase = get(),
            getAllUserBooksUseCase = get(),
            removeSearchQueryUseCase = get(),
            removeAllSearchQueriesUseCase = get(),
            markBookAsWantToReadUseCase = get(),
            removeBookFromLibraryUseCase = get(),
            flows = getAll(),
            appDispatchers = get(),
        )
    }

    factory { PreviousQueriesCollector() } bind SearchInitializer::class

    factory { QueriedBooksCollector() } bind SearchInitializer::class

    single<SearchLocalDataSource> {
        SearchLocalDataSourceImpl(dataStore = get())
    }

    single<SearchRemoteDataSource> {
        SearchRemoteDataSourceImpl(apolloClient = get())
    }

    single<SearchHistoryDataStore> {
        SearchHistoryDataStore(store = androidContext().searchHistory)
    }

    single<SearchRepository> {
        SearchRepositoryImpl(
            searchRemoteDataSource = get(),
            searchLocalDataSource = get(),
        )
    }

    factory {
        GetPreviousSearchQueriesUseCase(searchRepository = get())
    }

    factory {
        GetQueriedBooksUseCase(searchRepository = get())
    }

    factory {
        RemoveAllSearchQueriesUseCase(searchRepository = get())
    }

    factory {
        RemoveSearchQueryUseCase(searchRepository = get())
    }

    factory {
        SearchForNameUseCase(
            searchRepository = get(),
            getUserIdUseCase = get(),
        )
    }
}