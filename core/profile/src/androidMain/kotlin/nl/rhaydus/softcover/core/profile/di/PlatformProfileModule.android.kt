package nl.rhaydus.softcover.core.profile.di

import okio.FileSystem
import okio.Path.Companion.toPath
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module
import nl.rhaydus.softcover.core.profile.data.datastore.ProfileCacheDataStore
import nl.rhaydus.softcover.core.profile.data.datastore.createProfileCacheDataStore

actual val platformProfileModule: Module = module {
    single<ProfileCacheDataStore> {
        val filesDir = androidContext().filesDir.path
        ProfileCacheDataStore(
            store = createProfileCacheDataStore(FileSystem.SYSTEM) {
                "$filesDir/datastore/profile_cache.json".toPath()
            },
        )
    }
}
