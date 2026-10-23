package nl.rhaydus.softcover.core.book.data.repository

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.book.data.datasource.BooksLocalDataSource
import nl.rhaydus.softcover.core.book.data.datasource.BooksRemoteDataSource
import nl.rhaydus.softcover.core.book.domain.model.SimilarBookSeed
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SimilarBooksRepositoryImplTest {
    private lateinit var remote: BooksRemoteDataSource
    private lateinit var local: BooksLocalDataSource
    private lateinit var repository: SimilarBooksRepositoryImpl

    @BeforeEach
    fun setUp() {
        remote = mockk()
        local = mockk()
        repository = SimilarBooksRepositoryImpl(
            booksRemoteDataSource = remote,
            booksLocalDataSource = local,
        )
    }

    private fun buildSeed(bookId: Int): SimilarBookSeed = SimilarBookSeed(
        sourceBookId = bookId,
        sourceCanonicalId = null,
        rankedIds = listOf(2, 3),
        excludedSeriesBookIds = emptySet(),
        sourceSeriesIds = emptySet(),
    )

    @Nested
    inner class GetSeed {
        @Test
        fun `fetches the seed once per book id`() = runTest {
            // ----- Arrange -----
            coEvery {
                remote.fetchSimilarBookSeed(bookId = 1)
            } returns buildSeed(bookId = 1)
            coEvery {
                remote.fetchSimilarBookSeed(bookId = 2)
            } returns buildSeed(bookId = 2)

            // ----- Act -----
            val first = repository.getSeed(bookId = 1)
            val again = repository.getSeed(bookId = 1)
            val other = repository.getSeed(bookId = 2)

            // ----- Assert -----
            again shouldBe first
            other.sourceBookId shouldBe 2
            coVerify(exactly = 1) { remote.fetchSimilarBookSeed(bookId = 1) }
            coVerify(exactly = 1) { remote.fetchSimilarBookSeed(bookId = 2) }
        }

        @Test
        fun `refetches after a failure instead of caching it`() = runTest {
            // ----- Arrange -----
            val seed = buildSeed(bookId = 1)
            coEvery {
                remote.fetchSimilarBookSeed(bookId = 1)
            } throws IllegalStateException("boom") andThen seed

            // ----- Act -----
            shouldThrow<IllegalStateException> { repository.getSeed(bookId = 1) }
            val retried = repository.getSeed(bookId = 1)

            // ----- Assert -----
            retried shouldBe seed
            coVerify(exactly = 2) { remote.fetchSimilarBookSeed(bookId = 1) }
        }
    }
}
