package nl.rhaydus.softcover.feature.explore.di

import nl.rhaydus.softcover.core.data.database.SoftcoverDatabase
import nl.rhaydus.softcover.feature.explore.data.datasource.DismissedContinueSeriesLocalDataSource
import nl.rhaydus.softcover.feature.explore.data.datasource.DismissedContinueSeriesLocalDataSourceImpl
import nl.rhaydus.softcover.feature.explore.data.datasource.SearchLocalDataSource
import nl.rhaydus.softcover.feature.explore.data.datasource.SearchLocalDataSourceImpl
import nl.rhaydus.softcover.feature.explore.data.datasource.SearchRemoteDataSource
import nl.rhaydus.softcover.feature.explore.data.datasource.SearchRemoteDataSourceImpl
import nl.rhaydus.softcover.feature.explore.data.datastore.SearchHistoryDataStore
import nl.rhaydus.softcover.feature.explore.data.datastore.searchHistory
import nl.rhaydus.softcover.feature.explore.data.repository.ExploreRepositoryImpl
import nl.rhaydus.softcover.feature.explore.domain.repository.ExploreRepository
import nl.rhaydus.softcover.feature.explore.domain.usecase.DismissContinueSeriesBookUseCase
import nl.rhaydus.softcover.feature.explore.domain.usecase.DismissContinueSeriesUseCase
import nl.rhaydus.softcover.feature.explore.domain.usecase.GetContinueSeriesBooksUseCase
import nl.rhaydus.softcover.feature.explore.domain.usecase.GetPreviousSearchQueriesUseCase
import nl.rhaydus.softcover.feature.explore.domain.usecase.GetQueriedBooksUseCase
import nl.rhaydus.softcover.feature.explore.domain.usecase.RemoveAllSearchQueriesUseCase
import nl.rhaydus.softcover.feature.explore.domain.usecase.RemoveSearchQueryUseCase
import nl.rhaydus.softcover.feature.explore.domain.usecase.SearchForNameUseCase
import nl.rhaydus.softcover.feature.explore.domain.usecase.UndoContinueSeriesBookDismissalUseCase
import nl.rhaydus.softcover.feature.explore.domain.usecase.UndoContinueSeriesDismissalUseCase
import nl.rhaydus.softcover.feature.explore.presentation.flows.ContinueSeriesBooksCollector
import nl.rhaydus.softcover.feature.explore.presentation.flows.ExploreInitializer
import nl.rhaydus.softcover.feature.explore.presentation.flows.PreviousQueriesCollector
import nl.rhaydus.softcover.feature.explore.presentation.flows.QueriedBooksCollector
import nl.rhaydus.softcover.feature.explore.presentation.flows.TrendingBooksCollector
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreScreenScreenModel
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module

val exploreModule = module {
    factory {
        ExploreScreenScreenModel(
            getPreviousSearchQueriesUseCase = get(),
            getQueriedBooksUseCase = get(),
            searchForNameUseCase = get(),
            getAllUserBooksUseCase = get(),
            removeSearchQueryUseCase = get(),
            removeAllSearchQueriesUseCase = get(),
            markBookAsWantToReadUseCase = get(),
            removeBookFromLibraryUseCase = get(),
            getTrendingBooksUseCase = get(),
            getContinueSeriesBooksUseCase = get(),
            dismissContinueSeriesBookUseCase = get(),
            dismissContinueSeriesUseCase = get(),
            undoContinueSeriesBookDismissalUseCase = get(),
            undoContinueSeriesDismissalUseCase = get(),
            flows = getAll(),
            appDispatchers = get(),
        )
    }

    factory { PreviousQueriesCollector() } bind ExploreInitializer::class

    factory { QueriedBooksCollector() } bind ExploreInitializer::class

    factory { TrendingBooksCollector() } bind ExploreInitializer::class

    factory { ContinueSeriesBooksCollector() } bind ExploreInitializer::class

    single<SearchLocalDataSource> {
        SearchLocalDataSourceImpl(dataStore = get())
    }

    single<SearchRemoteDataSource> {
        SearchRemoteDataSourceImpl(apolloClient = get())
    }

    single<SearchHistoryDataStore> {
        SearchHistoryDataStore(store = androidContext().searchHistory)
    }

    single<DismissedContinueSeriesLocalDataSource> {
        DismissedContinueSeriesLocalDataSourceImpl(
            dao = get<SoftcoverDatabase>().dismissedContinueSeriesDao(),
        )
    }

    single<ExploreRepository> {
        ExploreRepositoryImpl(
            searchRemoteDataSource = get(),
            searchLocalDataSource = get(),
            dismissedContinueSeriesLocalDataSource = get(),
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

    factory {
        GetContinueSeriesBooksUseCase(
            booksRepository = get(),
            exploreRepository = get(),
        )
    }

    factory {
        DismissContinueSeriesBookUseCase(exploreRepository = get())
    }

    factory {
        DismissContinueSeriesUseCase(exploreRepository = get())
    }

    factory {
        UndoContinueSeriesBookDismissalUseCase(exploreRepository = get())
    }

    factory {
        UndoContinueSeriesDismissalUseCase(exploreRepository = get())
    }
}
