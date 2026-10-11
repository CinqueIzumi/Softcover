package nl.rhaydus.softcover.feature.settings.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.feature.settings.presentation.event.LibraryVisibilitySettingsEvent
import nl.rhaydus.softcover.feature.settings.presentation.model.LibraryTabEntry
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.LibraryVisibilitySettingsDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.LibraryVisibilitySettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.LibraryVisibilitySettingsUiState
import nl.rhaydus.softcover.feature.settings.presentation.state.withOrderedEntries
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnListToggleActionTest {
    private lateinit var stateFlow: MutableStateFlow<LibraryVisibilitySettingsUiState>
    private lateinit var scope: ActionScope<
        LibraryVisibilitySettingsUiState,
        LibraryVisibilitySettingsEvent,
        LibraryVisibilitySettingsLocalVariables,
        >

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

    private fun stubBookList(
        id: Int,
        name: String,
    ): BookList = mockk {
        every {
            this@mockk.id
        } returns id

        every {
            this@mockk.name
        } returns name

        every {
            this@mockk.books
        } returns emptyList()
    }

    @Nested
    inner class Execute {
        @Test
        fun `adds id to draftEnabledListIds when enabled is true`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryVisibilitySettingsUiState(
                draftEnabledListIds = persistentSetOf(10),
                initialized = true,
            )
            val dependencies = stubDependencies(this)
            val action = OnListToggleAction(
                id = 20,
                enabled = true,
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.draftEnabledListIds shouldBe setOf(10, 20)
        }

        @Test
        fun `rebuilds orderedEntries so the toggled list entry reflects the new checked state`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryVisibilitySettingsUiState(
                availableLists = persistentListOf(
                    stubBookList(
                        id = 20,
                        name = "Favourites",
                    ),
                ),
                draftEnabledListIds = persistentSetOf(),
                initialized = true,
            ).withOrderedEntries()
            val dependencies = stubDependencies(this)
            val action = OnListToggleAction(
                id = 20,
                enabled = true,
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            val entry = stateFlow.value.orderedEntries
                .filterIsInstance<LibraryTabEntry.CustomList>()
                .single()
            entry.listVisibilityToggle.checked shouldBe true
        }

        @Test
        fun `removes id from draftEnabledListIds when enabled is false`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryVisibilitySettingsUiState(
                draftEnabledListIds = persistentSetOf(10, 20, 30),
                initialized = true,
            )
            val dependencies = stubDependencies(this)
            val action = OnListToggleAction(
                id = 20,
                enabled = false,
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.draftEnabledListIds shouldBe setOf(10, 30)
        }
    }
}
