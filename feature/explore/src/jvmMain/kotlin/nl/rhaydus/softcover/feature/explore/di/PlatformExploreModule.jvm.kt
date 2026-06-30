package nl.rhaydus.softcover.feature.explore.di

import okio.FileSystem
import okio.Path.Companion.toPath
import org.koin.core.module.Module
import org.koin.dsl.module
import nl.rhaydus.softcover.core.domain.platform.desktopAppDataDirectory
import nl.rhaydus.softcover.feature.explore.data.datastore.SearchHistoryDataStore
import nl.rhaydus.softcover.feature.explore.data.datastore.createSearchHistoryDataStore

actual val platformExploreModule: Module = module {
    single<SearchHistoryDataStore> {
        SearchHistoryDataStore(
            store = createSearchHistoryDataStore(FileSystem.SYSTEM) {
                "${desktopAppDataDirectory()}/search_history.json".toPath()
            },
        )
    }
}
