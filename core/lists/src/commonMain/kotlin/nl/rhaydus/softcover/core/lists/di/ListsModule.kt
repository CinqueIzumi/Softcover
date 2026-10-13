package nl.rhaydus.softcover.core.lists.di

import nl.rhaydus.softcover.core.book.di.bookModule
import nl.rhaydus.softcover.core.database.di.databaseModule
import nl.rhaydus.softcover.core.domain.di.dispatcherModule
import nl.rhaydus.softcover.core.lists.data.datasource.ListsLocalDataSource
import nl.rhaydus.softcover.core.lists.data.datasource.ListsLocalDataSourceImpl
import nl.rhaydus.softcover.core.lists.data.datasource.ListsRemoteDataSource
import nl.rhaydus.softcover.core.lists.data.datasource.ListsRemoteDataSourceImpl
import nl.rhaydus.softcover.core.lists.data.repository.ListsRepositoryImpl
import nl.rhaydus.softcover.core.lists.domain.repository.ListsRepository
import nl.rhaydus.softcover.core.lists.domain.usecase.AddBookToListUseCase
import nl.rhaydus.softcover.core.lists.domain.usecase.GetAllUserListsUseCase
import nl.rhaydus.softcover.core.lists.domain.usecase.RemoveBookFromListUseCase
import nl.rhaydus.softcover.core.lists.domain.usecase.ReorderListBooksUseCase
import nl.rhaydus.softcover.core.lists.domain.usecase.SetEditionAsOwnedUseCase
import nl.rhaydus.softcover.core.lists.domain.usecase.SetListRankedUseCase
import nl.rhaydus.softcover.core.network.di.apolloModule
import org.koin.dsl.module

val listsModule = module {
    includes(
        dispatcherModule,
        databaseModule,
        apolloModule,
        bookModule,
    )

    single<ListsRemoteDataSource> {
        ListsRemoteDataSourceImpl(
            apolloClient = get(),
            appDispatchers = get(),
        )
    }

    single<ListsLocalDataSource> {
        ListsLocalDataSourceImpl(dao = get())
    }

    single<ListsRepository> {
        ListsRepositoryImpl(
            listsRemoteDataSource = get(),
            listsLocalDataSource = get(),
            booksRepository = get(),
            applicationScope = get(),
            listWriteQueue = get(),
            listWriteDrainer = get(),
        )
    }

    factory {
        GetAllUserListsUseCase(listsRepository = get())
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
}
