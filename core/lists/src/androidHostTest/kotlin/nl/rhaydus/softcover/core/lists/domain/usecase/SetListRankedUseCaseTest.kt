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

class SetListRankedUseCaseTest {
    private lateinit var listsRepository: ListsRepository
    private lateinit var useCase: SetListRankedUseCase

    @BeforeEach
    fun setUp() {
        listsRepository = mockk()
        useCase = SetListRankedUseCase(listsRepository = listsRepository)
    }

    @Nested
    inner class Invoke {
        @Test
        fun `returns success and delegates with correct arguments when repository completes`() = runTest {
            // ----- Arrange -----
            val listId = 10
            val ranked = true

            coJustRun {
                listsRepository.setListRanked(
                    listId = listId,
                    ranked = ranked,
                )
            }

            // ----- Act -----
            val result = useCase(
                listId = listId,
                ranked = ranked,
            )

            // ----- Assert -----
            result.isSuccess shouldBe true

            coVerify(exactly = 1) {
                listsRepository.setListRanked(
                    listId = listId,
                    ranked = ranked,
                )
            }
        }

        @Test
        fun `returns failure wrapping the throwable when repository throws`() = runTest {
            // ----- Arrange -----
            val listId = 10
            val ranked = false
            val expectedError = RuntimeException("server error")

            coEvery {
                listsRepository.setListRanked(
                    listId = listId,
                    ranked = ranked,
                )
            } throws expectedError

            // ----- Act -----
            val result = useCase(
                listId = listId,
                ranked = ranked,
            )

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
        }
    }
}
