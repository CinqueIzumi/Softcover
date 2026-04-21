package nl.rhaydus.softcover.feature.settings.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.feature.books.domain.repository.BooksRepository
import nl.rhaydus.softcover.feature.settings.domain.repository.SettingsRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ResetUserDataUseCaseTest {

    private lateinit var settingsRepository: SettingsRepository
    private lateinit var booksRepository: BooksRepository
    private lateinit var useCase: ResetUserDataUseCase

    @BeforeEach
    fun setUp() {
        settingsRepository = mockk(relaxed = true)
        booksRepository = mockk(relaxed = true)
        useCase = ResetUserDataUseCase(
            settingsRepository = settingsRepository,
            booksRepository = booksRepository,
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
