package nl.rhaydus.softcover.feature.explore.di

import org.koin.dsl.bind
import org.koin.dsl.module
import nl.rhaydus.softcover.core.book.di.bookModule
import nl.rhaydus.softcover.core.database.SoftcoverDatabase
import nl.rhaydus.softcover.core.database.di.databaseModule
import nl.rhaydus.softcover.core.designsystem.presentation.di.designSystemModule
import nl.rhaydus.softcover.core.domain.di.dispatcherModule
import nl.rhaydus.softcover.core.identity.di.identityModule
import nl.rhaydus.softcover.core.network.di.apolloModule
import nl.rhaydus.softcover.feature.explore.data.datasource.DismissedContinueSeriesLocalDataSource
import nl.rhaydus.softcover.feature.explore.data.datasource.DismissedContinueSeriesLocalDataSourceImpl
import nl.rhaydus.softcover.feature.explore.data.datasource.SearchLocalDataSource
import nl.rhaydus.softcover.feature.explore.data.datasource.SearchLocalDataSourceImpl
import nl.rhaydus.softcover.feature.explore.data.datasource.SearchRemoteDataSource
import nl.rhaydus.softcover.feature.explore.data.datasource.SearchRemoteDataSourceImpl
import nl.rhaydus.softcover.feature.explore.data.repository.ExploreRepositoryImpl
import nl.rhaydus.softcover.feature.explore.domain.repository.ExploreRepository
import nl.rhaydus.softcover.feature.explore.domain.usecase.DismissContinueSeriesBookUseCase
import nl.rhaydus.softcover.feature.explore.domain.usecase.DismissContinueSeriesUseCase
import nl.rhaydus.softcover.feature.explore.domain.usecase.EnrichDismissedContinueSeriesMetadataUseCase
import nl.rhaydus.softcover.feature.explore.domain.usecase.GetContinueSeriesBooksUseCase
import nl.rhaydus.softcover.feature.explore.domain.usecase.GetDismissedContinueSeriesBooksUseCase
import nl.rhaydus.softcover.feature.explore.domain.usecase.GetDismissedContinueSeriesUseCase
import nl.rhaydus.softcover.feature.explore.domain.usecase.GetPreviousSearchQueriesUseCase
import nl.rhaydus.softcover.feature.explore.domain.usecase.GetQueriedBooksUseCase
import nl.rhaydus.softcover.feature.explore.domain.usecase.RemoveAllSearchQueriesUseCase
import nl.rhaydus.softcover.feature.explore.domain.usecase.RemoveSearchQueryUseCase
import nl.rhaydus.softcover.feature.explore.domain.usecase.SearchForNameUseCase
import nl.rhaydus.softcover.feature.explore.domain.usecase.UndoContinueSeriesBookDismissalUseCase
import nl.rhaydus.softcover.feature.explore.domain.usecase.UndoContinueSeriesDismissalUseCase
import nl.rhaydus.softcover.feature.explore.presentation.collector.ContinueSeriesBooksCollector
import nl.rhaydus.softcover.feature.explore.presentation.collector.DismissedBooksCollector
import nl.rhaydus.softcover.feature.explore.presentation.collector.DismissedSeriesCollector
import nl.rhaydus.softcover.feature.explore.presentation.collector.EnrichDismissedMetadataCollector
import nl.rhaydus.softcover.feature.explore.presentation.collector.EnrichMetadataCollector
import nl.rhaydus.softcover.feature.explore.presentation.collector.ExploreCollector
import nl.rhaydus.softcover.feature.explore.presentation.collector.HiddenSuggestionsCollector
import nl.rhaydus.softcover.feature.explore.presentation.collector.PreviousQueriesCollector
import nl.rhaydus.softcover.feature.explore.presentation.collector.QueriedBooksCollector
import nl.rhaydus.softcover.feature.explore.presentation.collector.TrendingBooksCollector
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreScreenScreenModel
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.HiddenSuggestionsScreenModel

val exploreModule = module {
    includes(
        platformExploreModule,
        dispatcherModule,
        bookModule,
        identityModule,
        databaseModule,
        apolloModule,
        designSystemModule,
    )

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
            enrichDismissedContinueSeriesMetadataUseCase = get(),
            flows = getAll(),
            appDispatchers = get(),
        )
    }

    factory { PreviousQueriesCollector() } bind ExploreCollector::class

    factory { QueriedBooksCollector() } bind ExploreCollector::class

    factory { TrendingBooksCollector() } bind ExploreCollector::class

    factory { ContinueSeriesBooksCollector() } bind ExploreCollector::class

    factory { EnrichDismissedMetadataCollector() } bind ExploreCollector::class

    factory {
        HiddenSuggestionsScreenModel(
            getDismissedContinueSeriesBooksUseCase = get(),
            getDismissedContinueSeriesUseCase = get(),
            undoContinueSeriesBookDismissalUseCase = get(),
            undoContinueSeriesDismissalUseCase = get(),
            dismissContinueSeriesBookUseCase = get(),
            dismissContinueSeriesUseCase = get(),
            enrichDismissedContinueSeriesMetadataUseCase = get(),
            appDispatchers = get(),
            flows = getAll(),
        )
    }

    factory { DismissedBooksCollector() } bind HiddenSuggestionsCollector::class

    factory { DismissedSeriesCollector() } bind HiddenSuggestionsCollector::class

    factory { EnrichMetadataCollector() } bind HiddenSuggestionsCollector::class

    single<SearchLocalDataSource> {
        SearchLocalDataSourceImpl(dataStore = get())
    }

    single<SearchRemoteDataSource> {
        SearchRemoteDataSourceImpl(apolloClient = get())
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

    factory {
        GetDismissedContinueSeriesBooksUseCase(exploreRepository = get())
    }

    factory {
        GetDismissedContinueSeriesUseCase(exploreRepository = get())
    }

    factory {
        EnrichDismissedContinueSeriesMetadataUseCase(
            exploreRepository = get(),
            booksRepository = get(),
        )
    }
}
