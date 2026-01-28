package nl.rhaydus.softcover.feature.library.data.datasource

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.domain.model.Book

interface LibraryRemoteDataSource {
    suspend fun getUserBooks(userId: Int): Flow<List<Book>>

    suspend fun refreshUserBooks(userId: Int)
}