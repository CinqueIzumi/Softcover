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

class RemoveSearchQueryUseCaseTest {
    private lateinit var searchRepository: ExploreRepository
    private lateinit var useCase: RemoveSearchQueryUseCase

    @BeforeEach
    fun setUp() {
        searchRepository = mockk(relaxed = true)
        useCase = RemoveSearchQueryUseCase(searchRepository = searchRepository)
    }

    @Nested
    inner class Invoke {
        @Test
        fun `delegates to repository removeSearchQuery with the given name`() = runTest {
            // ----- Arrange -----
            val name = "dune"

            // ----- Act -----
            useCase(name = name)

            // ----- Assert -----
            coVerify {
                searchRepository.removeSearchQuery(name = name)
            }
        }

        @Test
        fun `returns success when repository succeeds`() = runTest {
            // ----- Arrange -----
            val name = "foundation"

            // ----- Act -----
            val result = useCase(name = name)

            // ----- Assert -----
            result.isSuccess shouldBe true
        }

        @Test
        fun `returns failure when repository throws`() = runTest {
            // ----- Arrange -----
            val name = "neuromancer"
            val expectedError = RuntimeException("storage error")

            coEvery {
                searchRepository.removeSearchQuery(name = name)
            } throws expectedError

            // ----- Act -----
            val result = useCase(name = name)

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
        }
    }
}
