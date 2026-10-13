package nl.rhaydus.softcover.core.lists.domain.usecase

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.lists.domain.repository.ListsRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class DrainPendingListWritesUseCaseTest {
    private lateinit var listsRepository: ListsRepository
    private lateinit var useCase: DrainPendingListWritesUseCase

    @BeforeEach
    fun setUp() {
        listsRepository = mockk()
        useCase = DrainPendingListWritesUseCase(listsRepository = listsRepository)
    }

    @Nested
    inner class Invoke {
        @Test
        fun `returns success after draining the repository`() = runTest {
            // ----- Arrange -----
            coJustRun { listsRepository.drainPendingListWrites() }

            // ----- Act -----
            val result = useCase()

            // ----- Assert -----
            result.isSuccess shouldBe true

            coVerify(exactly = 1) {
                listsRepository.drainPendingListWrites()
            }
        }

        @Test
        fun `returns failure wrapping the exception when repository throws`() = runTest {
            // ----- Arrange -----
            val error = IllegalStateException("drain failed")

            coEvery {
                listsRepository.drainPendingListWrites()
            } throws error

            // ----- Act -----
            val result = useCase()

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBeSameInstanceAs error
        }
    }
}
