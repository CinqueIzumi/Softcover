package nl.rhaydus.softcover.di

import nl.rhaydus.softcover.feature.book.data.datasource.BookDetailRemoteDataSource
import nl.rhaydus.softcover.feature.book.data.datasource.BookDetailRemoteDataSourceImpl
import nl.rhaydus.softcover.feature.library.data.datasource.LibraryRemoteDataSource
import nl.rhaydus.softcover.feature.library.data.datasource.LibraryRemoteDataSourceImpl
import nl.rhaydus.softcover.feature.reading.data.datasource.BookRemoteDataSource
import nl.rhaydus.softcover.feature.reading.data.datasource.BookRemoteDataSourceImpl
import nl.rhaydus.softcover.feature.settings.data.datasource.SettingsLocalDataSource
import nl.rhaydus.softcover.feature.settings.data.datasource.SettingsLocalDataSourceImpl
import nl.rhaydus.softcover.feature.settings.data.datasource.SettingsRemoteDataSource
import nl.rhaydus.softcover.feature.settings.data.datasource.SettingsRemoteDataSourceImpl
import org.koin.dsl.module

val dataSourceModule = module {
    single<SettingsLocalDataSource> {
        SettingsLocalDataSourceImpl(get())
    }

    single<SettingsRemoteDataSource> {
        SettingsRemoteDataSourceImpl(get())
    }

    single<BookRemoteDataSource> {
        BookRemoteDataSourceImpl(get())
    }

    single<BookDetailRemoteDataSource> {
        BookDetailRemoteDataSourceImpl(get())
    }

    single<LibraryRemoteDataSource> {
        LibraryRemoteDataSourceImpl(get())
    }
}