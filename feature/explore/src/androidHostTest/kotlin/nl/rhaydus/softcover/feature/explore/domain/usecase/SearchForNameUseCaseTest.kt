package nl.rhaydus.softcover.feature.explore.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.identity.domain.usecase.GetUserIdUseCase
import nl.rhaydus.softcover.feature.explore.domain.repository.ExploreRepository

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
        fun `delegates searchForName to repository with retrieved userId`() = runTest {
            // ----- Arrange -----
            val name = "dune"
            val userId = 7

            coEvery {
                getUserIdUseCase()
            } returns Result.success(userId)

            // ----- Act -----
            useCase(name = name)

            // ----- Assert -----
            coVerify {
                searchRepository.searchForName(
                    name = name,
                    userId = userId,
                )
            }
        }

        @Test
        fun `saves search query after searching`() = runTest {
            // ----- Arrange -----
            val name = "foundation"
            val userId = 3

            coEvery {
                getUserIdUseCase()
            } returns Result.success(userId)

            // ----- Act -----
            useCase(name = name)

            // ----- Assert -----
            coVerify {
                searchRepository.saveSearchQuery(name = name)
            }
        }

        @Test
        fun `returns success when both search and save succeed`() = runTest {
            // ----- Arrange -----
            val name = "1984"
            val userId = 1

            coEvery {
                getUserIdUseCase()
            } returns Result.success(userId)

            // ----- Act -----
            val result = useCase(name = name)

            // ----- Assert -----
            result.isSuccess shouldBe true
        }

        @Test
        fun `returns failure when getUserIdUseCase fails`() = runTest {
            // ----- Arrange -----
            val name = "brave new world"
            val expectedError = RuntimeException("no user id")

            coEvery {
                getUserIdUseCase()
            } returns Result.failure(expectedError)

            // ----- Act -----
            val result = useCase(name = name)

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
        }

        @Test
        fun `returns failure when searchForName throws`() = runTest {
            // ----- Arrange -----
            val name = "the road"
            val userId = 2
            val expectedError = RuntimeException("network error")

            coEvery {
                getUserIdUseCase()
            } returns Result.success(userId)

            coEvery {
                searchRepository.searchForName(
                    name = name,
                    userId = userId,
                )
            } throws expectedError

            // ----- Act -----
            val result = useCase(name = name)

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
        }

        @Test
        fun `does not save search query when getUserIdUseCase fails`() = runTest {
            // ----- Arrange -----
            val name = "americanah"

            coEvery {
                getUserIdUseCase()
            } returns Result.failure(RuntimeException("no id"))

            // ----- Act -----
            useCase(name = name)

            // ----- Assert -----
            coVerify(exactly = 0) {
                searchRepository.saveSearchQuery(name = any())
            }
        }

        @Test
        fun `does not save search query when searchForName throws`() = runTest {
            // ----- Arrange -----
            val name = "the hitchhiker"
            val userId = 5

            coEvery {
                getUserIdUseCase()
            } returns Result.success(userId)

            coEvery {
                searchRepository.searchForName(
                    name = name,
                    userId = userId,
                )
            } throws RuntimeException("apollo error")

            // ----- Act -----
            useCase(name = name)

            // ----- Assert -----
            coVerify(exactly = 0) {
                searchRepository.saveSearchQuery(name = any())
            }
        }
    }
}
