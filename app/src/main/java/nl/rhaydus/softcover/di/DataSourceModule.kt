package nl.rhaydus.softcover.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import nl.rhaydus.softcover.feature.settings.data.datasource.LocalSettingsDataSource
import nl.rhaydus.softcover.feature.settings.data.datasource.LocalSettingsDataSourceImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {
    @Binds
    abstract fun bindLocalSettingsDataSource(
        localSettingsDataSourceImpl: LocalSettingsDataSourceImpl,
    ): LocalSettingsDataSource
}