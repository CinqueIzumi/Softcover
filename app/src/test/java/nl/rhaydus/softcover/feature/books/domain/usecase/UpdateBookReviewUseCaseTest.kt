package nl.rhaydus.softcover.feature.books.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.feature.books.domain.repository.BooksRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class UpdateBookReviewUseCaseTest {

    private lateinit var booksRepository: BooksRepository
    private lateinit var useCase: UpdateBookReviewUseCase

    @BeforeEach
    fun setUp() {
        booksRepository = mockk()
        useCase = UpdateBookReviewUseCase(booksRepository = booksRepository)
    }

    @Nested
    inner class Invoke {

        @Test
        fun `returns success without touching the repository when book has no userBook`() = runTest {
            // ----- Arrange -----
            val inputBook = mockk<Book>()

            every { inputBook.userBook } returns null

            // ----- Act -----
            val result = useCase(inputBook, body = "Great read.", hasSpoilers = false)

            // ----- Assert -----
            result.isSuccess shouldBe true

            coVerify(exactly = 0) { booksRepository.updateBookReview(book = any(), body = any(), hasSpoilers = any()) }
            coVerify(exactly = 0) { booksRepository.cacheBook(book = any()) }
        }

        @Test
        fun `calls updateBookReview then cacheBook with the returned book and returns success`() = runTest {
            // ----- Arrange -----
            val body = "Absolutely loved it."
            val hasSpoilers = true
            val userBook = mockk<UserBook>()
            val inputBook = mockk<Book>()
            val updatedBook = mockk<Book>()

            every { inputBook.userBook } returns userBook

            coEvery {
                booksRepository.updateBookReview(book = inputBook, body = body, hasSpoilers = hasSpoilers)
            } returns updatedBook

            coJustRun {
                booksRepository.cacheBook(book = updatedBook)
            }

            // ----- Act -----
            val result = useCase(inputBook, body = body, hasSpoilers = hasSpoilers)

            // ----- Assert -----
            result.isSuccess shouldBe true
            coVerify(exactly = 1) { booksRepository.updateBookReview(book = inputBook, body = body, hasSpoilers = hasSpoilers) }
            coVerify(exactly = 1) { booksRepository.cacheBook(book = updatedBook) }
        }
    }
}
