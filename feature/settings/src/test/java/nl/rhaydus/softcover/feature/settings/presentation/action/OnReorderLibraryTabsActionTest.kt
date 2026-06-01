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

class OnReorderLibraryTabsActionTest {

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
        fun `updates draftTabOrder to the supplied list`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryVisibilitySettingsUiState(
                draftTabOrder = emptyList(),
                initialized = true,
            )
            val dependencies = stubDependencies(this)
            val newOrder = listOf("list-2", "status-3", "list-1")
            val action = OnReorderLibraryTabsAction(newOrderedIds = newOrder)

            // ----- Act -----
            action.execute(dependencies = dependencies, scope = scope)

            // ----- Assert -----
            stateFlow.value.draftTabOrder shouldBe newOrder
        }

        @Test
        fun `replaces an existing non-empty draftTabOrder`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryVisibilitySettingsUiState(
                draftTabOrder = listOf("status-1", "list-5"),
                initialized = true,
            )
            val dependencies = stubDependencies(this)
            val newOrder = listOf("list-5", "status-1")
            val action = OnReorderLibraryTabsAction(newOrderedIds = newOrder)

            // ----- Act -----
            action.execute(dependencies = dependencies, scope = scope)

            // ----- Assert -----
            stateFlow.value.draftTabOrder shouldBe newOrder
        }

        @Test
        fun `does not modify draftEnabledStatusCodes`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryVisibilitySettingsUiState(
                draftEnabledStatusCodes = setOf(1, 3),
                initialized = true,
            )
            val dependencies = stubDependencies(this)
            val action = OnReorderLibraryTabsAction(newOrderedIds = listOf("list-1"))

            // ----- Act -----
            action.execute(dependencies = dependencies, scope = scope)

            // ----- Assert -----
            stateFlow.value.draftEnabledStatusCodes shouldBe setOf(1, 3)
        }

        @Test
        fun `does not modify draftEnabledListIds`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryVisibilitySettingsUiState(
                draftEnabledListIds = setOf(10, 20),
                initialized = true,
            )
            val dependencies = stubDependencies(this)
            val action = OnReorderLibraryTabsAction(newOrderedIds = listOf("status-2"))

            // ----- Act -----
            action.execute(dependencies = dependencies, scope = scope)

            // ----- Assert -----
            stateFlow.value.draftEnabledListIds shouldBe setOf(10, 20)
        }

        @Test
        fun `does not modify persistedTabOrder`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryVisibilitySettingsUiState(
                persistedTabOrder = listOf("status-1", "list-5"),
                draftTabOrder = emptyList(),
                initialized = true,
            )
            val dependencies = stubDependencies(this)
            val action = OnReorderLibraryTabsAction(newOrderedIds = listOf("list-5", "status-1"))

            // ----- Act -----
            action.execute(dependencies = dependencies, scope = scope)

            // ----- Assert -----
            stateFlow.value.persistedTabOrder shouldBe listOf("status-1", "list-5")
        }

        @Test
        fun `accepts an empty list clearing the draft order`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryVisibilitySettingsUiState(
                draftTabOrder = listOf("list-1", "status-2"),
                initialized = true,
            )
            val dependencies = stubDependencies(this)
            val action = OnReorderLibraryTabsAction(newOrderedIds = emptyList())

            // ----- Act -----
            action.execute(dependencies = dependencies, scope = scope)

            // ----- Assert -----
            stateFlow.value.draftTabOrder shouldBe emptyList()
        }
    }
}
