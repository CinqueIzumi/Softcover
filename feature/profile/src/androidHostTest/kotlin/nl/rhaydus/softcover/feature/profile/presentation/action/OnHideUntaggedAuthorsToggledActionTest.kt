package nl.rhaydus.softcover.feature.profile.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.preferences.domain.usecase.SetHideUntaggedAuthorsUseCase
import nl.rhaydus.softcover.feature.profile.presentation.event.ProfileEvent
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.ProfileDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.LocalProfileVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.ProfileUiState
import nl.rhaydus.toad.ActionScope

class OnHideUntaggedAuthorsToggledActionTest {
    private lateinit var setHideUntaggedAuthorsUseCase: SetHideUntaggedAuthorsUseCase
    private lateinit var dependencies: ProfileDependencies
    private lateinit var stateFlow: MutableStateFlow<ProfileUiState>
    private lateinit var eventChannel: Channel<ProfileEvent>
    private lateinit var scope: ActionScope<ProfileUiState, ProfileEvent, LocalProfileVariables>

    @BeforeEach
    fun setUp() {
        setHideUntaggedAuthorsUseCase = mockk()
        stateFlow = MutableStateFlow(ProfileUiState())
        eventChannel = Channel(Channel.BUFFERED)
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LocalProfileVariables()),
            eventChannel = eventChannel,
        )
    }

    private fun stubDependencies(testScope: TestScope): ProfileDependencies {
        val dispatcher = UnconfinedTestDispatcher(testScope.testScheduler)
        return mockk<ProfileDependencies>(relaxed = true).also { mock ->
            every {
                mock.setHideUntaggedAuthorsUseCase
            } returns setHideUntaggedAuthorsUseCase

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
        fun `invokes setHideUntaggedAuthorsUseCase with the toggled value when turning on`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)

            coEvery {
                setHideUntaggedAuthorsUseCase(enabled = true)
            } returns Result.success(Unit)

            val action = OnHideUntaggedAuthorsToggledAction(newValue = true)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                setHideUntaggedAuthorsUseCase(enabled = true)
            }
        }

        @Test
        fun `invokes setHideUntaggedAuthorsUseCase with the toggled value when turning off`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)

            coEvery {
                setHideUntaggedAuthorsUseCase(enabled = false)
            } returns Result.success(Unit)

            val action = OnHideUntaggedAuthorsToggledAction(newValue = false)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                setHideUntaggedAuthorsUseCase(enabled = false)
            }
        }

        @Test
        fun `does not throw and leaves state untouched when the use case fails`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)
            val initialState = ProfileUiState()

            coEvery {
                setHideUntaggedAuthorsUseCase(enabled = true)
            } returns Result.failure(RuntimeException("write failed"))

            val action = OnHideUntaggedAuthorsToggledAction(newValue = true)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value shouldBe initialState
        }

        @Test
        fun `does not mutate state when the use case succeeds`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)
            val initialState = ProfileUiState()

            coEvery {
                setHideUntaggedAuthorsUseCase(enabled = true)
            } returns Result.success(Unit)

            val action = OnHideUntaggedAuthorsToggledAction(newValue = true)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value shouldBe initialState
        }

        @Test
        fun `does not send any event regardless of outcome`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)

            coEvery {
                setHideUntaggedAuthorsUseCase(enabled = true)
            } returns Result.failure(RuntimeException("write failed"))

            val action = OnHideUntaggedAuthorsToggledAction(newValue = true)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            val event = eventChannel.tryReceive().getOrNull()
            event shouldBe null
        }
    }
}
