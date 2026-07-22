package nl.rhaydus.softcover.feature.explore.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.feature.explore.domain.repository.ExploreRepository

class ClearSearchResultsUseCaseTest {
    private lateinit var searchRepository: ExploreRepository
    private lateinit var useCase: ClearSearchResultsUseCase

    @BeforeEach
    fun setUp() {
        searchRepository = mockk(relaxed = true)
        useCase = ClearSearchResultsUseCase(searchRepository = searchRepository)
    }

    @Nested
    inner class Invoke {
        @Test
        fun `delegates to repository clearSearchResults`() = runTest {
            // ----- Arrange -----
            // (searchRepository is relaxed — no additional setup needed)

            // ----- Act -----
            useCase()

            // ----- Assert -----
            coVerify {
                searchRepository.clearSearchResults()
            }
        }

        @Test
        fun `returns success when repository succeeds`() = runTest {
            // ----- Arrange -----
            // (searchRepository is relaxed — clearSearchResults returns Unit by default)

            // ----- Act -----
            val result = useCase()

            // ----- Assert -----
            result.isSuccess shouldBe true
        }

        @Test
        fun `returns failure when repository throws`() = runTest {
            // ----- Arrange -----
            val expectedError = RuntimeException("network error")

            coEvery {
                searchRepository.clearSearchResults()
            } throws expectedError

            // ----- Act -----
            val result = useCase()

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
        }
    }
}
