package nl.rhaydus.softcover.core.preferences.di

import okio.FileSystem
import okio.Path.Companion.toPath
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import nl.rhaydus.platform.IosSecureStorage
import nl.rhaydus.platform.SecureStorage
import nl.rhaydus.softcover.core.preferences.data.datastore.AppSettingsDataStore
import nl.rhaydus.softcover.core.preferences.data.datastore.createAppSettingsDataStore
import nl.rhaydus.softcover.core.preferences.data.security.IosLegacySecureApiKeyStorage
import nl.rhaydus.softcover.core.preferences.data.security.LegacySecureApiKeyStorage

actual val platformPreferencesModule: Module = module {
    single<AppSettingsDataStore> {
        AppSettingsDataStore(
            store = createAppSettingsDataStore(FileSystem.SYSTEM) {
                "${documentsDirectory()}/app_settings.json".toPath()
            },
        )
    }

    single<SecureStorage> {
        IosSecureStorage(dispatchers = get())
    }

    single<LegacySecureApiKeyStorage> {
        IosLegacySecureApiKeyStorage(dispatchers = get())
    }
}

private fun documentsDirectory(): String =
    NSSearchPathForDirectoriesInDomains(
        directory = NSDocumentDirectory,
        domainMask = NSUserDomainMask,
        expandTilde = true,
    ).first() as String
