package nl.rhaydus.softcover.feature.profile.presentation.collector

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.share.ShareCaptureProgress
import nl.rhaydus.softcover.core.component.share.ShareCaptureState
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
            stateFlow.value.shareSheet.shouldBeNull()
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
            stateFlow.value.shareSheet.shouldBeNull()
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
            stateFlow.value.shareSheet.shouldNotBeNull().card shouldBe readingLife.toReadingLifeShareCardUiModel(profile)
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
            stateFlow.value.shareSheet.shouldNotBeNull().card shouldBe readingLife.toReadingLifeShareCardUiModel(profile)
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
            stateFlow.value.shareSheet.shouldBeNull()
            job.cancel()
        }

        @Test
        fun `keeps the capture progress and close request when the profile changes mid-capture`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ProfileUiState(
                readingLife = buildReadingLife(),
                userProfileData = buildProfileData(),
                shareCapture = ShareCaptureState(
                    progress = ShareCaptureProgress.Sharing,
                    closeRequested = true,
                ),
            )
            val collector = ReadingLifeShareCardCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = mockk<ProfileDependencies>(relaxed = true),
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(userProfileData = buildProfileData(name = "John Roe"))

            // ----- Assert -----
            val sheet = stateFlow.value.shareSheet.shouldNotBeNull()
            sheet.displayName shouldBe "John Roe-reading-life"
            sheet.scaffold.busy shouldBe true
            sheet.scaffold.closeRequested shouldBe true
            stateFlow.value.shareCapture shouldBe ShareCaptureState(
                ShareCaptureProgress.Sharing,
                true,
            )
            job.cancel()
        }

        @Test
        fun `names the sheet after the reader`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ProfileUiState(
                readingLife = buildReadingLife(),
                userProfileData = buildProfileData(name = "Jane Doe"),
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
            stateFlow.value.shareSheet.shouldNotBeNull().displayName shouldBe "Jane Doe-reading-life"
            job.cancel()
        }
    }
}
