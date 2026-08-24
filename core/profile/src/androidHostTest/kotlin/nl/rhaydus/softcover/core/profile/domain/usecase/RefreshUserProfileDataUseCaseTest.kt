package nl.rhaydus.softcover.core.profile.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class RefreshUserProfileDataUseCaseTest {
    private lateinit var refreshReadingActivityUseCase: RefreshReadingActivityUseCase
    private lateinit var refreshUserProfileStatsUseCase: RefreshUserProfileStatsUseCase
    private lateinit var useCase: RefreshUserProfileDataUseCase

    @BeforeEach
    fun setUp() {
        refreshReadingActivityUseCase = mockk()
        refreshUserProfileStatsUseCase = mockk()
        useCase = RefreshUserProfileDataUseCase(
            refreshReadingActivityUseCase = refreshReadingActivityUseCase,
            refreshUserProfileStatsUseCase = refreshUserProfileStatsUseCase,
        )
    }

    @Nested
    inner class Invoke {
        @Test
        fun `returns success and calls both halves when both succeed`() = runTest {
            // ----- Arrange -----
            coEvery {
                refreshReadingActivityUseCase()
            } returns Result.success(Unit)
            coEvery {
                refreshUserProfileStatsUseCase()
            } returns Result.success(Unit)

            // ----- Act -----
            val result = useCase()

            // ----- Assert -----
            result.isSuccess shouldBe true
            coVerify(exactly = 1) { refreshReadingActivityUseCase() }
            coVerify(exactly = 1) { refreshUserProfileStatsUseCase() }
        }

        @Test
        fun `calls the activity half before the stats half`() = runTest {
            // ----- Arrange -----
            coEvery {
                refreshReadingActivityUseCase()
            } returns Result.success(Unit)
            coEvery {
                refreshUserProfileStatsUseCase()
            } returns Result.success(Unit)

            // ----- Act -----
            useCase()

            // ----- Assert -----
            coVerifyOrder {
                refreshReadingActivityUseCase()
                refreshUserProfileStatsUseCase()
            }
        }

        @Test
        fun `still calls the stats half and reports the activity failure when the activity half fails`() = runTest {
            // ----- Arrange -----
            val exception = RuntimeException("activity refresh failed")

            coEvery {
                refreshReadingActivityUseCase()
            } returns Result.failure(exception)
            coEvery {
                refreshUserProfileStatsUseCase()
            } returns Result.success(Unit)

            // ----- Act -----
            val result = useCase()

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe exception
            coVerify(exactly = 1) { refreshReadingActivityUseCase() }
            coVerify(exactly = 1) { refreshUserProfileStatsUseCase() }
        }

        @Test
        fun `still calls both halves and reports the stats failure when only the stats half fails`() = runTest {
            // ----- Arrange -----
            val exception = RuntimeException("stats refresh failed")

            coEvery {
                refreshReadingActivityUseCase()
            } returns Result.success(Unit)
            coEvery {
                refreshUserProfileStatsUseCase()
            } returns Result.failure(exception)

            // ----- Act -----
            val result = useCase()

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe exception
            coVerify(exactly = 1) { refreshReadingActivityUseCase() }
            coVerify(exactly = 1) { refreshUserProfileStatsUseCase() }
        }

        @Test
        fun `reports the activity failure — not the stats failure — when both halves fail`() = runTest {
            // ----- Arrange -----
            val activityException = RuntimeException("activity refresh failed")
            val statsException = RuntimeException("stats refresh failed")

            coEvery {
                refreshReadingActivityUseCase()
            } returns Result.failure(activityException)
            coEvery {
                refreshUserProfileStatsUseCase()
            } returns Result.failure(statsException)

            // ----- Act -----
            val result = useCase()

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe activityException
            coVerify(exactly = 1) { refreshReadingActivityUseCase() }
            coVerify(exactly = 1) { refreshUserProfileStatsUseCase() }
        }
    }
}
