package nl.rhaydus.softcover.feature.profile.presentation.action

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.core.domain.account.ResetUserDataUseCase
import nl.rhaydus.softcover.feature.profile.presentation.event.ProfileEvent
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.ProfileDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.LocalProfileVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.LogOut
import nl.rhaydus.softcover.feature.profile.presentation.state.ProfileUiState
import nl.rhaydus.softcover.feature.profile.presentation.state.logOutSheetScaffold
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnLogOutSheetDismissedActionTest {
    private lateinit var resetUserDataUseCase: ResetUserDataUseCase
    private lateinit var dependencies: ProfileDependencies
    private lateinit var stateFlow: MutableStateFlow<ProfileUiState>
    private lateinit var eventChannel: Channel<ProfileEvent>
    private lateinit var scope: ActionScope<ProfileUiState, ProfileEvent, LocalProfileVariables>

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        resetUserDataUseCase = mockk()
        stateFlow = MutableStateFlow(ProfileUiState())
        eventChannel = Channel(Channel.BUFFERED)
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LocalProfileVariables()),
            eventChannel = eventChannel,
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun closingWithLogOut(): SheetSession<SheetScaffoldUiModel, LogOut> =
        SheetSession.Open(
            model = logOutSheetScaffold().requestingClose(),
            pending = LogOut,
        )

    private fun stubDependencies(testScope: TestScope): ProfileDependencies {
        val dispatcher = UnconfinedTestDispatcher(testScope.testScheduler)
        return mockk<ProfileDependencies>(relaxed = true).also { mock ->
            every {
                mock.resetUserDataUseCase
            } returns resetUserDataUseCase

            every {
                mock.coroutineScope
            } returns testScope

            every {
                mock.mainDispatcher
            } returns dispatcher

            every {
                mock.launch(any())
            } answers { callOriginal() }
        }
    }

    @Nested
    inner class Execute {
        @Test
        fun `resets user data, closes the sheet and sends LogOutUserEvent when a log-out was recorded`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)
            stateFlow.value = ProfileUiState(logOutSheet = closingWithLogOut())

            coEvery {
                resetUserDataUseCase()
            } returns Result.success(Unit)

            val action = OnLogOutSheetDismissedAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.logOutSheet shouldBe SheetSession.Closed
            eventChannel.tryReceive().getOrNull().shouldBeInstanceOf<ProfileEvent.LogOutUserEvent>()
            eventChannel.tryReceive().getOrNull() shouldBe null
        }

        @Test
        fun `closes the sheet and sends only LogOutFailedEvent when the reset fails`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)
            stateFlow.value = ProfileUiState(logOutSheet = closingWithLogOut())

            coEvery {
                resetUserDataUseCase()
            } returns Result.failure(RuntimeException("reset failed"))

            val action = OnLogOutSheetDismissedAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.logOutSheet shouldBe SheetSession.Closed
            eventChannel.tryReceive().getOrNull().shouldBeInstanceOf<ProfileEvent.LogOutFailedEvent>()
            eventChannel.tryReceive().getOrNull() shouldBe null
        }

        @Test
        fun `closes the sheet without resetting user data or sending an event when no log-out was recorded`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)
            stateFlow.value = ProfileUiState(logOutSheet = SheetSession.Open(model = logOutSheetScaffold()))

            val action = OnLogOutSheetDismissedAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 0) { resetUserDataUseCase() }
            stateFlow.value.logOutSheet shouldBe SheetSession.Closed
            eventChannel.tryReceive().getOrNull() shouldBe null
        }
    }
}
