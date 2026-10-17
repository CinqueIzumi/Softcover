package nl.rhaydus.softcover.orchestration.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.account.RefreshLibraryUseCase
import nl.rhaydus.softcover.core.domain.model.RefreshScope
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class RefreshLibraryOnStartUseCaseTest {
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var refreshLibraryUseCase: RefreshLibraryUseCase
    private lateinit var useCase: RefreshLibraryOnStartUseCase

    private val activeScope = RefreshScope.ByStatuses(
        statuses = setOf(UserBookStatus.CURRENTLY_READING, UserBookStatus.PAUSED),
    )

    @BeforeEach
    fun setUp() {
        settingsRepository = mockk(relaxed = true)
        refreshLibraryUseCase = mockk()

        useCase = RefreshLibraryOnStartUseCase(
            settingsRepository = settingsRepository,
            refreshLibraryUseCase = refreshLibraryUseCase,
        )
    }

    @Nested
    inner class Invoke {
        @Test
        fun `refreshes every status and marks the flag when the backfill has not run`() = runTest {
            // ----- Arrange -----
            every {
                settingsRepository.readThroughsBackfilled
            } returns flowOf(false)
            coEvery {
                refreshLibraryUseCase(scope = any())
            } returns Result.success(Unit)

            // ----- Act -----
            val result = useCase()

            // ----- Assert -----
            result.isSuccess shouldBe true
            coVerify(exactly = 1) {
                refreshLibraryUseCase(
                    scope = RefreshScope.ByStatuses(statuses = UserBookStatus.entries.toSet()),
                )
            }
            coVerify(exactly = 1) { settingsRepository.markReadThroughsBackfilled() }
        }

        @Test
        fun `does not mark the flag and returns the failure when the backfill refresh fails`() = runTest {
            // ----- Arrange -----
            val expectedError = RuntimeException("refresh error")
            every {
                settingsRepository.readThroughsBackfilled
            } returns flowOf(false)
            coEvery {
                refreshLibraryUseCase(scope = any())
            } returns Result.failure(expectedError)

            // ----- Act -----
            val result = useCase()

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
            coVerify(exactly = 0) { settingsRepository.markReadThroughsBackfilled() }
        }

        @Test
        fun `refreshes only currently reading and paused without marking when already backfilled`() = runTest {
            // ----- Arrange -----
            every {
                settingsRepository.readThroughsBackfilled
            } returns flowOf(true)
            coEvery {
                refreshLibraryUseCase(scope = any())
            } returns Result.success(Unit)

            // ----- Act -----
            val result = useCase()

            // ----- Assert -----
            result.isSuccess shouldBe true
            coVerify(exactly = 1) { refreshLibraryUseCase(scope = activeScope) }
            coVerify(exactly = 0) { settingsRepository.markReadThroughsBackfilled() }
        }

        @Test
        fun `returns the failure when the active-scope refresh fails`() = runTest {
            // ----- Arrange -----
            val expectedError = RuntimeException("refresh error")
            every {
                settingsRepository.readThroughsBackfilled
            } returns flowOf(true)
            coEvery {
                refreshLibraryUseCase(scope = any())
            } returns Result.failure(expectedError)

            // ----- Act -----
            val result = useCase()

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
        }

        @Test
        fun `treats an empty flag read as backfilled and uses the active scope`() = runTest {
            // ----- Arrange -----
            every {
                settingsRepository.readThroughsBackfilled
            } returns emptyFlow()
            coEvery {
                refreshLibraryUseCase(scope = any())
            } returns Result.success(Unit)

            // ----- Act -----
            val result = useCase()

            // ----- Assert -----
            result.isSuccess shouldBe true
            coVerify(exactly = 1) { refreshLibraryUseCase(scope = activeScope) }
            coVerify(exactly = 0) { settingsRepository.markReadThroughsBackfilled() }
        }
    }
}
