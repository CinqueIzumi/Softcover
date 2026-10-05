package nl.rhaydus.softcover.feature.library.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.presentation.model.LibraryTab
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.mapper.toEmptyStateUiModel
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class EmptyStatesCollectorTest {
    @Nested
    inner class EmptyStatesFor {
        @Test
        fun `yields one entry per tab keyed by tab id`() {
            // ----- Arrange -----
            val dnf = LibraryTab.Status.of(UserBookStatus.DID_NOT_FINISH)
            val list = LibraryTab.CustomList(
                listId = 3,
                listName = "Classics",
            )
            val tabs = listOf(LibraryTab.All, dnf, list)

            // ----- Act -----
            val result = emptyStatesFor(visibleTabs = tabs)

            // ----- Assert -----
            result shouldBe mapOf(
                LibraryTab.All.id to LibraryTab.All.toEmptyStateUiModel(),
                dnf.id to dnf.toEmptyStateUiModel(),
                list.id to list.toEmptyStateUiModel(),
            )
        }
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `publishes empty states for the visible tabs and follows tab changes`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val stateFlow = MutableStateFlow(LibraryUiState())
            val scope = ActionScope(
                stateFlow = stateFlow,
                localVariablesFlow = MutableStateFlow(LibraryLocalVariables()),
                eventChannel = Channel<LibraryEvent>(Channel.BUFFERED),
            )
            val job = launch {
                EmptyStatesCollector().onLaunch(
                    scope = scope,
                    dependencies = mockk<LibraryDependencies>(relaxed = true),
                )
            }
            val list = LibraryTab.CustomList(
                listId = 9,
                listName = "Sci-fi",
            )

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(visibleTabs = listOf(LibraryTab.All, list))

            // ----- Assert -----
            stateFlow.value.emptyStatesByTab shouldBe emptyStatesFor(listOf(LibraryTab.All, list))
            job.cancel()
        }
    }
}
