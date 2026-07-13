package nl.rhaydus.softcover.core.book.domain.usecase

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.domain.model.Book

class GetAllUserBooksUseCaseTest {
    private lateinit var booksRepository: BooksRepository
    private lateinit var useCase: GetAllUserBooksUseCase

    @BeforeEach
    fun setUp() {
        booksRepository = mockk()
        useCase = GetAllUserBooksUseCase(booksRepository = booksRepository)
    }

    private fun stubBook(): Book = mockk()

    @Nested
    inner class Invoke {
        @Test
        fun `delegates to repository books flow`() = runTest {
            // ----- Arrange -----
            val book = stubBook()

            every {
                booksRepository.books
            } returns flowOf(listOf(book))

            // ----- Act & Assert -----
            useCase().test {
                awaitItem() shouldBe listOf(book)
                awaitComplete()
            }
        }

        @Test
        fun `emits empty list when repository emits empty list`() = runTest {
            // ----- Arrange -----
            every {
                booksRepository.books
            } returns flowOf(emptyList())

            // ----- Act & Assert -----
            useCase().test {
                awaitItem() shouldBe emptyList()
                awaitComplete()
            }
        }

        @Test
        fun `emits multiple emissions when repository flow emits multiple times`() = runTest {
            // ----- Arrange -----
            val firstBook = stubBook()
            val secondBook = stubBook()

            every {
                booksRepository.books
            } returns flowOf(
                listOf(firstBook),
                listOf(firstBook, secondBook),
            )

            // ----- Act & Assert -----
            useCase().test {
                awaitItem() shouldBe listOf(firstBook)
                awaitItem() shouldBe listOf(firstBook, secondBook)
                awaitComplete()
            }
        }
    }
}
