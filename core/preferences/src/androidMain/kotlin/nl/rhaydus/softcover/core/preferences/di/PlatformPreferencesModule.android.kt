package nl.rhaydus.softcover.core.preferences.di

import okio.Path.Companion.toPath
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module
import nl.rhaydus.softcover.core.preferences.data.datastore.AppSettingsDataStore
import nl.rhaydus.softcover.core.preferences.data.datastore.createAppSettingsDataStore
import nl.rhaydus.softcover.core.preferences.data.security.AndroidSecureApiKeyStorage
import nl.rhaydus.softcover.core.preferences.data.security.SecureApiKeyStorage

actual val platformPreferencesModule: Module = module {
    single<AppSettingsDataStore> {
        val filesDir = androidContext().filesDir.path
        AppSettingsDataStore(
            store = createAppSettingsDataStore { "$filesDir/datastore/app_settings.json".toPath() },
        )
    }

    single<SecureApiKeyStorage> {
        AndroidSecureApiKeyStorage(
            context = androidContext(),
            dispatchers = get(),
        )
    }
}
