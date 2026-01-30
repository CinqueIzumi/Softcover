package nl.rhaydus.softcover.di

import nl.rhaydus.softcover.feature.book.data.datasource.BookDetailRemoteDataSource
import nl.rhaydus.softcover.feature.book.data.datasource.BookDetailRemoteDataSourceImpl
import nl.rhaydus.softcover.feature.reading.data.datasource.BookRemoteDataSource
import nl.rhaydus.softcover.feature.reading.data.datasource.BookRemoteDataSourceImpl
import nl.rhaydus.softcover.feature.search.data.datasource.SearchLocalDataSource
import nl.rhaydus.softcover.feature.search.data.datasource.SearchLocalDataSourceImpl
import nl.rhaydus.softcover.feature.search.data.datasource.SearchRemoteDataSource
import nl.rhaydus.softcover.feature.search.data.datasource.SearchRemoteDataSourceImpl
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

    single<SearchRemoteDataSource> {
        SearchRemoteDataSourceImpl(apolloClient = get())
    }

    single<SearchLocalDataSource> {
        SearchLocalDataSourceImpl(dataStore = get())
    }
}