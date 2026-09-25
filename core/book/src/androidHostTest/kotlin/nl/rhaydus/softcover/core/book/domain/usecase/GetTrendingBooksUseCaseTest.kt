package nl.rhaydus.softcover.core.book.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.domain.model.Book

class GetTrendingBooksUseCaseTest {
    private lateinit var booksRepository: BooksRepository
    private lateinit var useCase: GetTrendingBooksUseCase

    @BeforeEach
    fun setUp() {
        booksRepository = mockk()
        useCase = GetTrendingBooksUseCase(booksRepository = booksRepository)
    }

    @Nested
    inner class Invoke {
        @Test
        fun `returns Result success wrapping the list returned by the repository`() = runTest {
            // ----- Arrange -----
            val trendingBooks = listOf(mockk<Book>(relaxed = true), mockk<Book>(relaxed = true))

            coEvery {
                booksRepository.fetchTrendingBooks(forceRefresh = any())
            } returns trendingBooks

            // ----- Act -----
            val result = useCase()

            // ----- Assert -----
            result.isSuccess shouldBe true
            result.getOrNull() shouldBe trendingBooks
        }

        @Test
        fun `returns Result failure when the repository throws`() = runTest {
            // ----- Arrange -----
            val error = RuntimeException("network error")

            coEvery {
                booksRepository.fetchTrendingBooks(forceRefresh = any())
            } throws error

            // ----- Act -----
            val result = useCase()

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe error
        }

        @Test
        fun `returns Result success wrapping an empty list when the repository returns no books`() = runTest {
            // ----- Arrange -----
            coEvery {
                booksRepository.fetchTrendingBooks(forceRefresh = any())
            } returns emptyList()

            // ----- Act -----
            val result = useCase()

            // ----- Assert -----
            result.isSuccess shouldBe true
            result.getOrNull() shouldBe emptyList()
        }

        @Test
        fun `default call forwards forceRefresh false to the repository`() = runTest {
            // ----- Arrange -----
            coEvery {
                booksRepository.fetchTrendingBooks(forceRefresh = any())
            } returns emptyList()

            // ----- Act -----
            useCase()

            // ----- Assert -----
            coVerify {
                booksRepository.fetchTrendingBooks(forceRefresh = false)
            }
        }

        @Test
        fun `forceRefresh true is forwarded verbatim to the repository`() = runTest {
            // ----- Arrange -----
            coEvery {
                booksRepository.fetchTrendingBooks(forceRefresh = any())
            } returns emptyList()

            // ----- Act -----
            useCase(forceRefresh = true)

            // ----- Assert -----
            coVerify {
                booksRepository.fetchTrendingBooks(forceRefresh = true)
            }
        }
    }
}
