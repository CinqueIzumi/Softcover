package nl.rhaydus.softcover.feature.books.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.books.domain.repository.BooksRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class RemoveBookFromLibraryUseCaseTest {

    private lateinit var booksRepository: BooksRepository
    private lateinit var useCase: RemoveBookFromLibraryUseCase

    @BeforeEach
    fun setUp() {
        booksRepository = mockk()
        useCase = RemoveBookFromLibraryUseCase(booksRepository = booksRepository)
    }

    @Nested
    inner class Invoke {

        @Test
        fun `removes book from library and returns success`() = runTest {
            // ----- Arrange -----
            val book = mockk<Book>()

            coJustRun {
                booksRepository.removeBookFromLibrary(book = book)
            }

            // ----- Act -----
            val result = useCase(book)

            // ----- Assert -----
            result.isSuccess shouldBe true

            coVerify(exactly = 1) { booksRepository.removeBookFromLibrary(book = book) }
        }

        @Test
        fun `returns failure when removeBookFromLibrary throws`() = runTest {
            // ----- Arrange -----
            val book = mockk<Book>()
            val expectedError = RuntimeException("network error")

            coEvery {
                booksRepository.removeBookFromLibrary(book = book)
            } throws expectedError

            // ----- Act -----
            val result = useCase(book)

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
        }
    }
}
