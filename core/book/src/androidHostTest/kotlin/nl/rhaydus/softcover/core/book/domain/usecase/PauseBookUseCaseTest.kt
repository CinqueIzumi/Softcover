package nl.rhaydus.softcover.core.book.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.UserBook
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PauseBookUseCaseTest {
    private lateinit var booksRepository: BooksRepository
    private lateinit var useCase: PauseBookUseCase

    @BeforeEach
    fun setUp() {
        booksRepository = mockk()
        useCase = PauseBookUseCase(booksRepository = booksRepository)
    }

    @Nested
    inner class Invoke {
        @Test
        fun `marks book as paused then caches the updated book`() = runTest {
            // ----- Arrange -----
            val inputBook = mockk<Book>()
            val updatedBook = mockk<Book>()

            every {
                inputBook.status
            } returns BookStatus.Reading

            every {
                inputBook.userBook
            } returns null

            every {
                inputBook.currentEdition
            } returns null

            coEvery {
                booksRepository.markBookAsPaused(
                    book = inputBook,
                    editionId = null,
                )
            } returns updatedBook

            coJustRun {
                booksRepository.cacheBook(book = updatedBook)
            }

            // ----- Act -----
            val result = useCase(inputBook)

            // ----- Assert -----
            result.getOrNull() shouldBe ShelfMutationOutcome.Applied
            coVerify(exactly = 1) {
                booksRepository.markBookAsPaused(
                    book = inputBook,
                    editionId = null,
                )
            }
            coVerify(exactly = 1) { booksRepository.cacheBook(book = updatedBook) }
        }

        @Test
        fun `forwards a non-null editionId to the repository`() = runTest {
            // ----- Arrange -----
            val inputBook = mockk<Book>()
            val editionId = 99
            val updatedBook = mockk<Book>()

            every {
                inputBook.status
            } returns BookStatus.None

            every {
                inputBook.userBook
            } returns null

            coEvery {
                booksRepository.markBookAsPaused(
                    book = inputBook,
                    editionId = editionId,
                )
            } returns updatedBook

            coJustRun {
                booksRepository.cacheBook(book = updatedBook)
            }

            // ----- Act -----
            useCase(
                book = inputBook,
                editionId = editionId,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                booksRepository.markBookAsPaused(
                    book = inputBook,
                    editionId = editionId,
                )
            }
        }

        @Test
        fun `shelved book keeps its stored edition and ignores the requested one`() = runTest {
            // ----- Arrange -----
            val inputBook = mockk<Book>()
            val userBook = mockk<UserBook>()
            val updatedBook = mockk<Book>()

            every {
                inputBook.status
            } returns BookStatus.Reading

            every {
                inputBook.userBook
            } returns userBook

            every {
                userBook.editionId
            } returns 5

            coEvery {
                booksRepository.markBookAsPaused(
                    book = inputBook,
                    editionId = 5,
                )
            } returns updatedBook

            coJustRun {
                booksRepository.cacheBook(book = updatedBook)
            }

            // ----- Act -----
            useCase(
                book = inputBook,
                editionId = 99,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                booksRepository.markBookAsPaused(
                    book = inputBook,
                    editionId = 5,
                )
            }
        }

        @Test
        fun `forwards the current edition when none is requested and the book is off the shelf`() = runTest {
            // ----- Arrange -----
            val inputBook = mockk<Book>()
            val currentEdition = mockk<BookEdition>()
            val updatedBook = mockk<Book>()

            every {
                inputBook.status
            } returns BookStatus.None

            every {
                inputBook.userBook
            } returns null

            every {
                inputBook.currentEdition
            } returns currentEdition

            every {
                currentEdition.id
            } returns 7

            coEvery {
                booksRepository.markBookAsPaused(
                    book = inputBook,
                    editionId = 7,
                )
            } returns updatedBook

            coJustRun {
                booksRepository.cacheBook(book = updatedBook)
            }

            // ----- Act -----
            useCase(inputBook)

            // ----- Assert -----
            coVerify(exactly = 1) {
                booksRepository.markBookAsPaused(
                    book = inputBook,
                    editionId = 7,
                )
            }
        }

        @Test
        fun `returns failure and skips caching when markBookAsPaused throws`() = runTest {
            // ----- Arrange -----
            val inputBook = mockk<Book>()
            val expectedError = RuntimeException("network error")

            every {
                inputBook.status
            } returns BookStatus.Reading

            every {
                inputBook.userBook
            } returns null

            every {
                inputBook.currentEdition
            } returns null

            coEvery {
                booksRepository.markBookAsPaused(
                    book = inputBook,
                    editionId = null,
                )
            } throws expectedError

            // ----- Act -----
            val result = useCase(inputBook)

            // ----- Assert -----
            result.exceptionOrNull() shouldBe expectedError
            coVerify(exactly = 0) { booksRepository.cacheBook(book = any()) }
        }

        @Test
        fun `returns failure when cacheBook throws`() = runTest {
            // ----- Arrange -----
            val inputBook = mockk<Book>()
            val updatedBook = mockk<Book>()
            val expectedError = RuntimeException("cache error")

            every {
                inputBook.status
            } returns BookStatus.Reading

            every {
                inputBook.userBook
            } returns null

            every {
                inputBook.currentEdition
            } returns null

            coEvery {
                booksRepository.markBookAsPaused(
                    book = inputBook,
                    editionId = null,
                )
            } returns updatedBook

            coEvery {
                booksRepository.cacheBook(book = updatedBook)
            } throws expectedError

            // ----- Act -----
            val result = useCase(inputBook)

            // ----- Assert -----
            result.exceptionOrNull() shouldBe expectedError
        }

        @Test
        fun `returns NoChange and does not touch repository when book is already Paused`() = runTest {
            // ----- Arrange -----
            val inputBook = mockk<Book>()

            every {
                inputBook.status
            } returns BookStatus.Paused

            // ----- Act -----
            val result = useCase(inputBook)

            // ----- Assert -----
            result.getOrNull() shouldBe ShelfMutationOutcome.NoChange
            coVerify(exactly = 0) {
                booksRepository.markBookAsPaused(
                    book = any(),
                    editionId = any(),
                )
            }
            coVerify(exactly = 0) { booksRepository.cacheBook(book = any()) }
        }
    }
}
