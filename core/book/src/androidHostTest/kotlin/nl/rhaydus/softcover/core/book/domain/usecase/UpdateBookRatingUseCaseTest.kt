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
import nl.rhaydus.softcover.core.domain.model.UserBook
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class UpdateBookRatingUseCaseTest {
    private lateinit var booksRepository: BooksRepository
    private lateinit var useCase: UpdateBookRatingUseCase

    @BeforeEach
    fun setUp() {
        booksRepository = mockk()
        useCase = UpdateBookRatingUseCase(booksRepository = booksRepository)
    }

    @Nested
    inner class Invoke {
        @Test
        fun `returns success without touching the repository when book has no userBook`() = runTest {
            // ----- Arrange -----
            val inputBook = mockk<Book>()

            every {
                inputBook.userBook
            } returns null

            // ----- Act -----
            val result = useCase(
                inputBook,
                rating = 4.0,
            )

            // ----- Assert -----
            result.isSuccess shouldBe true
            coVerify(exactly = 0) { booksRepository.updateBookRating(
                book = any(),
                rating = any(),
            ) }
            coVerify(exactly = 0) { booksRepository.cacheBook(book = any()) }
        }

        @Test
        fun `returns success without touching the repository when rating is unchanged`() = runTest {
            // ----- Arrange -----
            val rating = 3.5
            val userBook = mockk<UserBook>()
            val inputBook = mockk<Book>()

            every {
                inputBook.userBook
            } returns userBook
            every {
                userBook.rating
            } returns rating

            // ----- Act -----
            val result = useCase(
                inputBook,
                rating = rating,
            )

            // ----- Assert -----
            result.isSuccess shouldBe true
            coVerify(exactly = 0) { booksRepository.updateBookRating(
                book = any(),
                rating = any(),
            ) }
            coVerify(exactly = 0) { booksRepository.cacheBook(book = any()) }
        }

        @Test
        fun `calls updateBookRating then cacheBook and returns success on a real rating change`() = runTest {
            // ----- Arrange -----
            val newRating = 4.5
            val userBook = mockk<UserBook>()
            val inputBook = mockk<Book>()
            val updatedBook = mockk<Book>()

            every {
                inputBook.userBook
            } returns userBook
            every {
                userBook.rating
            } returns 3.0

            coEvery {
                booksRepository.updateBookRating(
                    book = inputBook,
                    rating = newRating,
                )
            } returns updatedBook

            coJustRun {
                booksRepository.cacheBook(book = updatedBook)
            }

            // ----- Act -----
            val result = useCase(
                inputBook,
                rating = newRating,
            )

            // ----- Assert -----
            result.isSuccess shouldBe true
            coVerify(exactly = 1) { booksRepository.updateBookRating(
                book = inputBook,
                rating = newRating,
            ) }
            coVerify(exactly = 1) { booksRepository.cacheBook(book = updatedBook) }
        }

        @Test
        fun `wraps a repository exception in Result failure`() = runTest {
            // ----- Arrange -----
            val newRating = 4.5
            val userBook = mockk<UserBook>()
            val inputBook = mockk<Book>()
            val expectedError = RuntimeException("network error")

            every {
                inputBook.userBook
            } returns userBook
            every {
                userBook.rating
            } returns 2.0

            coEvery {
                booksRepository.updateBookRating(
                    book = inputBook,
                    rating = newRating,
                )
            } throws expectedError

            // ----- Act -----
            val result = useCase(
                inputBook,
                rating = newRating,
            )

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
            coVerify(exactly = 0) { booksRepository.cacheBook(book = any()) }
        }
    }
}
