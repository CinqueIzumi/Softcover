package nl.rhaydus.softcover.feature.search.data.repository

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.search.data.datasource.SearchLocalDataSource
import nl.rhaydus.softcover.feature.search.data.datasource.SearchRemoteDataSource
import nl.rhaydus.softcover.feature.search.domain.repository.SearchRepository

class SearchRepositoryImpl(
    private val searchRemoteDataSource: SearchRemoteDataSource,
    private val searchLocalDataSource: SearchLocalDataSource,
) : SearchRepository {
    override val previousSearchQueries: Flow<List<String>> =
        searchLocalDataSource.previousSearchQueries

    override val queriedBooks: Flow<List<Book>> = searchRemoteDataSource.queriedBooks

    override suspend fun searchForName(
        name: String,
        userId: Int,
    ) {
        searchRemoteDataSource.searchForName(
            name = name,
            userId = userId,
        )
    }

    override suspend fun saveSearchQuery(name: String) {
        searchLocalDataSource.saveSearchQuery(name = name)
    }

    override suspend fun removeSearchQuery(name: String) {
        searchLocalDataSource.removeSearchQuery(name = name)
    }

    override suspend fun removeAllSearchQueries() {
        searchLocalDataSource.removeAllSearchQueries()
    }
}