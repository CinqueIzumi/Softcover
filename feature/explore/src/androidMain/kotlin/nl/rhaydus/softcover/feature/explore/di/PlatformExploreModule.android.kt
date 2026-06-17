package nl.rhaydus.softcover.feature.explore.di

import okio.FileSystem
import okio.Path.Companion.toPath
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module
import nl.rhaydus.softcover.feature.explore.data.datastore.SearchHistoryDataStore
import nl.rhaydus.softcover.feature.explore.data.datastore.createSearchHistoryDataStore

actual val platformExploreModule: Module = module {
    single<SearchHistoryDataStore> {
        val filesDir = androidContext().filesDir.path
        SearchHistoryDataStore(
            store = createSearchHistoryDataStore(FileSystem.SYSTEM) {
                "$filesDir/datastore/search_history.json".toPath()
            },
        )
    }
}
