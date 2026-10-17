package nl.rhaydus.softcover.core.lists.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.lists.domain.repository.ListsRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class RemoveBookFromListUseCaseTest {
    private lateinit var listsRepository: ListsRepository
    private lateinit var useCase: RemoveBookFromListUseCase

    @BeforeEach
    fun setUp() {
        listsRepository = mockk()
        useCase = RemoveBookFromListUseCase(listsRepository = listsRepository)
    }

    @Nested
    inner class Invoke {
        @Test
        fun `returns success and forwards the list and book to the repository`() = runTest {
            // ----- Arrange -----
            val listId = 42
            val bookId = 7

            coJustRun {
                listsRepository.removeBookFromList(
                    listId = listId,
                    bookId = bookId,
                )
            }

            // ----- Act -----
            val result = useCase(
                listId = listId,
                bookId = bookId,
            )

            // ----- Assert -----
            result.isSuccess shouldBe true

            coVerify(exactly = 1) {
                listsRepository.removeBookFromList(
                    listId = listId,
                    bookId = bookId,
                )
            }
        }

        @Test
        fun `returns failure wrapping the exception when repository throws`() = runTest {
            // ----- Arrange -----
            val listId = 42
            val bookId = 7
            val expectedError = RuntimeException("network error")

            coEvery {
                listsRepository.removeBookFromList(
                    listId = listId,
                    bookId = bookId,
                )
            } throws expectedError

            // ----- Act -----
            val result = useCase(
                listId = listId,
                bookId = bookId,
            )

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
        }
    }
}
