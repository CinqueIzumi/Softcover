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

class GetReadUserBooksUseCaseTest {

    private lateinit var booksRepository: BooksRepository
    private lateinit var useCase: GetReadUserBooksUseCase

    @BeforeEach
    fun setUp() {
        booksRepository = mockk()
        useCase = GetReadUserBooksUseCase(booksRepository = booksRepository)
    }

    private fun stubBook(): Book = mockk()

    @Nested
    inner class Invoke {

        @Test
        fun `delegates to repository with READ status`() = runTest {
            // ----- Arrange -----
            val book = stubBook()

            every {
                booksRepository.getBooksFlowByStatus(status = UserBookStatus.READ)
            } returns flowOf(listOf(book))

            // ----- Act & Assert -----
            useCase().test {
                awaitItem() shouldBe listOf(book)
                awaitComplete()
            }
        }

        @Test
        fun `emits empty list when no read books exist`() = runTest {
            // ----- Arrange -----
            every {
                booksRepository.getBooksFlowByStatus(status = UserBookStatus.READ)
            } returns flowOf(emptyList())

            // ----- Act & Assert -----
            useCase().test {
                awaitItem() shouldBe emptyList()
                awaitComplete()
            }
        }
    }
}
