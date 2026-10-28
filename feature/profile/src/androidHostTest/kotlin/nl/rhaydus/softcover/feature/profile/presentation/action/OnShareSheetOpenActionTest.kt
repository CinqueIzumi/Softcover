package nl.rhaydus.softcover.feature.profile.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.share.ShareCaptureProgress
import nl.rhaydus.softcover.core.component.share.ShareCaptureState
import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.feature.profile.presentation.event.ProfileEvent
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.ProfileDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.LocalProfileVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.ProfileUiState
import nl.rhaydus.softcover.feature.profile.presentation.state.buildShareProfile
import nl.rhaydus.softcover.feature.profile.presentation.state.buildShareReadingHistory
import nl.rhaydus.softcover.feature.profile.presentation.state.buildShareReadingStats
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
        fun `requests the sheet when the reading stats, history and profile are loaded`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ProfileUiState(
                readingStats = buildShareReadingStats(),
                readingHistory = buildShareReadingHistory(),
                userProfileData = buildShareProfile(),
            )
            val action = OnShareSheetOpenAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isShareSheetRequested shouldBe true
        }

        @Test
        fun `leaves opening the session to the collector`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ProfileUiState(
                readingStats = buildShareReadingStats(),
                readingHistory = buildShareReadingHistory(),
                userProfileData = buildShareProfile(),
            )
            val action = OnShareSheetOpenAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.shareSheet shouldBe SheetSession.Closed
        }

        @Test
        fun `resets leftover capture progress from an earlier opening`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ProfileUiState(
                readingStats = buildShareReadingStats(),
                readingHistory = buildShareReadingHistory(),
                userProfileData = buildShareProfile(),
                shareCapture = ShareCaptureState(progress = ShareCaptureProgress.Saving),
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
        fun `stays unrequested when only the reading stats and history have loaded`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ProfileUiState(
                readingStats = buildShareReadingStats(),
                readingHistory = buildShareReadingHistory(),
            )
            val action = OnShareSheetOpenAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isShareSheetRequested shouldBe false
        }

        @Test
        fun `stays unrequested when only the profile has loaded`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ProfileUiState(userProfileData = buildShareProfile())
            val action = OnShareSheetOpenAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isShareSheetRequested shouldBe false
        }
    }
}
