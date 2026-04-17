package nl.rhaydus.softcover.feature.books.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.ListBook
import nl.rhaydus.softcover.feature.books.domain.repository.BooksRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class RemoveListBookUseCaseTest {

    private lateinit var booksRepository: BooksRepository
    private lateinit var useCase: RemoveListBookUseCase

    @BeforeEach
    fun setUp() {
        booksRepository = mockk()
        useCase = RemoveListBookUseCase(booksRepository = booksRepository)
    }

    @Nested
    inner class Invoke {

        @Test
        fun `delegates removeListBook to repository and returns success`() = runTest {
            // ----- Arrange -----
            val listBook = mockk<ListBook>()

            coJustRun {
                booksRepository.removeListBook(book = listBook)
            }

            // ----- Act -----
            val result = useCase(listBook)

            // ----- Assert -----
            result.isSuccess shouldBe true
            coVerify(exactly = 1) { booksRepository.removeListBook(book = listBook) }
        }

        @Test
        fun `returns failure when repository throws`() = runTest {
            // ----- Arrange -----
            val listBook = mockk<ListBook>()
            val expectedError = RuntimeException("network error")

            coEvery {
                booksRepository.removeListBook(book = listBook)
            } throws expectedError

            // ----- Act -----
            val result = useCase(listBook)

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
        }
    }
}
