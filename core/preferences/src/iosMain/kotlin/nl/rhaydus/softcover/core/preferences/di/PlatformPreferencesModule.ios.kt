package nl.rhaydus.softcover.core.preferences.di

import okio.Path.Companion.toPath
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import nl.rhaydus.softcover.core.preferences.data.datastore.AppSettingsDataStore
import nl.rhaydus.softcover.core.preferences.data.datastore.createAppSettingsDataStore
import nl.rhaydus.softcover.core.preferences.data.security.IosSecureApiKeyStorage
import nl.rhaydus.softcover.core.preferences.data.security.SecureApiKeyStorage

actual val platformPreferencesModule: Module = module {
    single<AppSettingsDataStore> {
        AppSettingsDataStore(
            store = createAppSettingsDataStore { "${documentsDirectory()}/app_settings.json".toPath() },
        )
    }

    single<SecureApiKeyStorage> {
        IosSecureApiKeyStorage(dispatchers = get())
    }
}

private fun documentsDirectory(): String =
    NSSearchPathForDirectoriesInDomains(
        directory = NSDocumentDirectory,
        domainMask = NSUserDomainMask,
        expandTilde = true,
    ).first() as String
