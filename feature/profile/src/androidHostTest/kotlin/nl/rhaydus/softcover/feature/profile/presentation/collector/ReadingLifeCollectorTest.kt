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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.profile.domain.model.GenreBreakdown
import nl.rhaydus.softcover.core.profile.domain.model.RatingsDistribution
import nl.rhaydus.softcover.core.profile.domain.model.ReadingLife
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileData
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveReadingLifeUseCase
import nl.rhaydus.softcover.feature.profile.presentation.event.ProfileEvent
import nl.rhaydus.softcover.feature.profile.presentation.mapper.toReadingLifeShareCardUiModel
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.ProfileDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.LocalProfileVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.ProfileUiState
import nl.rhaydus.toad.ActionScope

class ReadingLifeCollectorTest {
    private lateinit var observeReadingLifeUseCase: ObserveReadingLifeUseCase
    private lateinit var stateFlow: MutableStateFlow<ProfileUiState>
    private lateinit var eventChannel: Channel<ProfileEvent>
    private lateinit var scope: ActionScope<ProfileUiState, ProfileEvent, LocalProfileVariables>
    private lateinit var readingLifeFlow: MutableSharedFlow<ReadingLife?>

    @BeforeEach
    fun setUp() {
        readingLifeFlow = MutableSharedFlow()
        observeReadingLifeUseCase = mockk()
        stateFlow = MutableStateFlow(ProfileUiState())
        eventChannel = Channel(Channel.BUFFERED)
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LocalProfileVariables()),
            eventChannel = eventChannel,
        )

        every {
            observeReadingLifeUseCase()
        } returns readingLifeFlow
    }

    private fun stubDependencies(testScope: TestScope): ProfileDependencies {
        val dispatcher = UnconfinedTestDispatcher(testScope.testScheduler)
        return mockk<ProfileDependencies>(relaxed = true).also { mock ->
            every {
                mock.observeReadingLifeUseCase
            } returns observeReadingLifeUseCase

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

    private fun buildReadingLife(trackedYears: Int = 3): ReadingLife = ReadingLife(
        booksByYear = emptyList(),
        pagesByYear = emptyList(),
        pagesByMonth = emptyList(),
        genres = GenreBreakdown(),
        ratings = RatingsDistribution(),
        recentlyLoved = emptyList(),
        trackedYears = trackedYears,
    )

    @Nested
    inner class OnLaunch {
        @Test
        fun `updates state readingLife when observe emits non-null`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val readingLife = buildReadingLife()
            val dependencies = stubDependencies(this)
            val collector = ReadingLifeCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            readingLifeFlow.emit(readingLife)

            // ----- Assert -----
            stateFlow.value.readingLife shouldBe readingLife
            job.cancel()
        }

        @Test
        fun `does not update state when observe emits null`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)
            val collector = ReadingLifeCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            readingLifeFlow.emit(null)

            // ----- Assert -----
            stateFlow.value.readingLife shouldBe null
            job.cancel()
        }

        @Test
        fun `leaves readingLifeShareCard null when userProfileData has not arrived yet`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val readingLife = buildReadingLife()
            val dependencies = stubDependencies(this)
            val collector = ReadingLifeCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            readingLifeFlow.emit(readingLife)

            // ----- Assert -----
            stateFlow.value.readingLifeShareCard shouldBe null
            job.cancel()
        }

        @Test
        fun `computes readingLifeShareCard from the already-held userProfileData when readingLife arrives`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val profileData = buildProfileData()
                val readingLife = buildReadingLife()
                stateFlow.value = ProfileUiState(userProfileData = profileData)
                val dependencies = stubDependencies(this)
                val collector = ReadingLifeCollector()
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Act -----
                readingLifeFlow.emit(readingLife)

                // ----- Assert -----
                stateFlow.value.readingLifeShareCard shouldBe readingLife.toReadingLifeShareCardUiModel(profileData)
                job.cancel()
            }

        @Test
        fun `recombines readingLifeShareCard using the currently held userProfileData when readingLife emits a second time`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val profileData = buildProfileData()
                val firstReadingLife = buildReadingLife(trackedYears = 3)
                val secondReadingLife = buildReadingLife(trackedYears = 5)
                stateFlow.value = ProfileUiState(userProfileData = profileData)
                val dependencies = stubDependencies(this)
                val collector = ReadingLifeCollector()
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Act -----
                readingLifeFlow.emit(firstReadingLife)
                readingLifeFlow.emit(secondReadingLife)

                // ----- Assert -----
                stateFlow.value.readingLifeShareCard shouldBe secondReadingLife.toReadingLifeShareCardUiModel(profileData)
                job.cancel()
            }
    }
}
