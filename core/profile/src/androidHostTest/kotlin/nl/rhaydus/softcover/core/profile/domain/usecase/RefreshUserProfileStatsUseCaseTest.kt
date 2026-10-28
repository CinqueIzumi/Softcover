package nl.rhaydus.softcover.core.profile.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.book.domain.usecase.HasSyncedReadBooksThisSessionUseCase
import nl.rhaydus.softcover.core.domain.account.RefreshLibraryUseCase
import nl.rhaydus.softcover.core.domain.model.RefreshScope
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
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
    private lateinit var hasSyncedReadBooksThisSessionUseCase: HasSyncedReadBooksThisSessionUseCase
    private lateinit var refreshLibraryUseCase: RefreshLibraryUseCase
    private lateinit var useCase: RefreshUserProfileStatsUseCase

    @BeforeEach
    fun setUp() {
        profileRepository = mockk()
        statsRefreshGate = ProfileRefreshGate()
        hasSyncedReadBooksThisSessionUseCase = mockk()
        refreshLibraryUseCase = mockk()
        every {
            hasSyncedReadBooksThisSessionUseCase()
        } returns true
        useCase = RefreshUserProfileStatsUseCase(
            profileRepository = profileRepository,
            statsRefreshGate = statsRefreshGate,
            refreshLibraryUseCase = refreshLibraryUseCase,
            hasSyncedReadBooksThisSessionUseCase = hasSyncedReadBooksThisSessionUseCase,
        )
    }

    private fun snapshot(): UserProfileSnapshot = UserProfileSnapshot(
        profileImageUrl = "https://example.com/avatar.png",
        name = "Jane Doe",
        username = "cinque",
        bio = "Avid reader",
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
                profileRepository.cacheUserProfileIdentity(snapshot = fetchedSnapshot)
            } returns Unit

            // ----- Act -----
            val result = useCase()

            // ----- Assert -----
            result.isSuccess shouldBe true
            coVerify(exactly = 1) { profileRepository.cacheUserProfileIdentity(snapshot = fetchedSnapshot) }
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
            coVerify(exactly = 0) { profileRepository.cacheUserProfileIdentity(snapshot = any()) }
        }
    }

    @Nested
    inner class ReadBooksRefresh {
        private val readScope = RefreshScope.ByStatuses(setOf(UserBookStatus.READ))

        private fun arrangeSuccessfulIdentity() {
            coEvery {
                profileRepository.fetchUserProfileSnapshot()
            } returns snapshot()
            coEvery {
                profileRepository.cacheUserProfileIdentity(snapshot = any())
            } returns Unit
        }

        @Test
        fun `refreshes the Read books after caching the identity when this session has not synced them`() =
            runTest {
                // ----- Arrange -----
                arrangeSuccessfulIdentity()
                every {
                    hasSyncedReadBooksThisSessionUseCase()
                } returns false
                coEvery {
                    refreshLibraryUseCase(scope = readScope)
                } returns Result.success(Unit)

                // ----- Act -----
                val result = useCase()

                // ----- Assert -----
                result.isSuccess shouldBe true
                coVerifyOrder {
                    profileRepository.cacheUserProfileIdentity(snapshot = any())
                    refreshLibraryUseCase(scope = readScope)
                }
            }

        @Test
        fun `skips the Read books refresh when this session has already synced them`() = runTest {
            // ----- Arrange -----
            arrangeSuccessfulIdentity()
            every {
                hasSyncedReadBooksThisSessionUseCase()
            } returns true

            // ----- Act -----
            val result = useCase()

            // ----- Assert -----
            result.isSuccess shouldBe true
            coVerify(exactly = 0) { refreshLibraryUseCase(scope = any()) }
        }

        @Test
        fun `returns the failure when the Read books refresh fails`() = runTest {
            // ----- Arrange -----
            val exception = RuntimeException("library refresh failed")
            arrangeSuccessfulIdentity()
            every {
                hasSyncedReadBooksThisSessionUseCase()
            } returns false
            coEvery {
                refreshLibraryUseCase(scope = readScope)
            } returns Result.failure(exception)

            // ----- Act -----
            val result = useCase()

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe exception
        }

        @Test
        fun `a failed Read books refresh leaves the gate open so the next invoke retries`() = runTest {
            // ----- Arrange -----
            arrangeSuccessfulIdentity()
            every {
                hasSyncedReadBooksThisSessionUseCase()
            } returns false
            coEvery {
                refreshLibraryUseCase(scope = readScope)
            } returnsMany listOf(
                Result.failure(RuntimeException("library refresh failed")),
                Result.success(Unit),
            )

            // ----- Act -----
            val firstResult = useCase()
            val secondResult = useCase()

            // ----- Assert -----
            firstResult.isFailure shouldBe true
            secondResult.isSuccess shouldBe true
            coVerify(exactly = 2) { refreshLibraryUseCase(scope = readScope) }
        }
    }

    @Nested
    inner class GateBehavior {
        private fun arrangeSuccessfulFetch() {
            coEvery {
                profileRepository.fetchUserProfileSnapshot()
            } returns snapshot()
            coEvery {
                profileRepository.cacheUserProfileIdentity(snapshot = any())
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
                profileRepository.cacheUserProfileIdentity(snapshot = any())
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
