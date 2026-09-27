package nl.rhaydus.softcover.core.lists.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.lists.domain.repository.ListsRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class AddBookToListUseCaseTest {
    private lateinit var listsRepository: ListsRepository
    private lateinit var useCase: AddBookToListUseCase

    @BeforeEach
    fun setUp() {
        listsRepository = mockk()
        useCase = AddBookToListUseCase(listsRepository = listsRepository)
    }

    private fun stubEdition(id: Int): BookEdition = mockk {
        every {
            this@mockk.id
        } returns id
    }

    @Nested
    inner class Invoke {
        @Test
        fun `returns success when repository call completes without error`() = runTest {
            // ----- Arrange -----
            val listId = 42
            val bookId = 7
            val edition = stubEdition(id = 10)

            coJustRun {
                listsRepository.addBookToList(
                    listId = listId,
                    bookId = bookId,
                    edition = edition,
                )
            }

            // ----- Act -----
            val result = useCase(
                listId = listId,
                bookId = bookId,
                edition = edition,
            )

            // ----- Assert -----
            result.isSuccess shouldBe true
        }

        @Test
        fun `delegates to repository with correct arguments`() = runTest {
            // ----- Arrange -----
            val listId = 42
            val bookId = 7
            val edition = stubEdition(id = 10)

            coJustRun {
                listsRepository.addBookToList(
                    listId = listId,
                    bookId = bookId,
                    edition = edition,
                )
            }

            // ----- Act -----
            useCase(
                listId = listId,
                bookId = bookId,
                edition = edition,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                listsRepository.addBookToList(
                    listId = listId,
                    bookId = bookId,
                    edition = edition,
                )
            }
        }

        @Test
        fun `returns failure wrapping the exception when repository throws`() = runTest {
            // ----- Arrange -----
            val listId = 42
            val bookId = 7
            val edition = stubEdition(id = 10)
            val expectedError = RuntimeException("network error")

            coEvery {
                listsRepository.addBookToList(
                    listId = listId,
                    bookId = bookId,
                    edition = edition,
                )
            } throws expectedError

            // ----- Act -----
            val result = useCase(
                listId = listId,
                bookId = bookId,
                edition = edition,
            )

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
        }
    }
}
