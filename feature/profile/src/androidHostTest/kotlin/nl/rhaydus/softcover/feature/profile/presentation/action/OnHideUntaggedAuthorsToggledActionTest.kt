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
import nl.rhaydus.softcover.core.preferences.domain.usecase.SetHideUntaggedAuthorsUseCase
import nl.rhaydus.softcover.feature.profile.presentation.event.StatsAtlasEvent
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.StatsAtlasDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasLocalVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnHideUntaggedAuthorsToggledActionTest {
    private lateinit var setHideUntaggedAuthorsUseCase: SetHideUntaggedAuthorsUseCase
    private lateinit var dependencies: StatsAtlasDependencies
    private lateinit var stateFlow: MutableStateFlow<StatsAtlasUiState>
    private lateinit var eventChannel: Channel<StatsAtlasEvent>
    private lateinit var scope: ActionScope<StatsAtlasUiState, StatsAtlasEvent, StatsAtlasLocalVariables>

    @BeforeEach
    fun setUp() {
        setHideUntaggedAuthorsUseCase = mockk()
        stateFlow = MutableStateFlow(StatsAtlasUiState())
        eventChannel = Channel(Channel.BUFFERED)
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(StatsAtlasLocalVariables()),
            eventChannel = eventChannel,
        )
    }

    private fun stubDependencies(testScope: TestScope): StatsAtlasDependencies {
        val dispatcher = UnconfinedTestDispatcher(testScope.testScheduler)
        return mockk<StatsAtlasDependencies>(relaxed = true).also { mock ->
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
        fun `does not throw and leaves state untouched when the use case fails`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)
            val initialState = StatsAtlasUiState()

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
    }
}
