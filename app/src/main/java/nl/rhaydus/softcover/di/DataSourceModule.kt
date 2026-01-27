package nl.rhaydus.softcover.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import nl.rhaydus.softcover.feature.book.data.datasource.BookDetailRemoteDataSource
import nl.rhaydus.softcover.feature.book.data.datasource.BookDetailRemoteDataSourceImpl
import nl.rhaydus.softcover.feature.reading.data.datasource.BookRemoteDataSource
import nl.rhaydus.softcover.feature.reading.data.datasource.BookRemoteDataSourceImpl
import nl.rhaydus.softcover.feature.settings.data.datasource.SettingsLocalDataSource
import nl.rhaydus.softcover.feature.settings.data.datasource.SettingsLocalDataSourceImpl
import nl.rhaydus.softcover.feature.settings.data.datasource.SettingsRemoteDataSource
import nl.rhaydus.softcover.feature.settings.data.datasource.SettingsRemoteDataSourceImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {
    @Binds
    abstract fun bindSettingsLocalDataSource(
        settingsLocalDataSourceImpl: SettingsLocalDataSourceImpl,
    ): SettingsLocalDataSource

    @Binds
    abstract fun bindSettingsRemoteDataSource(
        settingsRemoteDataSourceImpl: SettingsRemoteDataSourceImpl,
    ): SettingsRemoteDataSource

    @Binds
    abstract fun bindBookRemoteDataSource(
        bookRemoteDataSourceImpl: BookRemoteDataSourceImpl,
    ): BookRemoteDataSource

    @Binds
    abstract fun bindBookDetailRemoteDataSource(
        bookDetailRemoteDataSourceImpl: BookDetailRemoteDataSourceImpl,
    ): BookDetailRemoteDataSource
}