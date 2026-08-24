package nl.rhaydus.softcover.feature.explore.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.feature.explore.domain.model.MoodTag
import nl.rhaydus.softcover.feature.explore.domain.repository.ExploreRepository

class GetMoodTagsUseCaseTest {
    private lateinit var exploreRepository: ExploreRepository
    private lateinit var useCase: GetMoodTagsUseCase

    @BeforeEach
    fun setUp() {
        exploreRepository = mockk()
        useCase = GetMoodTagsUseCase(
            exploreRepository = exploreRepository,
        )
    }

    @Nested
    inner class Invoke {
        @Test
        fun `returns the mood tags from the repository`() = runTest {
            // ----- Arrange -----
            val tags = listOf(
                MoodTag(
                    id = 1,
                    label = "Cozy",
                    slug = "cozy",
                    bookCount = 10,
                ),
            )

            coEvery {
                exploreRepository.fetchMoodTags()
            } returns tags

            // ----- Act -----
            val result = useCase()

            // ----- Assert -----
            result shouldBe Result.success(tags)
        }

        @Test
        fun `returns success with an empty list when no tags exist`() = runTest {
            // ----- Arrange -----
            coEvery {
                exploreRepository.fetchMoodTags()
            } returns emptyList()

            // ----- Act -----
            val result = useCase()

            // ----- Assert -----
            result shouldBe Result.success(emptyList())
        }

        @Test
        fun `returns failure when the repository throws`() = runTest {
            // ----- Arrange -----
            val error = RuntimeException("network error")

            coEvery {
                exploreRepository.fetchMoodTags()
            } throws error

            // ----- Act -----
            val result = useCase()

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe error
        }
    }
}
