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
import nl.rhaydus.softcover.core.preferences.domain.usecase.ObserveAuthorDemographicsBackfilledUseCase
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileData
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveUserProfileDataUseCase
import nl.rhaydus.softcover.feature.profile.presentation.event.StatsAtlasEvent
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.StatsAtlasDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasLocalVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class AtlasProfileDataCollectorTest {
    private lateinit var observeUserProfileDataUseCase: ObserveUserProfileDataUseCase
    private lateinit var observeAuthorDemographicsBackfilledUseCase: ObserveAuthorDemographicsBackfilledUseCase
    private lateinit var stateFlow: MutableStateFlow<StatsAtlasUiState>
    private lateinit var scope: ActionScope<StatsAtlasUiState, StatsAtlasEvent, StatsAtlasLocalVariables>
    private lateinit var profileDataFlow: MutableSharedFlow<UserProfileData?>
    private lateinit var backfilledFlow: MutableStateFlow<Boolean>

    @BeforeEach
    fun setUp() {
        profileDataFlow = MutableSharedFlow()
        backfilledFlow = MutableStateFlow(true)
        observeUserProfileDataUseCase = mockk()
        observeAuthorDemographicsBackfilledUseCase = mockk()
        stateFlow = MutableStateFlow(StatsAtlasUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(StatsAtlasLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        every {
            observeUserProfileDataUseCase()
        } returns profileDataFlow

        every {
            observeAuthorDemographicsBackfilledUseCase()
        } returns backfilledFlow
    }

    private fun stubDependencies(testScope: TestScope): StatsAtlasDependencies {
        val dispatcher = UnconfinedTestDispatcher(testScope.testScheduler)
        return mockk<StatsAtlasDependencies>(relaxed = true).also { mock ->
            every {
                mock.observeUserProfileDataUseCase
            } returns observeUserProfileDataUseCase

            every {
                mock.observeAuthorDemographicsBackfilledUseCase
            } returns observeAuthorDemographicsBackfilledUseCase

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
        readingStreak = 7,
    )

    @Nested
    inner class OnLaunch {
        @Test
        fun `sets userProfileData and stops loading when the library is backfilled`() =
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
                stateFlow.value.isLoading shouldBe false
                job.cancel()
            }

        @Test
        fun `keeps loading while the library is not yet backfilled`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            backfilledFlow.value = false
            val dependencies = stubDependencies(this)
            val collector = AtlasProfileDataCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            profileDataFlow.emit(buildProfileData())

            // ----- Assert -----
            stateFlow.value.isLoading shouldBe true
            job.cancel()
        }

        @Test
        fun `stops loading once the backfill flag turns true`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            backfilledFlow.value = false
            val dependencies = stubDependencies(this)
            val collector = AtlasProfileDataCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }
            profileDataFlow.emit(buildProfileData())

            // ----- Act -----
            backfilledFlow.value = true

            // ----- Assert -----
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
