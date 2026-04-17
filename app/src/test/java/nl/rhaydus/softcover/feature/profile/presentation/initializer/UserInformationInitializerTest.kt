package nl.rhaydus.softcover.feature.profile.presentation.initializer

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.profile.presentation.event.ProfileEvent
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.ProfileDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.LocalProfileVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.ProfileUiState
import nl.rhaydus.softcover.feature.settings.domain.model.UserProfileData
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetUserProfileDataUseCase
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class UserInformationInitializerTest {

    private lateinit var getUserProfileDataUseCase: GetUserProfileDataUseCase
    private lateinit var dependencies: ProfileDependencies
    private lateinit var stateFlow: MutableStateFlow<ProfileUiState>
    private lateinit var scope: ActionScope<ProfileUiState, ProfileEvent, LocalProfileVariables>

    @BeforeEach
    fun setUp() {
        getUserProfileDataUseCase = mockk()
        stateFlow = MutableStateFlow(ProfileUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LocalProfileVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        dependencies = mockk<ProfileDependencies>(relaxed = true).also { mock ->
            every {
                mock.getUserProfileDataUseCase
            } returns getUserProfileDataUseCase
        }
    }

    private fun stubUserProfileData(): UserProfileData = UserProfileData(
        profileImageUrl = "https://example.com/avatar.png",
        name = "Test User",
        bio = "A short bio",
        booksRead = 42,
    )

    @Nested
    inner class OnLaunch {

        @Test
        fun `sets userProfileData in state when use case succeeds`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val expected = stubUserProfileData()

            coEvery {
                getUserProfileDataUseCase()
            } returns Result.success(expected)

            val initializer = UserInformationInitializer()

            // ----- Act -----
            initializer.onLaunch(scope = scope, dependencies = dependencies)

            // ----- Assert -----
            stateFlow.value.userProfileData shouldBe expected
        }

        @Test
        fun `sets isLoading to false when use case succeeds`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            coEvery {
                getUserProfileDataUseCase()
            } returns Result.success(stubUserProfileData())

            val initializer = UserInformationInitializer()

            // ----- Act -----
            initializer.onLaunch(scope = scope, dependencies = dependencies)

            // ----- Assert -----
            stateFlow.value.isLoading shouldBe false
        }

        @Test
        fun `leaves userProfileData as null when use case fails`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            coEvery {
                getUserProfileDataUseCase()
            } returns Result.failure(RuntimeException("fetch failed"))

            val initializer = UserInformationInitializer()

            // ----- Act -----
            initializer.onLaunch(scope = scope, dependencies = dependencies)

            // ----- Assert -----
            stateFlow.value.userProfileData shouldBe null
        }

        @Test
        fun `sets isLoading to false even when use case fails`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            coEvery {
                getUserProfileDataUseCase()
            } returns Result.failure(RuntimeException("fetch failed"))

            val initializer = UserInformationInitializer()

            // ----- Act -----
            initializer.onLaunch(scope = scope, dependencies = dependencies)

            // ----- Assert -----
            stateFlow.value.isLoading shouldBe false
        }

        @Test
        fun `does not overwrite other state fields when setting userProfileData`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val expected = stubUserProfileData()
            stateFlow.value = ProfileUiState(
                userProfileData = null,
                isLoading = true,
            )

            coEvery {
                getUserProfileDataUseCase()
            } returns Result.success(expected)

            val initializer = UserInformationInitializer()

            // ----- Act -----
            initializer.onLaunch(scope = scope, dependencies = dependencies)

            // ----- Assert -----
            stateFlow.value.userProfileData shouldBe expected
            stateFlow.value.isLoading shouldBe false
        }

        @Test
        fun `sets userProfileData with correct field values`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val expected = UserProfileData(
                profileImageUrl = "https://cdn.example.com/photo.jpg",
                name = "Jane Doe",
                bio = "Avid reader",
                booksRead = 128,
            )

            coEvery {
                getUserProfileDataUseCase()
            } returns Result.success(expected)

            val initializer = UserInformationInitializer()

            // ----- Act -----
            initializer.onLaunch(scope = scope, dependencies = dependencies)

            // ----- Assert -----
            stateFlow.value.userProfileData?.name shouldBe "Jane Doe"
            stateFlow.value.userProfileData?.booksRead shouldBe 128
            stateFlow.value.userProfileData?.profileImageUrl shouldBe "https://cdn.example.com/photo.jpg"
            stateFlow.value.userProfileData?.bio shouldBe "Avid reader"
        }
    }
}
