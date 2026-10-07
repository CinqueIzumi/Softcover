package nl.rhaydus.softcover.core.book.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.book.domain.model.StagedBookRemoval
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.domain.model.Book
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class StageBookRemovalUseCaseTest {
    private lateinit var booksRepository: BooksRepository
    private lateinit var useCase: StageBookRemovalUseCase

    @BeforeEach
    fun setUp() {
        booksRepository = mockk()
        useCase = StageBookRemovalUseCase(booksRepository = booksRepository)
    }

    @Nested
    inner class Invoke {
        @Test
        fun `delegates to the repository and returns its result`() = runTest {
            // ----- Arrange -----
            val input = listOf(mockk<Book>())
            val expected = mockk<StagedBookRemoval>()

            coEvery {
                booksRepository.stageBookRemoval(books = input)
            } returns expected

            // ----- Act -----
            val result = useCase(input)

            // ----- Assert -----
            result.isSuccess shouldBe true
            result.getOrNull() shouldBe expected

            coVerify(exactly = 1) {
                booksRepository.stageBookRemoval(books = input)
            }
        }

        @Test
        fun `wraps a repository exception in a failed Result`() = runTest {
            // ----- Arrange -----
            val input = listOf(mockk<Book>())
            val expectedError = RuntimeException("boom")

            coEvery {
                booksRepository.stageBookRemoval(books = input)
            } throws expectedError

            // ----- Act -----
            val result = useCase(input)

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
        }
    }
}
