package nl.rhaydus.softcover.feature.explore.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.feature.explore.domain.repository.ExploreRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class UndoContinueSeriesBookDismissalUseCaseTest {
    private lateinit var exploreRepository: ExploreRepository
    private lateinit var useCase: UndoContinueSeriesBookDismissalUseCase

    @BeforeEach
    fun setUp() {
        exploreRepository = mockk(relaxed = true)
        useCase = UndoContinueSeriesBookDismissalUseCase(exploreRepository = exploreRepository)
    }

    @Nested
    inner class Invoke {
        @Test
        fun `delegates to repository undoContinueSeriesBookDismissal with correct bookId`() = runTest {
            // ----- Arrange -----
            val bookId = 77

            // ----- Act -----
            useCase(bookId = bookId)

            // ----- Assert -----
            coVerify {
                exploreRepository.undoContinueSeriesBookDismissal(bookId = bookId)
            }
        }

        @Test
        fun `returns failure when repository throws`() = runTest {
            // ----- Arrange -----
            val expectedError = RuntimeException("db error")

            coEvery {
                exploreRepository.undoContinueSeriesBookDismissal(any())
            } throws expectedError

            // ----- Act -----
            val result = useCase(bookId = 1)

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
        }
    }
}
