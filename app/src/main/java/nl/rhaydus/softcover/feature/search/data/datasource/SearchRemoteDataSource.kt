package nl.rhaydus.softcover.feature.search.data.datasource

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.domain.model.Book

interface SearchRemoteDataSource {
    val queriedBooks: Flow<List<Book>>

    suspend fun searchForName(
        name: String,
        userId: Int,
    )
}