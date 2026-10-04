package nl.rhaydus.softcover.feature.profile.presentation.collector

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.profile.domain.model.GenreBreakdown
import nl.rhaydus.softcover.core.profile.domain.model.RatingsDistribution
import nl.rhaydus.softcover.core.profile.domain.model.ReadingLife
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileData
import nl.rhaydus.softcover.feature.profile.presentation.event.ProfileEvent
import nl.rhaydus.softcover.feature.profile.presentation.mapper.toReadingLifeShareCardUiModel
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.ProfileDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.LocalProfileVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.ProfileUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ReadingLifeShareCardCollectorTest {
    private lateinit var stateFlow: MutableStateFlow<ProfileUiState>
    private lateinit var eventChannel: Channel<ProfileEvent>
    private lateinit var scope: ActionScope<ProfileUiState, ProfileEvent, LocalProfileVariables>

    @BeforeEach
    fun setUp() {
        stateFlow = MutableStateFlow(ProfileUiState())
        eventChannel = Channel(Channel.BUFFERED)
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LocalProfileVariables()),
            eventChannel = eventChannel,
        )
    }

    private fun buildProfileData(name: String = "Jane Doe"): UserProfileData = UserProfileData(
        profileImageUrl = "https://example.com/avatar.png",
        name = name,
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
        fun `leaves the card null when neither reading life nor profile has arrived`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = ReadingLifeShareCardCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = mockk<ProfileDependencies>(relaxed = true),
                )
            }

            // ----- Assert -----
            stateFlow.value.readingLifeShareCard.shouldBeNull()
            job.cancel()
        }

        @Test
        fun `leaves the card null when only the reading life has arrived`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ProfileUiState(readingLife = buildReadingLife())
            val collector = ReadingLifeShareCardCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = mockk<ProfileDependencies>(relaxed = true),
                )
            }

            // ----- Assert -----
            stateFlow.value.readingLifeShareCard.shouldBeNull()
            job.cancel()
        }

        @Test
        fun `leaves the card null when only the profile has arrived`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ProfileUiState(userProfileData = buildProfileData())
            val collector = ReadingLifeShareCardCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = mockk<ProfileDependencies>(relaxed = true),
                )
            }

            // ----- Assert -----
            stateFlow.value.readingLifeShareCard.shouldBeNull()
            job.cancel()
        }

        @Test
        fun `maps the card once both the reading life and profile are held`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val readingLife = buildReadingLife()
            val profile = buildProfileData()
            stateFlow.value = ProfileUiState(
                readingLife = readingLife,
                userProfileData = profile,
            )
            val collector = ReadingLifeShareCardCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = mockk<ProfileDependencies>(relaxed = true),
                )
            }

            // ----- Assert -----
            stateFlow.value.readingLifeShareCard shouldBe readingLife.toReadingLifeShareCardUiModel(profile)
            job.cancel()
        }

        @Test
        fun `builds the card when the profile arrives after the reading life`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val readingLife = buildReadingLife()
            val profile = buildProfileData()
            stateFlow.value = ProfileUiState(readingLife = readingLife)
            val collector = ReadingLifeShareCardCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = mockk<ProfileDependencies>(relaxed = true),
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(userProfileData = profile)

            // ----- Assert -----
            stateFlow.value.readingLifeShareCard shouldBe readingLife.toReadingLifeShareCardUiModel(profile)
            job.cancel()
        }

        @Test
        fun `rebuilds the card against the held reading life when the profile changes`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val readingLife = buildReadingLife()
            val updatedProfile = buildProfileData(name = "Jane Updated")
            stateFlow.value = ProfileUiState(
                readingLife = readingLife,
                userProfileData = buildProfileData(),
            )
            val collector = ReadingLifeShareCardCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = mockk<ProfileDependencies>(relaxed = true),
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(userProfileData = updatedProfile)

            // ----- Assert -----
            stateFlow.value.readingLifeShareCard shouldBe readingLife.toReadingLifeShareCardUiModel(updatedProfile)
            job.cancel()
        }

        @Test
        fun `rebuilds the card against the held profile when the reading life changes`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val profile = buildProfileData()
            val updatedReadingLife = buildReadingLife(trackedYears = 5)
            stateFlow.value = ProfileUiState(
                readingLife = buildReadingLife(),
                userProfileData = profile,
            )
            val collector = ReadingLifeShareCardCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = mockk<ProfileDependencies>(relaxed = true),
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(readingLife = updatedReadingLife)

            // ----- Assert -----
            stateFlow.value.readingLifeShareCard shouldBe updatedReadingLife.toReadingLifeShareCardUiModel(profile)
            job.cancel()
        }

        @Test
        fun `clears the card when the reading life goes away`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ProfileUiState(
                readingLife = buildReadingLife(),
                userProfileData = buildProfileData(),
            )
            val collector = ReadingLifeShareCardCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = mockk<ProfileDependencies>(relaxed = true),
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(readingLife = null)

            // ----- Assert -----
            stateFlow.value.readingLifeShareCard.shouldBeNull()
            job.cancel()
        }
    }
}
