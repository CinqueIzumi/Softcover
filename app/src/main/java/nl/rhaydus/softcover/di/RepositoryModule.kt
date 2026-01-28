package nl.rhaydus.softcover.di

import nl.rhaydus.softcover.feature.book.data.repository.BookDetailRepositoryImpl
import nl.rhaydus.softcover.feature.book.domain.repository.BookDetailRepository
import nl.rhaydus.softcover.feature.library.data.repository.LibraryRepositoryImpl
import nl.rhaydus.softcover.feature.library.domain.repository.LibraryRepository
import nl.rhaydus.softcover.feature.reading.data.repository.BooksRepositoryImpl
import nl.rhaydus.softcover.feature.reading.domain.repository.BooksRepository
import nl.rhaydus.softcover.feature.settings.data.datasource.SettingsLocalDataSource
import nl.rhaydus.softcover.feature.settings.data.datasource.SettingsRemoteDataSource
import nl.rhaydus.softcover.feature.settings.data.repository.SettingsRepositoryImpl
import nl.rhaydus.softcover.feature.settings.domain.repository.SettingsRepository
import org.koin.dsl.module

val repositoryModule = module {
    single<SettingsRepository> {
        SettingsRepositoryImpl(
            settingsRemoteDataSource = get<SettingsRemoteDataSource>(),
            settingsLocalDataSource = get<SettingsLocalDataSource>()
        )
    }

    single<BooksRepository> {
        BooksRepositoryImpl(
            bookRemoteDataSource = get(),
        )
    }

    single<BookDetailRepository> {
        BookDetailRepositoryImpl(
            bookDetailRemoteDataSource = get()
        )
    }

    single<LibraryRepository> {
        LibraryRepositoryImpl(
            libraryRemoteDataSource = get()
        )
    }
}