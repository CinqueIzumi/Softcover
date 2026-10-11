package nl.rhaydus.softcover.feature.settings.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.feature.settings.presentation.event.LibraryVisibilitySettingsEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.LibraryVisibilitySettingsDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.LeaveScreen
import nl.rhaydus.softcover.feature.settings.presentation.state.LibraryVisibilitySettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.LibraryVisibilitySettingsUiState
import nl.rhaydus.softcover.feature.settings.presentation.state.discardSheetScaffold
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnDiscardLibraryVisibilityChangesActionTest {
    private lateinit var dependencies: LibraryVisibilitySettingsDependencies
    private lateinit var stateFlow: MutableStateFlow<LibraryVisibilitySettingsUiState>
    private lateinit var eventChannel: Channel<LibraryVisibilitySettingsEvent>
    private lateinit var scope: ActionScope<
        LibraryVisibilitySettingsUiState,
        LibraryVisibilitySettingsEvent,
        LibraryVisibilitySettingsLocalVariables,
        >

    @BeforeEach
    fun setUp() {
        dependencies = mockk(relaxed = true)
        stateFlow = MutableStateFlow(LibraryVisibilitySettingsUiState())
        eventChannel = Channel(Channel.BUFFERED)
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LibraryVisibilitySettingsLocalVariables()),
            eventChannel = eventChannel,
        )
    }

    @Nested
    inner class Execute {
        @Test
        fun `asks the sheet to close and records that the page is left afterwards`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryVisibilitySettingsUiState(
                discardSheet = SheetSession.Open(model = discardSheetScaffold()),
            )
            val action = OnDiscardLibraryVisibilityChangesAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.discardSheet shouldBe SheetSession.Open(
                model = discardSheetScaffold().requestingClose(),
                pending = LeaveScreen,
            )
        }

        @Test
        fun `stays closed when no discard sheet is open`() = runTest {
            // ----- Arrange -----
            val action = OnDiscardLibraryVisibilityChangesAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.discardSheet shouldBe SheetSession.Closed
        }

        @Test
        fun `sends no event`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryVisibilitySettingsUiState(
                discardSheet = SheetSession.Open(model = discardSheetScaffold()),
            )
            val action = OnDiscardLibraryVisibilityChangesAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            eventChannel.tryReceive().getOrNull() shouldBe null
        }
    }
}
