package nl.rhaydus.softcover.core.book.di

import nl.rhaydus.softcover.core.book.data.datasource.BooksLocalDataSource
import nl.rhaydus.softcover.core.book.data.datasource.BooksLocalDataSourceImpl
import nl.rhaydus.softcover.core.book.data.datasource.BooksRemoteDataSource
import nl.rhaydus.softcover.core.book.data.datasource.BooksRemoteDataSourceImpl
import nl.rhaydus.softcover.core.book.data.repository.BooksRepositoryImpl
import nl.rhaydus.softcover.core.book.data.storage.EditionImageStorage
import nl.rhaydus.softcover.core.book.data.storage.EditionImageStorageImpl
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.book.domain.usecase.AddBookByIsbnUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.FetchBookByIdUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.GetAllUserBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.GetCurrentlyReadingUserBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.GetDidNotFinishUserBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.GetEditionsByBookIdUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.GetReadUserBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.GetSortedAllUserBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.GetSortedBooksByStatusUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.GetTrendingBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.GetWantToReadUserBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.MarkBookAsReadUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.MarkBookAsReadingUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.MarkBookAsWantToReadUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.PersistEditionImageUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.RecordBookProgressUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.RemoveBookFromLibraryUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.ReorderShelfBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.ResolveBookByIsbnUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.UpdateBookEditionUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.UpdateBookProgressUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.UpdateBookRatingUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.UpdateBookReviewUseCase
import nl.rhaydus.softcover.core.database.SoftcoverDatabase
import nl.rhaydus.softcover.core.database.dao.BookDao
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val bookModule = module {
    single<EditionImageStorage> {
        EditionImageStorageImpl(context = androidContext())
    }

    single<BooksRemoteDataSource> {
        BooksRemoteDataSourceImpl(apolloClient = get())
    }

    single<BooksRepository> {
        BooksRepositoryImpl(
            booksRemoteDataSource = get(),
            booksLocalDataSource = get(),
            networkAvailability = get(),
            userBookWriteQueue = get(),
            userBookWriteDrainer = get(),
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
        GetTrendingBooksUseCase(booksRepository = get())
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
        FetchBookByIdUseCase(
            booksRepository = get(),
        )
    }

    factory {
        ResolveBookByIsbnUseCase(
            booksRepository = get(),
            fetchBookByIdUseCase = get(),
        )
    }

    factory {
        AddBookByIsbnUseCase(
            booksRepository = get(),
            fetchBookByIdUseCase = get(),
        )
    }

    factory {
        GetEditionsByBookIdUseCase(booksRepository = get())
    }

    factory {
        MarkBookAsReadingUseCase(booksRepository = get())
    }

    factory {
        UpdateBookRatingUseCase(booksRepository = get())
    }

    factory {
        UpdateBookReviewUseCase(booksRepository = get())
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
        RecordBookProgressUseCase(
            markBookAsReadUseCase = get(),
            updateBookProgressUseCase = get(),
        )
    }

    factory {
        PersistEditionImageUseCase(booksRepository = get())
    }

    factory {
        ReorderShelfBooksUseCase(booksRepository = get())
    }
}