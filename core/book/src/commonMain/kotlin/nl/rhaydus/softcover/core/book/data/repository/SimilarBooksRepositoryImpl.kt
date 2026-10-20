package nl.rhaydus.softcover.core.book.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.book.data.datasource.BooksLocalDataSource
import nl.rhaydus.softcover.core.book.data.datasource.BooksRemoteDataSource
import nl.rhaydus.softcover.core.book.domain.model.SimilarBookCandidate
import nl.rhaydus.softcover.core.book.domain.model.SimilarBookSeed
import nl.rhaydus.softcover.core.book.domain.repository.SimilarBooksRepository
import nl.rhaydus.softcover.core.domain.util.SessionValueCache

internal class SimilarBooksRepositoryImpl(
    private val booksRemoteDataSource: BooksRemoteDataSource,
    private val booksLocalDataSource: BooksLocalDataSource,
) : SimilarBooksRepository {
    private val seeds = SessionValueCache<Int, SimilarBookSeed>()

    override suspend fun getSeed(bookId: Int): SimilarBookSeed = seeds.getOrPut(key = bookId) {
        booksRemoteDataSource.fetchSimilarBookSeed(bookId = bookId)
    }

    override suspend fun fetchCandidates(ids: List<Int>): List<SimilarBookCandidate> =
        booksRemoteDataSource.fetchSimilarCandidates(ids = ids)

    override suspend fun getShelvedBookIds(ids: List<Int>): Set<Int> =
        booksLocalDataSource.getExistingUserBookBookIds(ids = ids).toSet()

    override fun observeShelvedBookIds(ids: List<Int>): Flow<Set<Int>> = booksLocalDataSource
        .observeShelvedBookIds(ids = ids)
        .map { it.toSet() }
}
