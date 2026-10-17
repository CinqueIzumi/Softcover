package nl.rhaydus.softcover.orchestration.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.domain.cache.NetworkCacheCleaner
import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository
import nl.rhaydus.softcover.core.profile.domain.repository.ProfileRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ResetUserDataUseCaseImplTest {
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var booksRepository: BooksRepository
    private lateinit var profileRepository: ProfileRepository
    private lateinit var networkCacheCleaner: NetworkCacheCleaner
    private lateinit var useCase: ResetUserDataUseCaseImpl

    @BeforeEach
    fun setUp() {
        settingsRepository = mockk(relaxed = true)
        booksRepository = mockk(relaxed = true)
        profileRepository = mockk(relaxed = true)
        networkCacheCleaner = mockk(relaxed = true)
        useCase = ResetUserDataUseCaseImpl(
            settingsRepository = settingsRepository,
            booksRepository = booksRepository,
            profileRepository = profileRepository,
            networkCacheCleaner = networkCacheCleaner,
        )
    }

    @Nested
    inner class Invoke {
        @Test
        fun `returns success and wipes the token, books, profile, settings and network cache`() = runTest {
            // ----- Arrange -----
            // (all deps are relaxed, so all calls succeed by default)

            // ----- Act -----
            val result = useCase()

            // ----- Assert -----
            result.isSuccess shouldBe true
            coVerify {
                settingsRepository.updateApiKey(key = "")
                booksRepository.removeAllBooks()
                profileRepository.clearProfileCache()
                settingsRepository.resetAllSettings()
                networkCacheCleaner.clearAll()
            }
        }

        @Test
        fun `returns failure when updateApiKey throws`() = runTest {
            // ----- Arrange -----
            val expectedError = RuntimeException("storage error")

            coEvery {
                settingsRepository.updateApiKey(key = "")
            } throws expectedError

            // ----- Act -----
            val result = useCase()

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
        }

        @Test
        fun `returns failure when removeAllBooks throws`() = runTest {
            // ----- Arrange -----
            val expectedError = RuntimeException("db error")

            coEvery {
                booksRepository.removeAllBooks()
            } throws expectedError

            // ----- Act -----
            val result = useCase()

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
        }

        @Test
        fun `returns failure when networkCacheCleaner clearAll throws`() = runTest {
            // ----- Arrange -----
            val expectedError = RuntimeException("network cache error")

            coEvery {
                networkCacheCleaner.clearAll()
            } throws expectedError

            // ----- Act -----
            val result = useCase()

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
        }
    }
}
