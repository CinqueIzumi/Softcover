package nl.rhaydus.softcover.feature.books.di

import nl.rhaydus.softcover.core.data.database.SoftcoverDatabase
import nl.rhaydus.softcover.feature.books.data.dao.BookDao
import nl.rhaydus.softcover.feature.books.data.datasource.BooksLocalDataSource
import nl.rhaydus.softcover.feature.books.data.datasource.BooksLocalDataSourceImpl
import nl.rhaydus.softcover.feature.books.data.datasource.BooksRemoteDataSource
import nl.rhaydus.softcover.feature.books.data.datasource.BooksRemoteDataSourceImpl
import nl.rhaydus.softcover.feature.books.data.repository.BooksRepositoryImpl
import nl.rhaydus.softcover.feature.books.domain.repository.BooksRepository
import nl.rhaydus.softcover.feature.books.domain.usecase.FetchBookByIdUseCase
import nl.rhaydus.softcover.feature.books.domain.usecase.GetEditionsByBookIdUseCase
import nl.rhaydus.softcover.feature.books.domain.usecase.GetAllUserBooksUseCase
import nl.rhaydus.softcover.feature.books.domain.usecase.GetSortedAllUserBooksUseCase
import nl.rhaydus.softcover.feature.books.domain.usecase.GetSortedBooksByStatusUseCase
import nl.rhaydus.softcover.feature.books.domain.usecase.GetAllUserListsUseCase
import nl.rhaydus.softcover.feature.books.domain.usecase.GetCurrentlyReadingUserBooksUseCase
import nl.rhaydus.softcover.feature.books.domain.usecase.GetDidNotFinishUserBooksUseCase
import nl.rhaydus.softcover.feature.books.domain.usecase.GetReadUserBooksUseCase
import nl.rhaydus.softcover.feature.books.domain.usecase.GetWantToReadUserBooksUseCase
import nl.rhaydus.softcover.feature.books.domain.usecase.MarkBookAsReadUseCase
import nl.rhaydus.softcover.feature.books.domain.usecase.PersistEditionImageUseCase
import nl.rhaydus.softcover.feature.books.domain.usecase.MarkBookAsReadingUseCase
import nl.rhaydus.softcover.feature.books.domain.usecase.MarkBookAsWantToReadUseCase
import nl.rhaydus.softcover.feature.books.domain.usecase.SetEditionAsOwnedUseCase
import nl.rhaydus.softcover.feature.books.domain.usecase.RefreshUserBooksUseCase
import nl.rhaydus.softcover.feature.books.domain.usecase.RemoveBookFromLibraryUseCase
import nl.rhaydus.softcover.feature.books.domain.usecase.UpdateBookEditionUseCase
import nl.rhaydus.softcover.feature.books.domain.usecase.UpdateBookProgressUseCase
import org.koin.dsl.module

val booksModule = module {
    single<BooksRemoteDataSource> {
        BooksRemoteDataSourceImpl(apolloClient = get())
    }

    single<BooksRepository> {
        BooksRepositoryImpl(
            booksRemoteDataSource = get(),
            booksLocalDataSource = get(),
            settingsRepository = get(),
            networkAvailability = get(),
            offlineProgressQueue = get(),
            pendingProgressDrainer = get(),
            applicationScope = get(),
        )
    }

    single<BooksLocalDataSource> {
        BooksLocalDataSourceImpl(
            dao = get(),
            editionImageStorage = get(),
        )
    }

    single<BookDao> {
        get<SoftcoverDatabase>().bookDao()
    }

    factory {
        GetAllUserBooksUseCase(booksRepository = get())
    }

    factory {
        GetSortedAllUserBooksUseCase(booksRepository = get())
    }

    factory {
        GetSortedBooksByStatusUseCase(booksRepository = get())
    }

    factory {
        GetCurrentlyReadingUserBooksUseCase(booksRepository = get())
    }

    factory {
        GetDidNotFinishUserBooksUseCase(booksRepository = get())
    }

    factory {
        GetReadUserBooksUseCase(booksRepository = get())
    }

    factory {
        GetWantToReadUserBooksUseCase(booksRepository = get())
    }

    factory {
        RefreshUserBooksUseCase(
            getUserIdUseCase = get(),
            booksRepository = get(),
        )
    }

    factory {
        FetchBookByIdUseCase(
            booksRepository = get(),
        )
    }

    factory {
        GetEditionsByBookIdUseCase(booksRepository = get())
    }

    factory {
        MarkBookAsReadingUseCase(booksRepository = get())
    }

    factory {
        MarkBookAsWantToReadUseCase(booksRepository = get())
    }

    factory {
        RemoveBookFromLibraryUseCase(booksRepository = get())
    }

    factory {
        MarkBookAsReadUseCase(repository = get())
    }

    factory {
        UpdateBookEditionUseCase(repository = get())
    }

    factory {
        UpdateBookProgressUseCase(repository = get())
    }

    factory {
        GetAllUserListsUseCase(booksRepository = get())
    }

    factory {
        SetEditionAsOwnedUseCase(booksRepository = get())
    }

    factory {
        PersistEditionImageUseCase(booksRepository = get())
    }
}