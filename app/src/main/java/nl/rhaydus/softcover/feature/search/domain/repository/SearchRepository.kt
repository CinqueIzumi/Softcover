package nl.rhaydus.softcover.feature.search.domain.repository

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.domain.model.Book

interface SearchRepository {
    val previousSearchQueries: Flow<List<String>>
    val queriedBooks: Flow<List<Book>>

    suspend fun searchForName(
        name: String,
        userId: Int,
    )

    suspend fun saveSearchQuery(name: String)

    suspend fun removeSearchQuery(name: String)

    suspend fun removeAllSearchQueries()
}