package nl.rhaydus.softcover.di

import nl.rhaydus.softcover.feature.settings.data.datastore.AppSettingsDataStore
import nl.rhaydus.softcover.feature.settings.data.datastore.appSettings
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataStoreModule = module {
    single<AppSettingsDataStore> {
        androidContext().appSettings
    }
}