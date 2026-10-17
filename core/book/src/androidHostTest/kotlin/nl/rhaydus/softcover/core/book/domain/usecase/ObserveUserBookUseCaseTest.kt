package nl.rhaydus.softcover.core.book.domain.usecase

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.domain.model.Book
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ObserveUserBookUseCaseTest {
    private lateinit var booksRepository: BooksRepository
    private lateinit var useCase: ObserveUserBookUseCase

    @BeforeEach
    fun setUp() {
        booksRepository = mockk()
        useCase = ObserveUserBookUseCase(booksRepository = booksRepository)
    }

    @Nested
    inner class Invoke {
        @Test
        fun `delegates to repository with the given id`() = runTest {
            // ----- Arrange -----
            val book = mockk<Book>()

            every {
                booksRepository.observeUserBook(id = 5)
            } returns flowOf(book)

            // ----- Act & Assert -----
            useCase(id = 5).test {
                awaitItem() shouldBe book
                awaitComplete()
            }
        }

        @Test
        fun `passes through a null book`() = runTest {
            // ----- Arrange -----
            every {
                booksRepository.observeUserBook(id = 5)
            } returns flowOf(null)

            // ----- Act & Assert -----
            useCase(id = 5).test {
                awaitItem() shouldBe null
                awaitComplete()
            }
        }
    }
}
