package nl.rhaydus.softcover.feature.books.domain.usecase

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.feature.books.domain.repository.BooksRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class GetAllUserListsUseCaseTest {

    private lateinit var booksRepository: BooksRepository
    private lateinit var useCase: GetAllUserListsUseCase

    @BeforeEach
    fun setUp() {
        booksRepository = mockk()
        useCase = GetAllUserListsUseCase(booksRepository = booksRepository)
    }

    private fun stubBookList(): BookList = mockk()

    @Nested
    inner class Invoke {

        @Test
        fun `delegates to repository allUserLists flow`() = runTest {
            // ----- Arrange -----
            val bookList = stubBookList()

            every {
                booksRepository.allUserLists
            } returns flowOf(listOf(bookList))

            // ----- Act & Assert -----
            useCase().test {
                awaitItem() shouldBe listOf(bookList)
                awaitComplete()
            }
        }

        @Test
        fun `emits empty list when repository emits empty list`() = runTest {
            // ----- Arrange -----
            every {
                booksRepository.allUserLists
            } returns flowOf(emptyList())

            // ----- Act & Assert -----
            useCase().test {
                awaitItem() shouldBe emptyList()
                awaitComplete()
            }
        }
    }
}
