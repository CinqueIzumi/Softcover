package nl.rhaydus.softcover.core.preferences.di

import eu.anifantakis.lib.ksafe.KSafe
import eu.anifantakis.lib.ksafe.KSafeConfig
import okio.FileSystem
import okio.Path.Companion.toPath
import org.koin.core.module.Module
import org.koin.dsl.module
import nl.rhaydus.platform.JvmSecureStorage
import nl.rhaydus.platform.SecureStorage
import nl.rhaydus.softcover.core.domain.platform.desktopAppDataDirectory
import nl.rhaydus.softcover.core.preferences.data.datastore.AppSettingsDataStore
import nl.rhaydus.softcover.core.preferences.data.datastore.createAppSettingsDataStore

actual val platformPreferencesModule: Module = module {
    single<AppSettingsDataStore> {
        AppSettingsDataStore(
            store = createAppSettingsDataStore(FileSystem.SYSTEM) {
                "${desktopAppDataDirectory()}/app_settings.json".toPath()
            },
        )
    }

    // KSafe custodies the encryption key in the OS secret store; namespaced so neither its OS-vault
    // key nor its on-disk data file can collide with other apps sharing the same OS user.
    single { KSafe(config = KSafeConfig(appNamespace = "nl.rhaydus.softcover")) }

    // No LegacySecureApiKeyStorage on desktop: the key string is unchanged and KSafe is namespaced by
    // this module, so the foundation store reads exactly what the old one wrote.
    single<SecureStorage> {
        JvmSecureStorage(
            ksafe = get(),
            dispatchers = get(),
        )
    }
}
