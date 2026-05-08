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

class DismissContinueSeriesBookUseCaseTest {

    private lateinit var exploreRepository: ExploreRepository
    private lateinit var useCase: DismissContinueSeriesBookUseCase

    @BeforeEach
    fun setUp() {
        exploreRepository = mockk(relaxed = true)
        useCase = DismissContinueSeriesBookUseCase(exploreRepository = exploreRepository)
    }

    @Nested
    inner class Invoke {

        @Test
        fun `delegates to repository dismissContinueSeriesBook with correct bookId`() = runTest {
            // ----- Arrange -----
            val bookId = 42

            // ----- Act -----
            useCase(bookId = bookId)

            // ----- Assert -----
            coVerify {
                exploreRepository.dismissContinueSeriesBook(bookId = bookId)
            }
        }

        @Test
        fun `returns failure when repository throws`() = runTest {
            // ----- Arrange -----
            val expectedError = RuntimeException("db error")

            coEvery {
                exploreRepository.dismissContinueSeriesBook(any())
            } throws expectedError

            // ----- Act -----
            val result = useCase(bookId = 1)

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
        }
    }
}
