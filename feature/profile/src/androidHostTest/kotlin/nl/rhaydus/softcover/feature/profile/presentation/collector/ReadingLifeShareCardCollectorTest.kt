package nl.rhaydus.softcover.feature.profile.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.share.ShareCaptureProgress
import nl.rhaydus.softcover.core.component.share.ShareCaptureState
import nl.rhaydus.softcover.core.component.share.ShareCardSheetUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.feature.profile.presentation.event.ProfileEvent
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.ProfileDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.LocalProfileVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.ProfileUiState
import nl.rhaydus.softcover.feature.profile.presentation.state.buildShareCard
import nl.rhaydus.softcover.feature.profile.presentation.state.buildShareProfile
import nl.rhaydus.softcover.feature.profile.presentation.state.buildShareReadingHistory
import nl.rhaydus.softcover.feature.profile.presentation.state.buildShareReadingStats
import nl.rhaydus.softcover.feature.profile.presentation.state.profileShareSheetFor
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

    private fun openSheet(
        name: String = "Jane Doe",
        progress: ShareCaptureProgress = ShareCaptureProgress.Idle,
        closing: Boolean = false,
    ): SheetSession<ShareCardSheetUiModel, Nothing> {
        val model = profileShareSheetFor(
            card = buildShareCard(name = name),
            progress = progress,
        )

        return SheetSession.Open(model = if (closing) model.requestingClose() else model)
    }

    private fun requestedState(
        shareSheet: SheetSession<ShareCardSheetUiModel, Nothing> = SheetSession.Closed,
        shareCapture: ShareCaptureState = ShareCaptureState(),
    ): ProfileUiState =
        ProfileUiState(
            readingStats = buildShareReadingStats(),
            readingHistory = buildShareReadingHistory(),
            userProfileData = buildShareProfile(),
            isShareSheetRequested = true,
            shareSheet = shareSheet,
            shareCapture = shareCapture,
        )

    private fun TestScope.launchCollector(): Job =
        launch {
            ReadingLifeShareCardCollector().onLaunch(
                scope = scope,
                dependencies = mockk<ProfileDependencies>(relaxed = true),
            )
        }

    @Nested
    inner class OnLaunch {
        @Test
        fun `leaves a closed session closed while the sheet is not requested`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ProfileUiState(
                readingStats = buildShareReadingStats(),
                readingHistory = buildShareReadingHistory(),
                userProfileData = buildShareProfile(),
            )

            // ----- Act -----
            val job = launchCollector()

            // ----- Assert -----
            stateFlow.value.shareSheet shouldBe SheetSession.Closed
            job.cancel()
        }

        @Test
        fun `opens the sheet once it is requested with both sources held`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = requestedState()

            // ----- Act -----
            val job = launchCollector()

            // ----- Assert -----
            stateFlow.value.shareSheet shouldBe openSheet()
            job.cancel()
        }

        @Test
        fun `stays closed while only the reading stats and history have arrived`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ProfileUiState(
                readingStats = buildShareReadingStats(),
                readingHistory = buildShareReadingHistory(),
                isShareSheetRequested = true,
            )

            // ----- Act -----
            val job = launchCollector()

            // ----- Assert -----
            stateFlow.value.shareSheet shouldBe SheetSession.Closed
            job.cancel()
        }

        @Test
        fun `opens when the request arrives after the collector started`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ProfileUiState(
                readingStats = buildShareReadingStats(),
                readingHistory = buildShareReadingHistory(),
                userProfileData = buildShareProfile(),
            )
            val job = launchCollector()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(isShareSheetRequested = true)

            // ----- Assert -----
            stateFlow.value.shareSheet shouldBe openSheet()
            job.cancel()
        }

        @Test
        fun `asks the open sheet to close when the request clears`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = requestedState()
            val job = launchCollector()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(isShareSheetRequested = false)

            // ----- Assert -----
            stateFlow.value.shareSheet shouldBe openSheet(closing = true)
            job.cancel()
        }

        @Test
        fun `asks the open sheet to close when the reading stats go away`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = requestedState()
            val job = launchCollector()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(readingStats = null)

            // ----- Assert -----
            stateFlow.value.shareSheet shouldBe openSheet(closing = true)
            job.cancel()
        }

        @Test
        fun `updates the open sheet when the profile changes`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = requestedState()
            val job = launchCollector()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(userProfileData = buildShareProfile(name = "John Roe"))

            // ----- Assert -----
            stateFlow.value.shareSheet shouldBe openSheet(name = "John Roe")
            job.cancel()
        }

        @Test
        fun `carries the capture progress into the open sheet`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = requestedState()
            val job = launchCollector()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(
                shareCapture = ShareCaptureState(progress = ShareCaptureProgress.Sharing),
            )

            // ----- Assert -----
            stateFlow.value.shareSheet shouldBe openSheet(progress = ShareCaptureProgress.Sharing)
            job.cancel()
        }

        @Test
        fun `returns the sheet to idle when the capture finishes`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = requestedState(shareCapture = ShareCaptureState(progress = ShareCaptureProgress.Saving))
            val job = launchCollector()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(shareCapture = ShareCaptureState())

            // ----- Assert -----
            stateFlow.value.shareSheet shouldBe openSheet()
            job.cancel()
        }

        @Test
        fun `reopens a closing sheet when it is requested again`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = requestedState(shareSheet = openSheet(closing = true))

            // ----- Act -----
            val job = launchCollector()

            // ----- Assert -----
            stateFlow.value.shareSheet shouldBe openSheet()
            job.cancel()
        }

        @Test
        fun `keeps a closing sheet closing while the request stays cleared`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ProfileUiState(
                readingStats = buildShareReadingStats(),
                readingHistory = buildShareReadingHistory(),
                userProfileData = buildShareProfile(),
                shareSheet = openSheet(closing = true),
            )
            val job = launchCollector()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(userProfileData = buildShareProfile(name = "John Roe"))

            // ----- Assert -----
            stateFlow.value.shareSheet shouldBe openSheet(closing = true)
            job.cancel()
        }
    }
}
