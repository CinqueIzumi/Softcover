package nl.rhaydus.softcover.core.profile.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.profile.domain.ProfileRefreshGate
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileSnapshot
import nl.rhaydus.softcover.core.profile.domain.repository.ProfileRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class RefreshUserProfileStatsUseCaseTest {
    private lateinit var profileRepository: ProfileRepository

    // A real gate (not mocked): it has no external dependencies, and using the real
    // check-fetch-mark logic is what lets the GateBehavior tests below observe it actually
    // skipping or retrying the network fetch across successive useCase() calls.
    private lateinit var statsRefreshGate: ProfileRefreshGate
    private lateinit var useCase: RefreshUserProfileStatsUseCase

    @BeforeEach
    fun setUp() {
        profileRepository = mockk()
        statsRefreshGate = ProfileRefreshGate()
        useCase = RefreshUserProfileStatsUseCase(
            profileRepository = profileRepository,
            statsRefreshGate = statsRefreshGate,
        )
    }

    private fun snapshot(): UserProfileSnapshot = UserProfileSnapshot(
        profileImageUrl = "https://example.com/avatar.png",
        name = "Jane Doe",
        username = "cinque",
        bio = "Avid reader",
        booksRead = 42,
        totalPagesRead = 12000,
        averageRating = 4.2,
        trackedYears = 7,
    )

    @Nested
    inner class Invoke {
        @Test
        fun `returns success and caches the fetched snapshot`() = runTest {
            // ----- Arrange -----
            val fetchedSnapshot = snapshot()

            coEvery {
                profileRepository.fetchUserProfileSnapshot()
            } returns fetchedSnapshot

            coEvery {
                profileRepository.cacheUserProfileStats(snapshot = fetchedSnapshot)
            } returns Unit

            // ----- Act -----
            val result = useCase()

            // ----- Assert -----
            result.isSuccess shouldBe true
            coVerify(exactly = 1) { profileRepository.cacheUserProfileStats(snapshot = fetchedSnapshot) }
        }

        @Test
        fun `returns failure when fetchUserProfileSnapshot throws and never caches`() = runTest {
            // ----- Arrange -----
            val exception = RuntimeException("network error")

            coEvery {
                profileRepository.fetchUserProfileSnapshot()
            } throws exception

            // ----- Act -----
            val result = useCase()

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe exception
            coVerify(exactly = 0) { profileRepository.cacheUserProfileStats(snapshot = any()) }
        }
    }

    @Nested
    inner class GateBehavior {
        private fun arrangeSuccessfulFetch() {
            coEvery {
                profileRepository.fetchUserProfileSnapshot()
            } returns snapshot()
            coEvery {
                profileRepository.cacheUserProfileStats(snapshot = any())
            } returns Unit
        }

        @Test
        fun `a second invoke within the same session skips the network fetch entirely`() = runTest {
            // ----- Arrange -----
            arrangeSuccessfulFetch()

            // ----- Act -----
            useCase()
            useCase()

            // ----- Assert -----
            coVerify(exactly = 1) { profileRepository.fetchUserProfileSnapshot() }
        }

        @Test
        fun `a failed fetch leaves the gate open so the next invoke retries`() = runTest {
            // ----- Arrange -----
            var fetchCallCount = 0

            coEvery {
                profileRepository.fetchUserProfileSnapshot()
            } coAnswers {
                fetchCallCount++
                if (fetchCallCount == 1) throw RuntimeException("network error") else snapshot()
            }
            coEvery {
                profileRepository.cacheUserProfileStats(snapshot = any())
            } returns Unit

            // ----- Act -----
            val firstResult = useCase()
            val secondResult = useCase()

            // ----- Assert -----
            firstResult.isFailure shouldBe true
            secondResult.isSuccess shouldBe true
            coVerify(exactly = 2) { profileRepository.fetchUserProfileSnapshot() }
        }
    }
}
