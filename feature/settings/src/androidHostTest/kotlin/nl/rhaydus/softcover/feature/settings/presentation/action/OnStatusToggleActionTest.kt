package nl.rhaydus.softcover.feature.settings.presentation.action

import io.kotest.matchers.shouldBe
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
import nl.rhaydus.softcover.feature.settings.presentation.event.LibraryVisibilitySettingsEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.LibraryVisibilitySettingsDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.LibraryVisibilitySettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.LibraryVisibilitySettingsUiState
import nl.rhaydus.toad.ActionScope

class OnStatusToggleActionTest {
    private lateinit var stateFlow: MutableStateFlow<LibraryVisibilitySettingsUiState>
    private lateinit var scope: ActionScope<LibraryVisibilitySettingsUiState, LibraryVisibilitySettingsEvent, LibraryVisibilitySettingsLocalVariables>

    @BeforeEach
    fun setUp() {
        stateFlow = MutableStateFlow(LibraryVisibilitySettingsUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LibraryVisibilitySettingsLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    private fun stubDependencies(testScope: TestScope): LibraryVisibilitySettingsDependencies {
        val dispatcher = UnconfinedTestDispatcher(testScope.testScheduler)
        return mockk<LibraryVisibilitySettingsDependencies>(relaxed = true).also { mock ->
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
        fun `adds code to draftEnabledStatusCodes when enabled is true`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryVisibilitySettingsUiState(
                draftEnabledStatusCodes = setOf(1),
                initialized = true,
            )
            val dependencies = stubDependencies(this)
            val action = OnStatusToggleAction(
                code = 3,
                enabled = true,
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.draftEnabledStatusCodes shouldBe setOf(1, 3)
        }

        @Test
        fun `removes code from draftEnabledStatusCodes when enabled is false`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryVisibilitySettingsUiState(
                draftEnabledStatusCodes = setOf(1, 3, 5),
                initialized = true,
            )
            val dependencies = stubDependencies(this)
            val action = OnStatusToggleAction(
                code = 3,
                enabled = false,
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.draftEnabledStatusCodes shouldBe setOf(1, 5)
        }

        @Test
        fun `does not modify persistedEnabledStatusCodes`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryVisibilitySettingsUiState(
                persistedEnabledStatusCodes = setOf(1, 3),
                draftEnabledStatusCodes = setOf(1, 3),
                initialized = true,
            )
            val dependencies = stubDependencies(this)
            val action = OnStatusToggleAction(
                code = 5,
                enabled = true,
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.persistedEnabledStatusCodes shouldBe setOf(1, 3)
        }

        @Test
        fun `does not modify draftEnabledListIds`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryVisibilitySettingsUiState(
                draftEnabledStatusCodes = setOf(1),
                draftEnabledListIds = setOf(99),
                initialized = true,
            )
            val dependencies = stubDependencies(this)
            val action = OnStatusToggleAction(
                code = 3,
                enabled = true,
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.draftEnabledListIds shouldBe setOf(99)
        }

        @Test
        fun `toggling an already-present code with enabled true leaves the set unchanged`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryVisibilitySettingsUiState(
                draftEnabledStatusCodes = setOf(1, 3),
                initialized = true,
            )
            val dependencies = stubDependencies(this)
            val action = OnStatusToggleAction(
                code = 1,
                enabled = true,
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.draftEnabledStatusCodes shouldBe setOf(1, 3)
        }

        @Test
        fun `toggling an absent code with enabled false leaves the set unchanged`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryVisibilitySettingsUiState(
                draftEnabledStatusCodes = setOf(1),
                initialized = true,
            )
            val dependencies = stubDependencies(this)
            val action = OnStatusToggleAction(
                code = 99,
                enabled = false,
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.draftEnabledStatusCodes shouldBe setOf(1)
        }
    }
}
