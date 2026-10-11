package nl.rhaydus.softcover.feature.profile.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.share.ShareCaptureProgress
import nl.rhaydus.softcover.core.component.share.ShareCaptureState
import nl.rhaydus.softcover.core.component.share.ShareCardSheetEvent
import nl.rhaydus.softcover.core.component.share.ShareCardSheetUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.feature.profile.presentation.event.ProfileEvent
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.ProfileDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.LocalProfileVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.ProfileUiState
import nl.rhaydus.softcover.feature.profile.presentation.state.buildShareCard
import nl.rhaydus.softcover.feature.profile.presentation.state.profileShareSheetFor
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnShareCaptureEventActionTest {
    private lateinit var dependencies: ProfileDependencies
    private lateinit var stateFlow: MutableStateFlow<ProfileUiState>
    private lateinit var scope: ActionScope<ProfileUiState, ProfileEvent, LocalProfileVariables>

    @BeforeEach
    fun setUp() {
        dependencies = mockk(relaxed = true)
        stateFlow = MutableStateFlow(ProfileUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LocalProfileVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    @Nested
    inner class Execute {
        @Test
        fun `records sharing progress for ShareStarted`() = runTest {
            // ----- Arrange -----
            val action = OnShareCaptureEventAction(captureEvent = ShareCardSheetEvent.ShareStarted)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.shareCapture shouldBe ShareCaptureState(progress = ShareCaptureProgress.Sharing)
        }

        @Test
        fun `records saving progress for SaveStarted`() = runTest {
            // ----- Arrange -----
            val action = OnShareCaptureEventAction(captureEvent = ShareCardSheetEvent.SaveStarted)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.shareCapture shouldBe ShareCaptureState(progress = ShareCaptureProgress.Saving)
        }

        @Test
        fun `returns the capture to idle for CaptureFinished`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ProfileUiState(
                shareCapture = ShareCaptureState(progress = ShareCaptureProgress.Sharing),
            )
            val action = OnShareCaptureEventAction(captureEvent = ShareCardSheetEvent.CaptureFinished)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.shareCapture shouldBe ShareCaptureState()
        }

        @Test
        fun `keeps the sheet requested while a capture runs`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ProfileUiState(isShareSheetRequested = true)
            val action = OnShareCaptureEventAction(captureEvent = ShareCardSheetEvent.ShareStarted)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isShareSheetRequested shouldBe true
        }

        @Test
        fun `withdraws the sheet request for Shared`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ProfileUiState(isShareSheetRequested = true)
            val action = OnShareCaptureEventAction(captureEvent = ShareCardSheetEvent.Shared)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isShareSheetRequested shouldBe false
        }

        @Test
        fun `leaves the capture and the request unchanged for Dismissed`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ProfileUiState(
                isShareSheetRequested = true,
                shareCapture = ShareCaptureState(progress = ShareCaptureProgress.Saving),
            )
            val action = OnShareCaptureEventAction(captureEvent = ShareCardSheetEvent.Dismissed)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isShareSheetRequested shouldBe true
            stateFlow.value.shareCapture shouldBe ShareCaptureState(progress = ShareCaptureProgress.Saving)
        }

        @Test
        fun `leaves the open share session to the collector for Shared`() = runTest {
            // ----- Arrange -----
            val open: SheetSession<ShareCardSheetUiModel, Nothing> = SheetSession.Open(
                model = profileShareSheetFor(
                    card = buildShareCard(),
                    progress = ShareCaptureProgress.Idle,
                ),
            )
            stateFlow.value = ProfileUiState(shareSheet = open)
            val action = OnShareCaptureEventAction(captureEvent = ShareCardSheetEvent.Shared)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.shareSheet shouldBe open
        }
    }
}
