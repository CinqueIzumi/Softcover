package nl.rhaydus.softcover.feature.explore.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.identity.domain.usecase.GetUserIdUseCase
import nl.rhaydus.softcover.feature.explore.domain.model.ExploreSortMode
import nl.rhaydus.softcover.feature.explore.domain.repository.ExploreRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SearchForNameUseCaseTest {
    private lateinit var searchRepository: ExploreRepository
    private lateinit var getUserIdUseCase: GetUserIdUseCase
    private lateinit var useCase: SearchForNameUseCase

    @BeforeEach
    fun setUp() {
        searchRepository = mockk(relaxed = true)
        getUserIdUseCase = mockk()
        useCase = SearchForNameUseCase(
            searchRepository = searchRepository,
            getUserIdUseCase = getUserIdUseCase,
        )
    }

    @Nested
    inner class Invoke {
        @Test
        fun `searches with the retrieved userId and records the query on page 1`() = runTest {
            // ----- Arrange -----
            coEvery {
                getUserIdUseCase()
            } returns Result.success(7)

            // ----- Act -----
            val result = useCase(
                name = "dune",
                sortMode = ExploreSortMode.POPULARITY,
            )

            // ----- Assert -----
            result.isSuccess shouldBe true
            coVerify {
                searchRepository.searchForName(
                    name = "dune",
                    userId = 7,
                    sortMode = ExploreSortMode.POPULARITY,
                    page = 1,
                )
                searchRepository.saveSearchQuery(name = "dune")
            }
        }

        @Test
        fun `does not record the query again on a later page`() = runTest {
            // ----- Arrange -----
            coEvery {
                getUserIdUseCase()
            } returns Result.success(7)

            // ----- Act -----
            useCase(
                name = "dune",
                sortMode = ExploreSortMode.RELEVANCE,
                page = 2,
            )

            // ----- Assert -----
            coVerify {
                searchRepository.searchForName(
                    name = "dune",
                    userId = 7,
                    sortMode = ExploreSortMode.RELEVANCE,
                    page = 2,
                )
            }
            coVerify(exactly = 0) {
                searchRepository.saveSearchQuery(name = any())
            }
        }

        @Test
        fun `returns failure without searching or recording when getUserIdUseCase fails`() = runTest {
            // ----- Arrange -----
            val expectedError = RuntimeException("no user id")

            coEvery {
                getUserIdUseCase()
            } returns Result.failure(expectedError)

            // ----- Act -----
            val result = useCase(
                name = "brave new world",
                sortMode = ExploreSortMode.RELEVANCE,
            )

            // ----- Assert -----
            result.exceptionOrNull() shouldBe expectedError
            coVerify(exactly = 0) {
                searchRepository.searchForName(
                    name = any(),
                    userId = any(),
                    sortMode = any(),
                    page = any(),
                )
                searchRepository.saveSearchQuery(name = any())
            }
        }

        @Test
        fun `returns failure and does not record the query when searchForName throws`() = runTest {
            // ----- Arrange -----
            val expectedError = RuntimeException("network error")

            coEvery {
                getUserIdUseCase()
            } returns Result.success(2)

            coEvery {
                searchRepository.searchForName(
                    name = any(),
                    userId = any(),
                    sortMode = any(),
                    page = any(),
                )
            } throws expectedError

            // ----- Act -----
            val result = useCase(
                name = "the road",
                sortMode = ExploreSortMode.RELEVANCE,
            )

            // ----- Assert -----
            result.exceptionOrNull() shouldBe expectedError
            coVerify(exactly = 0) {
                searchRepository.saveSearchQuery(name = any())
            }
        }
    }
}
