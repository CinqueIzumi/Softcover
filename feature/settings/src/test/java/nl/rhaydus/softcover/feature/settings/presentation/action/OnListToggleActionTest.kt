package nl.rhaydus.softcover.feature.settings.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.settings.presentation.event.LibraryVisibilitySettingsEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.LibraryVisibilitySettingsDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.LibraryVisibilitySettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.LibraryVisibilitySettingsUiState
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnListToggleActionTest {

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
        fun `adds id to draftEnabledListIds when enabled is true`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryVisibilitySettingsUiState(
                draftEnabledListIds = setOf(10),
                initialized = true,
            )
            val dependencies = stubDependencies(this)
            val action = OnListToggleAction(id = 20, enabled = true)

            // ----- Act -----
            action.execute(dependencies = dependencies, scope = scope)

            // ----- Assert -----
            stateFlow.value.draftEnabledListIds shouldBe setOf(10, 20)
        }

        @Test
        fun `removes id from draftEnabledListIds when enabled is false`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryVisibilitySettingsUiState(
                draftEnabledListIds = setOf(10, 20, 30),
                initialized = true,
            )
            val dependencies = stubDependencies(this)
            val action = OnListToggleAction(id = 20, enabled = false)

            // ----- Act -----
            action.execute(dependencies = dependencies, scope = scope)

            // ----- Assert -----
            stateFlow.value.draftEnabledListIds shouldBe setOf(10, 30)
        }

        @Test
        fun `does not modify persistedEnabledListIds`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryVisibilitySettingsUiState(
                persistedEnabledListIds = setOf(5),
                draftEnabledListIds = setOf(5),
                initialized = true,
            )
            val dependencies = stubDependencies(this)
            val action = OnListToggleAction(id = 99, enabled = true)

            // ----- Act -----
            action.execute(dependencies = dependencies, scope = scope)

            // ----- Assert -----
            stateFlow.value.persistedEnabledListIds shouldBe setOf(5)
        }

        @Test
        fun `does not modify draftEnabledStatusCodes`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryVisibilitySettingsUiState(
                draftEnabledStatusCodes = setOf(1, 3),
                draftEnabledListIds = setOf(10),
                initialized = true,
            )
            val dependencies = stubDependencies(this)
            val action = OnListToggleAction(id = 20, enabled = true)

            // ----- Act -----
            action.execute(dependencies = dependencies, scope = scope)

            // ----- Assert -----
            stateFlow.value.draftEnabledStatusCodes shouldBe setOf(1, 3)
        }

        @Test
        fun `toggling an already-present id with enabled true leaves the set unchanged`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryVisibilitySettingsUiState(
                draftEnabledListIds = setOf(10, 20),
                initialized = true,
            )
            val dependencies = stubDependencies(this)
            val action = OnListToggleAction(id = 10, enabled = true)

            // ----- Act -----
            action.execute(dependencies = dependencies, scope = scope)

            // ----- Assert -----
            stateFlow.value.draftEnabledListIds shouldBe setOf(10, 20)
        }

        @Test
        fun `toggling an absent id with enabled false leaves the set unchanged`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryVisibilitySettingsUiState(
                draftEnabledListIds = setOf(10),
                initialized = true,
            )
            val dependencies = stubDependencies(this)
            val action = OnListToggleAction(id = 999, enabled = false)

            // ----- Act -----
            action.execute(dependencies = dependencies, scope = scope)

            // ----- Assert -----
            stateFlow.value.draftEnabledListIds shouldBe setOf(10)
        }
    }
}
