package nl.rhaydus.softcover.core.preferences.di

import okio.FileSystem
import okio.Path.Companion.toPath
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module
import nl.rhaydus.platform.AndroidSecureStorage
import nl.rhaydus.platform.SecureStorage
import nl.rhaydus.softcover.core.preferences.data.datastore.AppSettingsDataStore
import nl.rhaydus.softcover.core.preferences.data.datastore.createAppSettingsDataStore
import nl.rhaydus.softcover.core.preferences.data.security.AndroidLegacySecureApiKeyStorage
import nl.rhaydus.softcover.core.preferences.data.security.LegacySecureApiKeyStorage

actual val platformPreferencesModule: Module = module {
    single<AppSettingsDataStore> {
        val filesDir = androidContext().filesDir.path
        AppSettingsDataStore(
            store = createAppSettingsDataStore(FileSystem.SYSTEM) {
                "$filesDir/datastore/app_settings.json".toPath()
            },
        )
    }

    single<SecureStorage> {
        AndroidSecureStorage(
            context = androidContext(),
            dispatchers = get(),
        )
    }

    single<LegacySecureApiKeyStorage> {
        AndroidLegacySecureApiKeyStorage(
            context = androidContext(),
            dispatchers = get(),
        )
    }
}
