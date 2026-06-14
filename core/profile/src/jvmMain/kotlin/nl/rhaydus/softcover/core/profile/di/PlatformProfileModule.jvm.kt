package nl.rhaydus.softcover.core.profile.di

import okio.FileSystem
import okio.Path.Companion.toPath
import org.koin.core.module.Module
import org.koin.dsl.module
import nl.rhaydus.softcover.core.domain.platform.desktopAppDataDirectory
import nl.rhaydus.softcover.core.profile.data.datastore.ProfileCacheDataStore
import nl.rhaydus.softcover.core.profile.data.datastore.createProfileCacheDataStore

actual val platformProfileModule: Module = module {
    single<ProfileCacheDataStore> {
        ProfileCacheDataStore(
            store = createProfileCacheDataStore(FileSystem.SYSTEM) {
                "${desktopAppDataDirectory()}/profile_cache.json".toPath()
            },
        )
    }
}
