package nl.rhaydus.softcover.feature.books.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.feature.books.domain.repository.BooksRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ReorderShelfBooksUseCaseTest {

    private lateinit var booksRepository: BooksRepository
    private lateinit var useCase: ReorderShelfBooksUseCase

    @BeforeEach
    fun setUp() {
        booksRepository = mockk(relaxed = true)
        useCase = ReorderShelfBooksUseCase(booksRepository = booksRepository)
    }

    @Nested
    inner class Invoke {

        @Test
        fun `delegates to repository applyShelfManualOrderPrefix with the given status and bookIds`() = runTest {
            // ----- Arrange -----
            val status = UserBookStatus.READ
            val bookIds = listOf(3, 1, 2)

            // ----- Act -----
            useCase(status = status, prefixOrderedBookIds = bookIds)

            // ----- Assert -----
            coVerify {
                booksRepository.applyShelfManualOrderPrefix(
                    status = status,
                    prefixBookIds = bookIds,
                )
            }
        }

        @Test
        fun `returns success when repository succeeds`() = runTest {
            // ----- Arrange -----
            val status = UserBookStatus.WANT_TO_READ
            val bookIds = listOf(10, 20)

            // ----- Act -----
            val result = useCase(status = status, prefixOrderedBookIds = bookIds)

            // ----- Assert -----
            result.isSuccess shouldBe true
        }

        @Test
        fun `returns failure and does not throw when repository throws`() = runTest {
            // ----- Arrange -----
            val status = UserBookStatus.CURRENTLY_READING
            val bookIds = listOf(5, 7)
            val expectedError = RuntimeException("db write failed")

            coEvery {
                booksRepository.applyShelfManualOrderPrefix(
                    status = status,
                    prefixBookIds = bookIds,
                )
            } throws expectedError

            // ----- Act -----
            val result = useCase(status = status, prefixOrderedBookIds = bookIds)

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
        }
    }
}
