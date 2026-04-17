package nl.rhaydus.softcover.feature.settings.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.feature.settings.domain.model.UserProfileData
import nl.rhaydus.softcover.feature.settings.domain.repository.SettingsRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class GetUserProfileDataUseCaseTest {

    private lateinit var settingsRepository: SettingsRepository
    private lateinit var useCase: GetUserProfileDataUseCase

    @BeforeEach
    fun setUp() {
        settingsRepository = mockk()
        useCase = GetUserProfileDataUseCase(settingsRepository = settingsRepository)
    }

    private fun stubUserProfileData(): UserProfileData = UserProfileData(
        profileImageUrl = "https://example.com/image.png",
        name = "Jane Doe",
        bio = "Book lover",
        booksRead = 42,
    )

    @Nested
    inner class Invoke {

        @Test
        fun `returns success with user profile data when repository succeeds`() = runTest {
            // ----- Arrange -----
            val expectedData = stubUserProfileData()

            coEvery {
                settingsRepository.getUserProfileData()
            } returns expectedData

            // ----- Act -----
            val result = useCase()

            // ----- Assert -----
            result.isSuccess shouldBe true
            result.getOrNull() shouldBe expectedData
        }

        @Test
        fun `returns failure when repository throws`() = runTest {
            // ----- Arrange -----
            val expectedError = RuntimeException("network error")

            coEvery {
                settingsRepository.getUserProfileData()
            } throws expectedError

            // ----- Act -----
            val result = useCase()

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
        }

        @Test
        fun `returns success with profile data having empty optional fields`() = runTest {
            // ----- Arrange -----
            val minimalData = UserProfileData(
                profileImageUrl = "",
                name = "",
                bio = "",
                booksRead = 0,
            )

            coEvery {
                settingsRepository.getUserProfileData()
            } returns minimalData

            // ----- Act -----
            val result = useCase()

            // ----- Assert -----
            result.isSuccess shouldBe true
            result.getOrNull() shouldBe minimalData
        }
    }
}
