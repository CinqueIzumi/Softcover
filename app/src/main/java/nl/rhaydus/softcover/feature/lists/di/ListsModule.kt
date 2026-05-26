package nl.rhaydus.softcover.feature.lists.di

import nl.rhaydus.softcover.feature.lists.data.datasource.ListsLocalDataSource
import nl.rhaydus.softcover.feature.lists.data.datasource.ListsLocalDataSourceImpl
import nl.rhaydus.softcover.feature.lists.data.datasource.ListsRemoteDataSource
import nl.rhaydus.softcover.feature.lists.data.datasource.ListsRemoteDataSourceImpl
import nl.rhaydus.softcover.feature.lists.data.repository.ListsRepositoryImpl
import nl.rhaydus.softcover.feature.lists.domain.repository.ListsRepository
import nl.rhaydus.softcover.feature.lists.domain.usecase.AddBookToListUseCase
import nl.rhaydus.softcover.feature.lists.domain.usecase.CreateListUseCase
import nl.rhaydus.softcover.feature.lists.domain.usecase.GetAllUserListsUseCase
import nl.rhaydus.softcover.feature.lists.domain.usecase.RemoveBookFromListUseCase
import nl.rhaydus.softcover.feature.lists.domain.usecase.ReorderListBooksUseCase
import nl.rhaydus.softcover.feature.lists.domain.usecase.SetEditionAsOwnedUseCase
import nl.rhaydus.softcover.feature.lists.domain.usecase.SetListRankedUseCase
import nl.rhaydus.softcover.feature.lists.presentation.screenmodel.CreateListScreenModel
import org.koin.dsl.module

val listsModule = module {
    single<ListsRemoteDataSource> {
        ListsRemoteDataSourceImpl(apolloClient = get())
    }

    single<ListsLocalDataSource> {
        ListsLocalDataSourceImpl(dao = get())
    }

    single<ListsRepository> {
        ListsRepositoryImpl(
            listsRemoteDataSource = get(),
            listsLocalDataSource = get(),
            applicationScope = get(),
        )
    }

    factory {
        GetAllUserListsUseCase(listsRepository = get())
    }

    factory {
        CreateListUseCase(listsRepository = get())
    }

    factory {
        SetEditionAsOwnedUseCase(listsRepository = get())
    }

    factory {
        AddBookToListUseCase(listsRepository = get())
    }

    factory {
        RemoveBookFromListUseCase(listsRepository = get())
    }

    factory {
        ReorderListBooksUseCase(listsRepository = get())
    }

    factory {
        SetListRankedUseCase(listsRepository = get())
    }

    factory {
        CreateListScreenModel(
            createListUseCase = get(),
            dispatchers = get(),
        )
    }
}
