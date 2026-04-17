package nl.rhaydus.softcover.feature.books.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.books.domain.repository.BooksRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class FetchBookByIdUseCaseTest {

    private lateinit var booksRepository: BooksRepository
    private lateinit var getAllUserBooksUseCase: GetAllUserBooksUseCase
    private lateinit var useCase: FetchBookByIdUseCase

    @BeforeEach
    fun setUp() {
        booksRepository = mockk()
        getAllUserBooksUseCase = mockk()
        useCase = FetchBookByIdUseCase(
            booksRepository = booksRepository,
            getAllUserBooksUseCase = getAllUserBooksUseCase,
        )
    }

    private fun stubBook(id: Int): Book = mockk {
        every {
            this@mockk.id
        } returns id
    }

    @Nested
    inner class Invoke {

        @Test
        fun `returns user book when matching id is present in user books`() = runTest {
            // ----- Arrange -----
            val bookId = 42
            val originalBook = stubBook(bookId)
            val userVariant = stubBook(bookId)
            val otherUserBook = stubBook(7)

            coEvery {
                booksRepository.fetchBookById(id = bookId)
            } returns originalBook

            every {
                getAllUserBooksUseCase()
            } returns flowOf(
                listOf(
                    otherUserBook,
                    userVariant,
                )
            )

            // ----- Act -----
            val result = useCase(bookId)

            // ----- Assert -----
            result.isSuccess shouldBe true
            result.getOrNull() shouldBe userVariant
        }

        @Test
        fun `returns original book when no user book matches the id`() = runTest {
            // ----- Arrange -----
            val bookId = 42
            val originalBook = stubBook(bookId)
            val unrelatedBook = stubBook(99)

            coEvery {
                booksRepository.fetchBookById(id = bookId)
            } returns originalBook

            every {
                getAllUserBooksUseCase()
            } returns flowOf(listOf(unrelatedBook))

            // ----- Act -----
            val result = useCase(bookId)

            // ----- Assert -----
            result.isSuccess shouldBe true
            result.getOrNull() shouldBe originalBook
        }

        @Test
        fun `returns original book when user books list is empty`() = runTest {
            // ----- Arrange -----
            val bookId = 1
            val originalBook = stubBook(bookId)

            coEvery {
                booksRepository.fetchBookById(id = bookId)
            } returns originalBook

            every {
                getAllUserBooksUseCase()
            } returns flowOf(emptyList())

            // ----- Act -----
            val result = useCase(bookId)

            // ----- Assert -----
            result.isSuccess shouldBe true
            result.getOrNull() shouldBe originalBook
        }

        @Test
        fun `returns original book when user books flow emits nothing`() = runTest {
            // ----- Arrange -----
            val bookId = 1
            val originalBook = stubBook(bookId)

            coEvery {
                booksRepository.fetchBookById(id = bookId)
            } returns originalBook

            every {
                getAllUserBooksUseCase()
            } returns emptyFlow()

            // ----- Act -----
            val result = useCase(bookId)

            // ----- Assert -----
            result.isSuccess shouldBe true
            result.getOrNull() shouldBe originalBook
        }

        @Test
        fun `returns failure when repository throws`() = runTest {
            // ----- Arrange -----
            val bookId = 5
            val expectedError = IllegalStateException("boom")

            coEvery {
                booksRepository.fetchBookById(id = bookId)
            } throws expectedError

            // ----- Act -----
            val result = useCase(bookId)

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
        }

        @Test
        fun `returns failure when user books flow throws`() = runTest {
            // ----- Arrange -----
            val bookId = 5
            val originalBook = stubBook(bookId)
            val expectedError = IllegalStateException("flow boom")

            coEvery {
                booksRepository.fetchBookById(id = bookId)
            } returns originalBook

            every {
                getAllUserBooksUseCase()
            } returns flow { throw expectedError }

            // ----- Act -----
            val result = useCase(bookId)

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
        }
    }
}
