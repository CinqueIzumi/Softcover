package nl.rhaydus.softcover.feature.search.data.datasource

import kotlinx.coroutines.flow.Flow

interface SearchLocalDataSource {
    val previousSearchQueries: Flow<List<String>>

    suspend fun saveSearchQuery(name: String)

    suspend fun removeSearchQuery(name: String)

    suspend fun removeAllSearchQueries()
}
