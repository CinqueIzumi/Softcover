package nl.rhaydus.softcover.feature.search.data.datasource

import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.feature.search.data.datastore.SearchHistoryDataStore
import nl.rhaydus.softcover.feature.search.data.datastore.serializer.SearchHistoryEntity

class SearchLocalDataSourceImpl(
    private val dataStore: SearchHistoryDataStore,
) : SearchLocalDataSource {
    override val previousSearchQueries = dataStore.store.data.map { it.previousQueries }

    private val maxListSize = 10

    override suspend fun saveSearchQuery(name: String) {
        dataStore.store.updateData { currentData: SearchHistoryEntity ->
            val updatedList = currentData.previousQueries.toMutableList().apply {
                remove(name)

                add(0, name)

                if (size > maxListSize) {
                    subList(maxListSize, size).clear()
                }
            }

            currentData.copy(previousQueries = updatedList)
        }
    }

    override suspend fun removeSearchQuery(name: String) {
        dataStore.store.updateData { currentData: SearchHistoryEntity ->
            val updatedList = currentData.previousQueries.toMutableList().apply {
                remove(name)
            }

            currentData.copy(previousQueries = updatedList)
        }
    }

    override suspend fun removeAllSearchQueries() {
        dataStore.store.updateData {
            it.copy(previousQueries = emptyList())
        }
    }
}