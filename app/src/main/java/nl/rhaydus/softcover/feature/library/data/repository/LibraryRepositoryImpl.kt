package nl.rhaydus.softcover.feature.library.data.repository

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.library.data.datasource.LibraryRemoteDataSource
import nl.rhaydus.softcover.feature.library.domain.repository.LibraryRepository

class LibraryRepositoryImpl(
    private val libraryRemoteDataSource: LibraryRemoteDataSource,
) : LibraryRepository {
    override suspend fun getUserBooks(userId: Int): Flow<List<Book>> {
        return libraryRemoteDataSource.getUserBooks(userId = userId)
    }

    override suspend fun refreshUserBooks(userId: Int) {
        libraryRemoteDataSource.refreshUserBooks(userId = userId)
    }
}