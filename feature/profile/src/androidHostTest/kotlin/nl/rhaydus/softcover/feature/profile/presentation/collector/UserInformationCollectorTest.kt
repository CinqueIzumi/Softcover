package nl.rhaydus.softcover.feature.profile.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileData
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveUserProfileDataUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.RefreshUserProfileDataUseCase
import nl.rhaydus.softcover.feature.profile.presentation.event.ProfileEvent
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.ProfileDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.LocalProfileVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.ProfileUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class UserInformationCollectorTest {
    private lateinit var observeUserProfileDataUseCase: ObserveUserProfileDataUseCase
    private lateinit var refreshUserProfileDataUseCase: RefreshUserProfileDataUseCase
    private lateinit var stateFlow: MutableStateFlow<ProfileUiState>
    private lateinit var eventChannel: Channel<ProfileEvent>
    private lateinit var scope: ActionScope<ProfileUiState, ProfileEvent, LocalProfileVariables>
    private lateinit var profileDataFlow: MutableSharedFlow<UserProfileData?>

    @BeforeEach
    fun setUp() {
        profileDataFlow = MutableSharedFlow()
        observeUserProfileDataUseCase = mockk()
        refreshUserProfileDataUseCase = mockk()
        stateFlow = MutableStateFlow(ProfileUiState())
        eventChannel = Channel(Channel.BUFFERED)
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LocalProfileVariables()),
            eventChannel = eventChannel,
        )

        every {
            observeUserProfileDataUseCase()
        } returns profileDataFlow
    }

    private fun stubDependencies(testScope: kotlinx.coroutines.test.TestScope): ProfileDependencies {
        val dispatcher = UnconfinedTestDispatcher(testScope.testScheduler)
        return mockk<ProfileDependencies>(relaxed = true).also { mock ->
            every {
                mock.observeUserProfileDataUseCase
            } returns observeUserProfileDataUseCase

            every {
                mock.refreshUserProfileDataUseCase
            } returns refreshUserProfileDataUseCase

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

    private fun buildProfileData(): UserProfileData = UserProfileData(
        profileImageUrl = "https://example.com/avatar.png",
        name = "Jane Doe",
        username = "cinque",
        bio = "Avid reader",
        booksRead = 42,
        totalPagesRead = 12000,
        averageRating = 4.2,
        readingStreak = 7,
    )

    @Nested
    inner class OnLaunch {
        @Test
        fun `invokes refreshUserProfileDataUseCase on launch`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            coEvery { refreshUserProfileDataUseCase() } returns Result.success(Unit)
            val dependencies = stubDependencies(this)
            val initializer = UserInformationCollector()
            val job = launch { initializer.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            // (refresh is triggered immediately on launch via dependencies.launch { ... })

            // ----- Assert -----
            coVerify(exactly = 1) { refreshUserProfileDataUseCase() }
            job.cancel()
        }

        @Test
        fun `updates state to userProfileData and isLoading false when observe emits non-null`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            coEvery { refreshUserProfileDataUseCase() } returns Result.success(Unit)
            val profileData = buildProfileData()
            val dependencies = stubDependencies(this)
            val initializer = UserInformationCollector()
            val job = launch { initializer.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            profileDataFlow.emit(profileData)

            // ----- Assert -----
            stateFlow.value.userProfileData shouldBe profileData
            stateFlow.value.isLoading shouldBe false
            job.cancel()
        }

        @Test
        fun `does not update state when observe emits null`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            coEvery { refreshUserProfileDataUseCase() } returns Result.success(Unit)
            val dependencies = stubDependencies(this)
            val initializer = UserInformationCollector()
            val job = launch { initializer.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            profileDataFlow.emit(null)

            // ----- Assert -----
            stateFlow.value.userProfileData shouldBe null
            stateFlow.value.isLoading shouldBe true
            job.cancel()
        }

        @Test
        fun `swallows refresh failure and still collects from observe`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            coEvery {
                refreshUserProfileDataUseCase()
            } returns Result.failure(RuntimeException("refresh error"))
            val profileData = buildProfileData()
            val dependencies = stubDependencies(this)
            val initializer = UserInformationCollector()
            val job = launch { initializer.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            profileDataFlow.emit(profileData)

            // ----- Assert -----
            stateFlow.value.userProfileData shouldBe profileData
            stateFlow.value.isLoading shouldBe false
            job.cancel()
        }
    }
}
