package nl.rhaydus.softcover.feature.library.domain.repository

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.domain.model.Book

interface LibraryRepository {
    suspend fun getUserBooks(userId: Int): Flow<List<Book>>

    suspend fun refreshUserBooks(userId: Int)
}