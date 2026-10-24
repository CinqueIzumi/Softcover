package nl.rhaydus.softcover.feature.profile.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileData
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveUserProfileDataUseCase
import nl.rhaydus.softcover.feature.profile.presentation.event.StatsAtlasEvent
import nl.rhaydus.softcover.feature.profile.presentation.mapper.atlasOpeningUiModel
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.StatsAtlasDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasLocalVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class AtlasProfileDataCollectorTest {
    private lateinit var observeUserProfileDataUseCase: ObserveUserProfileDataUseCase
    private lateinit var stateFlow: MutableStateFlow<StatsAtlasUiState>
    private lateinit var scope: ActionScope<StatsAtlasUiState, StatsAtlasEvent, StatsAtlasLocalVariables>
    private lateinit var profileDataFlow: MutableSharedFlow<UserProfileData?>

    @BeforeEach
    fun setUp() {
        profileDataFlow = MutableSharedFlow()
        observeUserProfileDataUseCase = mockk()
        stateFlow = MutableStateFlow(StatsAtlasUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(StatsAtlasLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        every {
            observeUserProfileDataUseCase()
        } returns profileDataFlow
    }

    private fun stubDependencies(testScope: TestScope): StatsAtlasDependencies {
        val dispatcher = UnconfinedTestDispatcher(testScope.testScheduler)
        return mockk<StatsAtlasDependencies>(relaxed = true).also { mock ->
            every {
            mock.observeUserProfileDataUseCase
        } returns observeUserProfileDataUseCase
            every {
            mock.coroutineScope
        } returns testScope
            every {
            mock.mainDispatcher
        } returns dispatcher
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
        fun `sets userProfileData, the opening tiles and isLoading false when observe emits non-null`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val profileData = buildProfileData()
                val dependencies = stubDependencies(this)
                val collector = AtlasProfileDataCollector()
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Act -----
                profileDataFlow.emit(profileData)

                // ----- Assert -----
                stateFlow.value.userProfileData shouldBe profileData
                stateFlow.value.opening shouldBe atlasOpeningUiModel(profileData)
                stateFlow.value.isLoading shouldBe false
                job.cancel()
            }

        @Test
        fun `does not update state when observe emits null`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)
            val collector = AtlasProfileDataCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            profileDataFlow.emit(null)

            // ----- Assert -----
            stateFlow.value.userProfileData shouldBe null
            stateFlow.value.isLoading shouldBe true
            job.cancel()
        }
    }
}
