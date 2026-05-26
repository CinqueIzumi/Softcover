package nl.rhaydus.softcover.feature.lists.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.feature.lists.domain.repository.ListsRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ReorderListBooksUseCaseTest {

    private lateinit var listsRepository: ListsRepository
    private lateinit var useCase: ReorderListBooksUseCase

    @BeforeEach
    fun setUp() {
        listsRepository = mockk()
        useCase = ReorderListBooksUseCase(listsRepository = listsRepository)
    }

    @Nested
    inner class Invoke {

        @Test
        fun `returns success and delegates with correct arguments when repository completes`() = runTest {
            // ----- Arrange -----
            val listId = 42
            val startPosition = 1
            val orderedIds = listOf(10, 20, 30)

            coJustRun {
                listsRepository.reorderListBooks(
                    listId = listId,
                    startPosition = startPosition,
                    orderedListBookIds = orderedIds,
                )
            }

            // ----- Act -----
            val result = useCase(
                listId = listId,
                startPosition = startPosition,
                orderedListBookIds = orderedIds,
            )

            // ----- Assert -----
            result.isSuccess shouldBe true

            coVerify(exactly = 1) {
                listsRepository.reorderListBooks(
                    listId = listId,
                    startPosition = startPosition,
                    orderedListBookIds = orderedIds,
                )
            }
        }

        @Test
        fun `returns failure wrapping the throwable when repository throws`() = runTest {
            // ----- Arrange -----
            val listId = 42
            val startPosition = 0
            val orderedIds = listOf(5, 6)
            val expectedError = RuntimeException("server error")

            coEvery {
                listsRepository.reorderListBooks(
                    listId = listId,
                    startPosition = startPosition,
                    orderedListBookIds = orderedIds,
                )
            } throws expectedError

            // ----- Act -----
            val result = useCase(
                listId = listId,
                startPosition = startPosition,
                orderedListBookIds = orderedIds,
            )

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
        }
    }
}
