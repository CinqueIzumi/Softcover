package nl.rhaydus.softcover.core.book.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.domain.model.TrendingPeriod
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.hours

class RefreshTrendingBooksUseCaseTest {
    private lateinit var booksRepository: BooksRepository
    private lateinit var useCase: RefreshTrendingBooksUseCase

    @BeforeEach
    fun setUp() {
        booksRepository = mockk()
        useCase = RefreshTrendingBooksUseCase(booksRepository = booksRepository)
    }

    @Nested
    inner class Invoke {
        @Test
        fun `returns Result failure carrying the exception when the repository throws`() = runTest {
            // ----- Arrange -----
            val error = RuntimeException("network error")

            coEvery {
                booksRepository.refreshTrendingBooks(
                    period = any(),
                    maxAge = any(),
                )
            } throws error

            // ----- Act -----
            val result = useCase(
                period = TrendingPeriod.Week,
                maxAge = 1.hours,
            )

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe error
        }
    }
}
