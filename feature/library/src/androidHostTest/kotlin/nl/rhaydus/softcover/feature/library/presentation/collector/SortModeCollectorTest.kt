package nl.rhaydus.softcover.feature.library.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.domain.model.LibrarySortSettings
import nl.rhaydus.softcover.core.domain.model.SortDirection
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetLibrarySortSettingsAsFlowUseCase
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SortModeCollectorTest {
    private lateinit var settingsFlow: MutableStateFlow<Map<String, LibrarySortSettings>>
    private lateinit var dependencies: LibraryDependencies
    private lateinit var stateFlow: MutableStateFlow<LibraryUiState>
    private lateinit var scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>

    @BeforeEach
    fun setUp() {
        settingsFlow = MutableStateFlow(emptyMap())
        stateFlow = MutableStateFlow(LibraryUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LibraryLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        val useCase = mockk<GetLibrarySortSettingsAsFlowUseCase>()

        every {
            useCase()
        } returns settingsFlow

        dependencies = mockk<LibraryDependencies>(relaxed = true).also { mock ->
            every {
                mock.getLibrarySortSettingsAsFlowUseCase
            } returns useCase
        }
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `splits each tab's settings into a mode map and a direction map`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val job = launch {
                SortModeCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            settingsFlow.value = mapOf(
                "all" to LibrarySortSettings(
                    mode = LibrarySortMode.TITLE,
                    direction = SortDirection.DESCENDING,
                ),
                "status-3" to LibrarySortSettings(
                    mode = LibrarySortMode.DATE_FINISHED,
                    direction = SortDirection.ASCENDING,
                ),
            )

            // ----- Assert -----
            stateFlow.value.sortModeByTab shouldBe mapOf(
                "all" to LibrarySortMode.TITLE,
                "status-3" to LibrarySortMode.DATE_FINISHED,
            )
            stateFlow.value.sortDirectionByTab shouldBe mapOf(
                "all" to SortDirection.DESCENDING,
                "status-3" to SortDirection.ASCENDING,
            )
            job.cancel()
        }

        @Test
        fun `a tab dropped from the settings is dropped from both maps`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            settingsFlow.value = mapOf(
                "all" to LibrarySortSettings(
                    mode = LibrarySortMode.TITLE,
                    direction = SortDirection.ASCENDING,
                ),
            )
            val job = launch {
                SortModeCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            settingsFlow.value = emptyMap()

            // ----- Assert -----
            stateFlow.value.sortModeByTab shouldBe emptyMap()
            stateFlow.value.sortDirectionByTab shouldBe emptyMap()
            job.cancel()
        }
    }
}
