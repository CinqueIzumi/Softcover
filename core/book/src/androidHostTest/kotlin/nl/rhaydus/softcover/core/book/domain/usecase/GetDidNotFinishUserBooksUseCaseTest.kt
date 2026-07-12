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
import nl.rhaydus.softcover.core.domain.model.UserBookStatus

class GetDidNotFinishUserBooksUseCaseTest {
    private lateinit var booksRepository: BooksRepository
    private lateinit var useCase: GetDidNotFinishUserBooksUseCase

    @BeforeEach
    fun setUp() {
        booksRepository = mockk()
        useCase = GetDidNotFinishUserBooksUseCase(booksRepository = booksRepository)
    }

    private fun stubBook(): Book = mockk()

    @Nested
    inner class Invoke {
        @Test
        fun `delegates to repository with DID_NOT_FINISH status`() = runTest {
            // ----- Arrange -----
            val book = stubBook()

            every {
                booksRepository.getBooksFlowByStatus(status = UserBookStatus.DID_NOT_FINISH)
            } returns flowOf(listOf(book))

            // ----- Act & Assert -----
            useCase().test {
                awaitItem() shouldBe listOf(book)
                awaitComplete()
            }
        }

        @Test
        fun `emits empty list when no did-not-finish books exist`() = runTest {
            // ----- Arrange -----
            every {
                booksRepository.getBooksFlowByStatus(status = UserBookStatus.DID_NOT_FINISH)
            } returns flowOf(emptyList())

            // ----- Act & Assert -----
            useCase().test {
                awaitItem() shouldBe emptyList()
                awaitComplete()
            }
        }
    }
}
