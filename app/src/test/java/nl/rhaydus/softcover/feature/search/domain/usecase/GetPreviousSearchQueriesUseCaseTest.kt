package nl.rhaydus.softcover.feature.search.domain.usecase

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.feature.search.domain.repository.SearchRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class GetPreviousSearchQueriesUseCaseTest {

    private lateinit var searchRepository: SearchRepository
    private lateinit var useCase: GetPreviousSearchQueriesUseCase

    @BeforeEach
    fun setUp() {
        searchRepository = mockk()
        useCase = GetPreviousSearchQueriesUseCase(searchRepository = searchRepository)
    }

    @Nested
    inner class Invoke {

        @Test
        fun `returns flow from repository previousSearchQueries`() = runTest {
            // ----- Arrange -----
            val expectedQueries = listOf("kotlin", "coroutines")

            every {
                searchRepository.previousSearchQueries
            } returns flowOf(expectedQueries)

            // ----- Act -----
            val result = useCase()

            // ----- Assert -----
            result.test {
                awaitItem() shouldBe expectedQueries
                awaitComplete()
            }
        }

        @Test
        fun `returns empty flow when repository emits empty list`() = runTest {
            // ----- Arrange -----
            every {
                searchRepository.previousSearchQueries
            } returns flowOf(emptyList())

            // ----- Act -----
            val result = useCase()

            // ----- Assert -----
            result.test {
                awaitItem() shouldBe emptyList()
                awaitComplete()
            }
        }

        @Test
        fun `returns immediately completing flow when repository flow is empty`() = runTest {
            // ----- Arrange -----
            every {
                searchRepository.previousSearchQueries
            } returns emptyFlow()

            // ----- Act -----
            val result = useCase()

            // ----- Assert -----
            result.test {
                awaitComplete()
            }
        }
    }
}
