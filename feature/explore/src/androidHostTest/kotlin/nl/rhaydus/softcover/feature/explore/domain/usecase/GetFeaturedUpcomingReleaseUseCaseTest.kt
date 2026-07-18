package nl.rhaydus.softcover.feature.explore.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.explore.domain.repository.ExploreRepository

class GetFeaturedUpcomingReleaseUseCaseTest {
    private lateinit var exploreRepository: ExploreRepository
    private lateinit var useCase: GetFeaturedUpcomingReleaseUseCase

    @BeforeEach
    fun setUp() {
        exploreRepository = mockk()
        useCase = GetFeaturedUpcomingReleaseUseCase(exploreRepository = exploreRepository)
    }

    private fun stubBook(id: Int = 1): Book = mockk {
        every {
            this@mockk.id
        } returns id
    }

    @Nested
    inner class Invoke {
        @Test
        fun `returns the featured book from the repository`() = runTest {
            // ----- Arrange -----
            val book = stubBook(id = 42)

            coEvery {
                exploreRepository.fetchFeaturedUpcomingRelease()
            } returns book

            // ----- Act -----
            val result = useCase()

            // ----- Assert -----
            result shouldBe Result.success(book)
        }

        @Test
        fun `returns success with null when no book qualifies`() = runTest {
            // ----- Arrange -----
            coEvery {
                exploreRepository.fetchFeaturedUpcomingRelease()
            } returns null

            // ----- Act -----
            val result = useCase()

            // ----- Assert -----
            result shouldBe Result.success(null)
        }

        @Test
        fun `returns failure when the repository throws`() = runTest {
            // ----- Arrange -----
            val error = RuntimeException("network error")

            coEvery {
                exploreRepository.fetchFeaturedUpcomingRelease()
            } throws error

            // ----- Act -----
            val result = useCase()

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe error
        }
    }
}
