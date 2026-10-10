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

class OnShareSheetOpenActionTest {
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
        fun `shows the sheet`() = runTest {
            // ----- Arrange -----
            val action = OnShareSheetOpenAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isShareSheetVisible shouldBe true
        }

        @Test
        fun `resets leftover capture progress and close request`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ProfileUiState(
                shareCapture = ShareCaptureState(
                    progress = ShareCaptureProgress.Saving,
                    closeRequested = true,
                ),
            )
            val action = OnShareSheetOpenAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.shareCapture shouldBe ShareCaptureState()
        }

        @Test
        fun `resets the held sheet scaffold to idle`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ProfileUiState(
                shareSheet = profileShareSheetFor(
                    card = buildShareCard(),
                    progress = ShareCaptureProgress.Sharing,
                    closeRequested = true,
                ),
            )
            val action = OnShareSheetOpenAction()

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

        @Test
        fun `leaves the sheet null when none is held`() = runTest {
            // ----- Arrange -----
            val action = OnShareSheetOpenAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.shareSheet shouldBe null
        }
    }
}
