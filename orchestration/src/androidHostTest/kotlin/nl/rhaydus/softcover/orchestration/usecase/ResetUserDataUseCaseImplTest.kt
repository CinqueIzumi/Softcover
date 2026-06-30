package nl.rhaydus.softcover.orchestration.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
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
        fun `returns success when all repository calls succeed`() = runTest {
            // ----- Arrange -----
            // (all deps are relaxed, so all calls succeed by default)

            // ----- Act -----
            val result = useCase()

            // ----- Assert -----
            result.isSuccess shouldBe true
        }

        @Test
        fun `clears api key by calling updateApiKey with an empty string`() = runTest {
            // ----- Arrange -----
            // (deps are relaxed)

            // ----- Act -----
            useCase()

            // ----- Assert -----
            coVerify {
                settingsRepository.updateApiKey(key = "")
            }
        }

        @Test
        fun `removes all books via books repository`() = runTest {
            // ----- Arrange -----
            // (deps are relaxed)

            // ----- Act -----
            useCase()

            // ----- Assert -----
            coVerify {
                booksRepository.removeAllBooks()
            }
        }

        @Test
        fun `clears profile cache via profile repository`() = runTest {
            // ----- Arrange -----
            // (deps are relaxed)

            // ----- Act -----
            useCase()

            // ----- Assert -----
            coVerify {
                profileRepository.clearProfileCache()
            }
        }

        @Test
        fun `resets all settings via settings repository`() = runTest {
            // ----- Arrange -----
            // (deps are relaxed)

            // ----- Act -----
            useCase()

            // ----- Assert -----
            coVerify {
                settingsRepository.resetAllSettings()
            }
        }

        @Test
        fun `clears network cache via network cache cleaner`() = runTest {
            // ----- Arrange -----
            // (deps are relaxed)

            // ----- Act -----
            useCase()

            // ----- Assert -----
            coVerify {
                networkCacheCleaner.clearAll()
            }
        }

        @Test
        fun `calls all steps in declaration order`() = runTest {
            // ----- Arrange -----
            // (deps are relaxed)

            // ----- Act -----
            useCase()

            // ----- Assert -----
            coVerifyOrder {
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
        fun `returns failure when clearProfileCache throws`() = runTest {
            // ----- Arrange -----
            val expectedError = RuntimeException("cache clear error")

            coEvery {
                profileRepository.clearProfileCache()
            } throws expectedError

            // ----- Act -----
            val result = useCase()

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
        }

        @Test
        fun `returns failure when resetAllSettings throws`() = runTest {
            // ----- Arrange -----
            val expectedError = RuntimeException("settings reset error")

            coEvery {
                settingsRepository.resetAllSettings()
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
