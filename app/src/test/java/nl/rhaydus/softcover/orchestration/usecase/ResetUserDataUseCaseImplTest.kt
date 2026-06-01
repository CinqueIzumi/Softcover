package nl.rhaydus.softcover.orchestration.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository
import nl.rhaydus.softcover.feature.profile.domain.repository.ProfileRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ResetUserDataUseCaseImplTest {

    private lateinit var settingsRepository: SettingsRepository
    private lateinit var booksRepository: BooksRepository
    private lateinit var profileRepository: ProfileRepository
    private lateinit var useCase: ResetUserDataUseCaseImpl

    @BeforeEach
    fun setUp() {
        settingsRepository = mockk(relaxed = true)
        booksRepository = mockk(relaxed = true)
        profileRepository = mockk(relaxed = true)
        useCase = ResetUserDataUseCaseImpl(
            settingsRepository = settingsRepository,
            booksRepository = booksRepository,
            profileRepository = profileRepository,
        )
    }

    @Nested
    inner class Invoke {

        @Test
        fun `returns success when all repository calls succeed`() = runTest {
            // ----- Arrange -----
            // (repositories are relaxed, so all calls succeed by default)

            // ----- Act -----
            val result = useCase()

            // ----- Assert -----
            result.isSuccess shouldBe true
        }

        @Test
        fun `clears api key by calling updateApiKey with an empty string`() = runTest {
            // ----- Arrange -----
            // (repositories are relaxed)

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
            // (repositories are relaxed)

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
            // (repositories are relaxed)

            // ----- Act -----
            useCase()

            // ----- Assert -----
            coVerify {
                profileRepository.clearProfileCache()
            }
        }

        @Test
        fun `clears profile cache after removing all books and before resetting library visibility`() = runTest {
            // ----- Arrange -----
            // (repositories are relaxed)

            // ----- Act -----
            useCase()

            // ----- Assert -----
            coVerifyOrder {
                booksRepository.removeAllBooks()
                profileRepository.clearProfileCache()
                settingsRepository.resetLibraryVisibilityPreferences()
            }
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
        fun `resets userId to -1 via settings repository`() = runTest {
            // ----- Arrange -----
            // (repositories are relaxed)

            // ----- Act -----
            useCase()

            // ----- Assert -----
            coVerify {
                settingsRepository.updateUserId(id = -1)
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
        fun `returns failure when updateUserId throws`() = runTest {
            // ----- Arrange -----
            val expectedError = RuntimeException("userId error")

            coEvery {
                settingsRepository.updateUserId(id = -1)
            } throws expectedError

            // ----- Act -----
            val result = useCase()

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
        }

        @Test
        fun `resets library visibility preferences via settings repository`() = runTest {
            // ----- Arrange -----
            // (repositories are relaxed)

            // ----- Act -----
            useCase()

            // ----- Assert -----
            coVerify {
                settingsRepository.resetLibraryVisibilityPreferences()
            }
        }

        @Test
        fun `returns failure when resetLibraryVisibilityPreferences throws`() = runTest {
            // ----- Arrange -----
            val expectedError = RuntimeException("prefs reset error")

            coEvery {
                settingsRepository.resetLibraryVisibilityPreferences()
            } throws expectedError

            // ----- Act -----
            val result = useCase()

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
        }
    }
}
