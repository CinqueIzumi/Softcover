package nl.rhaydus.softcover.feature.search.domain.usecase

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.search.domain.repository.SearchRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class GetQueriedBooksUseCaseTest {

    private lateinit var searchRepository: SearchRepository
    private lateinit var useCase: GetQueriedBooksUseCase

    @BeforeEach
    fun setUp() {
        searchRepository = mockk()
        useCase = GetQueriedBooksUseCase(searchRepository = searchRepository)
    }

    private fun stubBook(): Book = mockk()

    @Nested
    inner class Invoke {

        @Test
        fun `returns flow from repository queriedBooks`() = runTest {
            // ----- Arrange -----
            val expectedBooks = listOf(stubBook(), stubBook())

            every {
                searchRepository.queriedBooks
            } returns flowOf(expectedBooks)

            // ----- Act -----
            val result = useCase()

            // ----- Assert -----
            result.test {
                awaitItem() shouldBe expectedBooks
                awaitComplete()
            }
        }

        @Test
        fun `returns flow emitting empty list when repository has no queried books`() = runTest {
            // ----- Arrange -----
            every {
                searchRepository.queriedBooks
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
                searchRepository.queriedBooks
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
