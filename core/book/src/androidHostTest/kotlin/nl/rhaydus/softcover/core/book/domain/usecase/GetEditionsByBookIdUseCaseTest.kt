package nl.rhaydus.softcover.core.book.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.domain.model.BookEdition

class GetEditionsByBookIdUseCaseTest {
    private lateinit var booksRepository: BooksRepository
    private lateinit var useCase: GetEditionsByBookIdUseCase

    @BeforeEach
    fun setUp() {
        booksRepository = mockk()
        useCase = GetEditionsByBookIdUseCase(booksRepository = booksRepository)
    }

    private fun stubBookEdition(id: Int = 10): BookEdition = mockk {
        io.mockk.every {
            this@mockk.id
        } returns id
    }

    @Nested
    inner class Invoke {
        @Test
        fun `returns success with list of editions when repository succeeds`() = runTest {
            // ----- Arrange -----
            val bookId = 5
            val expectedEditions = listOf(
                stubBookEdition(id = 10),
                stubBookEdition(id = 11),
            )

            coEvery {
                booksRepository.getEditionsByBookId(bookId = bookId)
            } returns expectedEditions

            // ----- Act -----
            val result = useCase(bookId)

            // ----- Assert -----
            result.isSuccess shouldBe true
            result.getOrNull() shouldBe expectedEditions
        }

        @Test
        fun `returns success with empty list when repository returns no editions`() = runTest {
            // ----- Arrange -----
            val bookId = 5

            coEvery {
                booksRepository.getEditionsByBookId(bookId = bookId)
            } returns emptyList()

            // ----- Act -----
            val result = useCase(bookId)

            // ----- Assert -----
            result.isSuccess shouldBe true
            result.getOrNull() shouldBe emptyList()
        }

        @Test
        fun `returns failure when repository throws`() = runTest {
            // ----- Arrange -----
            val bookId = 5
            val expectedError = IllegalStateException("network error")

            coEvery {
                booksRepository.getEditionsByBookId(bookId = bookId)
            } throws expectedError

            // ----- Act -----
            val result = useCase(bookId)

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
        }

        @Test
        fun `returns success with single edition when repository returns one edition`() = runTest {
            // ----- Arrange -----
            val bookId = 7
            val singleEdition = stubBookEdition(id = 42)

            coEvery {
                booksRepository.getEditionsByBookId(bookId = bookId)
            } returns listOf(singleEdition)

            // ----- Act -----
            val result = useCase(bookId)

            // ----- Assert -----
            result.isSuccess shouldBe true
            result.getOrNull()?.size shouldBe 1
            result.getOrNull()?.get(0) shouldBe singleEdition
        }
    }
}
