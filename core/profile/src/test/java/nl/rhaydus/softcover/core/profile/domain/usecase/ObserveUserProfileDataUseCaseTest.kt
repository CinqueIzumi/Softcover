package nl.rhaydus.softcover.core.profile.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileData
import nl.rhaydus.softcover.core.profile.domain.repository.ProfileRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ObserveUserProfileDataUseCaseTest {
    private lateinit var profileRepository: ProfileRepository
    private lateinit var useCase: ObserveUserProfileDataUseCase

    @BeforeEach
    fun setUp() {
        profileRepository = mockk()
        useCase = ObserveUserProfileDataUseCase(profileRepository = profileRepository)
    }

    @Nested
    inner class Invoke {
        @Test
        fun `returns the flow from the repository`() = runTest {
            // ----- Arrange -----
            val profileData = UserProfileData(
                profileImageUrl = "https://example.com/avatar.png",
                name = "Jane Doe",
                username = "cinque",
                bio = "Avid reader",
                booksRead = 42,
                totalPagesRead = 12000,
                averageRating = 4.2,
                readingStreak = 7,
            )
            val repositoryFlow = MutableStateFlow<UserProfileData?>(profileData)

            every {
                profileRepository.observeUserProfileData()
            } returns repositoryFlow

            // ----- Act -----
            val result = useCase().first()

            // ----- Assert -----
            result shouldBe profileData
        }
    }
}
