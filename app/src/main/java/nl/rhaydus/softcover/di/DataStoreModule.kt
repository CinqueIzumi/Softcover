package nl.rhaydus.softcover.di

import nl.rhaydus.softcover.feature.search.data.datastore.SearchHistoryDataStore
import nl.rhaydus.softcover.feature.search.data.datastore.searchHistory
import nl.rhaydus.softcover.feature.settings.data.datastore.AppSettingsDataStore
import nl.rhaydus.softcover.feature.settings.data.datastore.appSettings
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataStoreModule = module {
    single<AppSettingsDataStore> {
        AppSettingsDataStore(store = androidContext().appSettings)
    }

    single<SearchHistoryDataStore> {
        SearchHistoryDataStore(store = androidContext().searchHistory)
    }
}