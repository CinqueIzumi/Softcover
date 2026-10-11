package nl.rhaydus.softcover.feature.library.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.openBulkRemoveSession
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnBulkRemoveSheetExpandedChangeActionTest {
    private lateinit var dependencies: LibraryDependencies
    private lateinit var stateFlow: MutableStateFlow<LibraryUiState>
    private lateinit var scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>

    @BeforeEach
    fun setUp() {
        dependencies = mockk(relaxed = true)
        stateFlow = MutableStateFlow(LibraryUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LibraryLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    @Nested
    inner class Execute {
        @Test
        fun `showing the sheet requests it without building its model`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(
                selectedBookIds = setOf(1, 2, 3),
                bulkActionInProgress = true,
            )

            // ----- Act -----
            OnBulkRemoveSheetExpandedChangeAction(shown = true).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isBulkRemoveSheetRequested shouldBe true
            stateFlow.value.bulkRemoveSheet shouldBe SheetSession.Closed
        }

        @Test
        fun `showing the sheet again leaves a closing session for the collector to replace`() = runTest {
            // ----- Arrange -----
            val closing = openBulkRemoveSession(closing = true)
            stateFlow.value = LibraryUiState(
                selectedBookIds = setOf(1),
                bulkRemoveSheet = closing,
            )

            // ----- Act -----
            OnBulkRemoveSheetExpandedChangeAction(shown = true).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isBulkRemoveSheetRequested shouldBe true
            stateFlow.value.bulkRemoveSheet shouldBe closing
        }

        @Test
        fun `hiding the sheet after its animated dismiss closes the session and withdraws the request`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(
                bulkRemoveSheet = openBulkRemoveSession(closing = true),
                isBulkRemoveSheetRequested = true,
            )

            // ----- Act -----
            OnBulkRemoveSheetExpandedChangeAction(shown = false).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.bulkRemoveSheet shouldBe SheetSession.Closed
            stateFlow.value.isBulkRemoveSheetRequested shouldBe false
        }
    }
}
