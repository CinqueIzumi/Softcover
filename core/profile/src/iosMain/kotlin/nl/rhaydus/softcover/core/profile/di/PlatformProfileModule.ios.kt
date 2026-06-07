package nl.rhaydus.softcover.core.profile.di

import okio.FileSystem
import okio.Path.Companion.toPath
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import nl.rhaydus.softcover.core.profile.data.datastore.ProfileCacheDataStore
import nl.rhaydus.softcover.core.profile.data.datastore.createProfileCacheDataStore

actual val platformProfileModule: Module = module {
    single<ProfileCacheDataStore> {
        ProfileCacheDataStore(
            store = createProfileCacheDataStore(FileSystem.SYSTEM) {
                "${documentsDirectory()}/profile_cache.json".toPath()
            },
        )
    }
}

private fun documentsDirectory(): String =
    NSSearchPathForDirectoriesInDomains(
        directory = NSDocumentDirectory,
        domainMask = NSUserDomainMask,
        expandTilde = true,
    ).first() as String
