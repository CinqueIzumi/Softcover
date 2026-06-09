package nl.rhaydus.softcover.feature.explore.di

import okio.FileSystem
import okio.Path.Companion.toPath
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import nl.rhaydus.softcover.feature.explore.data.datastore.SearchHistoryDataStore
import nl.rhaydus.softcover.feature.explore.data.datastore.createSearchHistoryDataStore

actual val platformExploreModule: Module = module {
    single<SearchHistoryDataStore> {
        SearchHistoryDataStore(
            store = createSearchHistoryDataStore(FileSystem.SYSTEM) {
                "${documentsDirectory()}/search_history.json".toPath()
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
