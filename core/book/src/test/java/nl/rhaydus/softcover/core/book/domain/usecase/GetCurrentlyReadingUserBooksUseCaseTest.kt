package nl.rhaydus.softcover.core.book.domain.usecase

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class GetCurrentlyReadingUserBooksUseCaseTest {

    private lateinit var booksRepository: BooksRepository
    private lateinit var useCase: GetCurrentlyReadingUserBooksUseCase

    @BeforeEach
    fun setUp() {
        booksRepository = mockk()
        useCase = GetCurrentlyReadingUserBooksUseCase(booksRepository = booksRepository)
    }

    private fun stubBook(): Book = mockk()

    @Nested
    inner class Invoke {

        @Test
        fun `delegates to repository with CURRENTLY_READING status`() = runTest {
            // ----- Arrange -----
            val book = stubBook()

            every {
                booksRepository.getBooksFlowByStatus(status = UserBookStatus.CURRENTLY_READING)
            } returns flowOf(listOf(book))

            // ----- Act & Assert -----
            useCase().test {
                awaitItem() shouldBe listOf(book)
                awaitComplete()
            }
        }

        @Test
        fun `emits empty list when no currently reading books exist`() = runTest {
            // ----- Arrange -----
            every {
                booksRepository.getBooksFlowByStatus(status = UserBookStatus.CURRENTLY_READING)
            } returns flowOf(emptyList())

            // ----- Act & Assert -----
            useCase().test {
                awaitItem() shouldBe emptyList()
                awaitComplete()
            }
        }
    }
}
