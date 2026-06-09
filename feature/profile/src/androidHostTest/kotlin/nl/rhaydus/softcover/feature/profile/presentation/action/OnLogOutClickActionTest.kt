package nl.rhaydus.softcover.feature.profile.presentation.action

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.designsystem.presentation.toad.ActionScope
import nl.rhaydus.softcover.core.domain.account.ResetUserDataUseCase
import nl.rhaydus.softcover.feature.profile.presentation.event.LogOutUserEvent
import nl.rhaydus.softcover.feature.profile.presentation.event.ProfileEvent
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.ProfileDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.LocalProfileVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.ProfileUiState
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
        stateFlow = MutableStateFlow(ProfileUiState())
        eventChannel = Channel(Channel.BUFFERED)
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LocalProfileVariables()),
            eventChannel = eventChannel,
        )
    }

    private fun stubDependencies(testScope: kotlinx.coroutines.test.TestScope): ProfileDependencies {
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
        fun `sends LogOutUserEvent when use case succeeds`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)

            coEvery {
                resetUserDataUseCase()
            } returns Result.success(Unit)

            val action = OnLogOutClickAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            val event = eventChannel.tryReceive().getOrNull()
            event.shouldBeInstanceOf<LogOutUserEvent>()
        }

        @Test
        fun `does not send any event when use case fails`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)

            coEvery {
                resetUserDataUseCase()
            } returns Result.failure(RuntimeException("reset failed"))

            val action = OnLogOutClickAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            val event = eventChannel.tryReceive().getOrNull()
            event shouldBe null
        }

        @Test
        fun `does not mutate ui state when use case succeeds`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)
            val initialState = ProfileUiState()

            coEvery {
                resetUserDataUseCase()
            } returns Result.success(Unit)

            val action = OnLogOutClickAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value shouldBe initialState
        }

        @Test
        fun `does not mutate ui state when use case fails`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)
            val initialState = ProfileUiState()

            coEvery {
                resetUserDataUseCase()
            } returns Result.failure(RuntimeException("reset failed"))

            val action = OnLogOutClickAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value shouldBe initialState
        }

        @Test
        fun `invokes resetUserDataUseCase exactly once`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)

            coEvery {
                resetUserDataUseCase()
            } returns Result.success(Unit)

            val action = OnLogOutClickAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                resetUserDataUseCase()
            }
        }
    }
}
