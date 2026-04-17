package nl.rhaydus.softcover.feature.search.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.feature.search.domain.repository.SearchRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class RemoveAllSearchQueriesUseCaseTest {

    private lateinit var searchRepository: SearchRepository
    private lateinit var useCase: RemoveAllSearchQueriesUseCase

    @BeforeEach
    fun setUp() {
        searchRepository = mockk(relaxed = true)
        useCase = RemoveAllSearchQueriesUseCase(searchRepository = searchRepository)
    }

    @Nested
    inner class Invoke {

        @Test
        fun `delegates to repository removeAllSearchQueries`() = runTest {
            // ----- Arrange -----
            // (searchRepository is relaxed — no additional setup needed)

            // ----- Act -----
            useCase()

            // ----- Assert -----
            coVerify {
                searchRepository.removeAllSearchQueries()
            }
        }

        @Test
        fun `returns success when repository succeeds`() = runTest {
            // ----- Arrange -----
            // (searchRepository is relaxed — removeAllSearchQueries returns Unit by default)

            // ----- Act -----
            val result = useCase()

            // ----- Assert -----
            result.isSuccess shouldBe true
        }

        @Test
        fun `returns failure when repository throws`() = runTest {
            // ----- Arrange -----
            val expectedError = RuntimeException("datastore error")

            coEvery {
                searchRepository.removeAllSearchQueries()
            } throws expectedError

            // ----- Act -----
            val result = useCase()

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
        }
    }
}
