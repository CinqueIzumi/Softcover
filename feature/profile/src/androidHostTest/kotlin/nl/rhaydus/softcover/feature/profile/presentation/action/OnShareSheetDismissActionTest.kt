package nl.rhaydus.softcover.feature.profile.presentation.action

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.share.ShareCaptureProgress
import nl.rhaydus.softcover.core.component.share.ShareCaptureState
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

class OnShareSheetDismissActionTest {
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
        fun `hides the sheet`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ProfileUiState(isShareSheetVisible = true)
            val action = OnShareSheetDismissAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isShareSheetVisible shouldBe false
        }

        @Test
        fun `clears the capture and close state`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ProfileUiState(
                isShareSheetVisible = true,
                shareCapture = ShareCaptureState(
                    progress = ShareCaptureProgress.Sharing,
                    closeRequested = true,
                ),
            )
            val action = OnShareSheetDismissAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.shareCapture shouldBe ShareCaptureState()
        }

        @Test
        fun `clears the held sheet scaffold busy and close request`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ProfileUiState(
                isShareSheetVisible = true,
                shareSheet = profileShareSheetFor(
                    card = buildShareCard(),
                    progress = ShareCaptureProgress.Saving,
                    closeRequested = true,
                ),
            )
            val action = OnShareSheetDismissAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            val scaffold = stateFlow.value.shareSheet.shouldNotBeNull().scaffold
            scaffold.busy shouldBe false
            scaffold.closeRequested shouldBe false
        }
    }
}
