package nl.rhaydus.softcover.feature.profile.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.core.domain.account.ResetUserDataUseCase
import nl.rhaydus.softcover.feature.profile.presentation.event.ProfileEvent
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.ProfileDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.LocalProfileVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.LogOut
import nl.rhaydus.softcover.feature.profile.presentation.state.ProfileUiState
import nl.rhaydus.softcover.feature.profile.presentation.state.logOutSheetScaffold
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnLogOutClickActionTest {
    private lateinit var resetUserDataUseCase: ResetUserDataUseCase
    private lateinit var dependencies: ProfileDependencies
    private lateinit var stateFlow: MutableStateFlow<ProfileUiState>
    private lateinit var eventChannel: Channel<ProfileEvent>
    private lateinit var scope: ActionScope<ProfileUiState, ProfileEvent, LocalProfileVariables>

    @BeforeEach
    fun setUp() {
        resetUserDataUseCase = mockk()
        dependencies = mockk<ProfileDependencies>(relaxed = true)
        stateFlow = MutableStateFlow(ProfileUiState())
        eventChannel = Channel(Channel.BUFFERED)
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LocalProfileVariables()),
            eventChannel = eventChannel,
        )
    }

    private val openScaffold = logOutSheetScaffold()

    @Nested
    inner class Execute {
        @Test
        fun `records the log-out without performing it or sending an event`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ProfileUiState(logOutSheet = SheetSession.Open(model = openScaffold))
            val action = OnLogOutClickAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.logOutSheet shouldBe SheetSession.Open(
                model = openScaffold.requestingClose(),
                pending = LogOut,
            )
            coVerify(exactly = 0) { resetUserDataUseCase() }
            eventChannel.tryReceive().getOrNull() shouldBe null
        }

        @Test
        fun `stays closed when no log-out sheet is open`() = runTest {
            // ----- Arrange -----
            val action = OnLogOutClickAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.logOutSheet shouldBe SheetSession.Closed
        }
    }
}
