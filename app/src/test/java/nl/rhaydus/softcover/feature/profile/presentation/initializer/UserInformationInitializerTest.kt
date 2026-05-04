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
import nl.rhaydus.softcover.feature.profile.domain.model.UserProfileData
import nl.rhaydus.softcover.feature.profile.domain.usecase.GetUserProfileDataUseCase
import nl.rhaydus.softcover.feature.profile.presentation.event.ProfileEvent
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.ProfileDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.LocalProfileVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.ProfileUiState
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class UserInformationInitializerTest {

    private lateinit var getUserProfileDataUseCase: GetUserProfileDataUseCase
    private lateinit var stateFlow: MutableStateFlow<ProfileUiState>
    private lateinit var eventChannel: Channel<ProfileEvent>
    private lateinit var scope: ActionScope<ProfileUiState, ProfileEvent, LocalProfileVariables>

    @BeforeEach
    fun setUp() {
        getUserProfileDataUseCase = mockk()
        stateFlow = MutableStateFlow(ProfileUiState())
        eventChannel = Channel(Channel.BUFFERED)
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LocalProfileVariables()),
            eventChannel = eventChannel,
        )
    }

    private fun stubDependencies(testScope: kotlinx.coroutines.test.TestScope): ProfileDependencies {
        val dispatcher = UnconfinedTestDispatcher(testScope.testScheduler)
        return mockk<ProfileDependencies>(relaxed = true).also { mock ->
            every {
                mock.getUserProfileDataUseCase
            } returns getUserProfileDataUseCase

            every {
                mock.coroutineScope
            } returns testScope

            every {
                mock.mainDispatcher
            } returns dispatcher

            every {
                mock.launch(any())
            } answers { callOriginal() }
        }
    }

    private fun stubUserProfileData() = UserProfileData(
        profileImageUrl = "https://example.com/avatar.png",
        name = "Jane Doe",
        bio = "Avid reader",
        booksRead = 42,
        totalPagesRead = 12000,
        averageRating = 4.2,
        readingStreak = 7,
    )

    @Nested
    inner class OnLaunch {

        @Test
        fun `sets userProfileData on state and isLoading false when use case succeeds`() = runTest {
            // ----- Arrange -----
            val profileData = stubUserProfileData()
            val dependencies = stubDependencies(this)

            coEvery {
                getUserProfileDataUseCase()
            } returns Result.success(profileData)

            val initializer = UserInformationInitializer()

            // ----- Act -----
            initializer.onLaunch(scope = scope, dependencies = dependencies)

            // ----- Assert -----
            stateFlow.value.userProfileData shouldBe profileData
            stateFlow.value.isLoading shouldBe false
        }

        @Test
        fun `leaves userProfileData null and sets isLoading false when use case fails`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)

            coEvery {
                getUserProfileDataUseCase()
            } returns Result.failure(RuntimeException("fetch error"))

            val initializer = UserInformationInitializer()

            // ----- Act -----
            initializer.onLaunch(scope = scope, dependencies = dependencies)

            // ----- Assert -----
            stateFlow.value.userProfileData shouldBe null
            stateFlow.value.isLoading shouldBe false
        }
    }
}
