package nl.rhaydus.softcover.feature.profile.presentation.action

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.share.ShareCaptureProgress
import nl.rhaydus.softcover.core.component.share.ShareCaptureState
import nl.rhaydus.softcover.core.component.share.ShareCardSheetEvent
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
        fun `marks the held sheet busy with a sharing label for ShareStarted`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ProfileUiState(
                shareSheet = profileShareSheetFor(
                    card = buildShareCard(),
                    progress = ShareCaptureProgress.Idle,
                    closeRequested = false,
                ),
            )
            val action = OnShareCaptureEventAction(captureEvent = ShareCardSheetEvent.ShareStarted)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            val scaffold = stateFlow.value.shareSheet.shouldNotBeNull().scaffold
            scaffold.busy shouldBe true
            scaffold.footer.shouldNotBeNull().primary.shouldNotBeNull().label shouldBe "Sharing…"
        }

        @Test
        fun `marks the held sheet busy and running the secondary button for SaveStarted`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ProfileUiState(
                shareSheet = profileShareSheetFor(
                    card = buildShareCard(),
                    progress = ShareCaptureProgress.Idle,
                    closeRequested = false,
                ),
            )
            val action = OnShareCaptureEventAction(captureEvent = ShareCardSheetEvent.SaveStarted)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            val scaffold = stateFlow.value.shareSheet.shouldNotBeNull().scaffold
            scaffold.busy shouldBe true
            scaffold.footer.shouldNotBeNull().secondary.shouldNotBeNull().running shouldBe true
        }

        @Test
        fun `returns the held sheet to idle for CaptureFinished`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ProfileUiState(
                shareCapture = ShareCaptureState(progress = ShareCaptureProgress.Sharing),
                shareSheet = profileShareSheetFor(
                    card = buildShareCard(),
                    progress = ShareCaptureProgress.Sharing,
                    closeRequested = false,
                ),
            )
            val action = OnShareCaptureEventAction(captureEvent = ShareCardSheetEvent.CaptureFinished)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.shareCapture.progress shouldBe ShareCaptureProgress.Idle
            val scaffold = stateFlow.value.shareSheet.shouldNotBeNull().scaffold
            scaffold.busy shouldBe false
            scaffold.footer.shouldNotBeNull().primary.shouldNotBeNull().label shouldBe "Share"
        }

        @Test
        fun `requests close on the held sheet for Shared`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ProfileUiState(
                shareSheet = profileShareSheetFor(
                    card = buildShareCard(),
                    progress = ShareCaptureProgress.Idle,
                    closeRequested = false,
                ),
            )
            val action = OnShareCaptureEventAction(captureEvent = ShareCardSheetEvent.Shared)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.shareCapture.closeRequested shouldBe true
            stateFlow.value.shareSheet.shouldNotBeNull().scaffold.closeRequested shouldBe true
        }

        @Test
        fun `leaves capture state and visibility unchanged for Dismissed`() = runTest {
            // ----- Arrange -----
            val capture = ShareCaptureState(
                progress = ShareCaptureProgress.Saving,
                closeRequested = true,
            )
            stateFlow.value = ProfileUiState(
                isShareSheetVisible = true,
                shareCapture = capture,
            )
            val action = OnShareCaptureEventAction(captureEvent = ShareCardSheetEvent.Dismissed)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.shareCapture shouldBe capture
            stateFlow.value.isShareSheetVisible shouldBe true
        }

        @Test
        fun `records the capture without a sheet when none is held`() = runTest {
            // ----- Arrange -----
            val action = OnShareCaptureEventAction(captureEvent = ShareCardSheetEvent.SaveStarted)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.shareCapture.progress shouldBe ShareCaptureProgress.Saving
            stateFlow.value.shareSheet shouldBe null
        }
    }
}
