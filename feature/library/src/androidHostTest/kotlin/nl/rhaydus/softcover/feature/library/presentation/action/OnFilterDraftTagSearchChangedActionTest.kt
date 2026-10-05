package nl.rhaydus.softcover.feature.library.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilterDraft
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilters
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnFilterDraftTagSearchChangedActionTest {
    private lateinit var dependencies: LibraryDependencies
    private lateinit var stateFlow: MutableStateFlow<LibraryUiState>
    private lateinit var scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>

    private val tabId = "list-10"

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
        fun `no-op when filterDraft is null`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(filterDraft = null)

            // ----- Act -----
            OnFilterDraftTagSearchChangedAction(query = "kotlin").execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.filterDraft shouldBe null
            stateFlow.value.tagSearchField.text shouldBe ""
        }

        @Test
        fun `sets the draft's tag search and the tag search field to the given query`() = runTest {
            // ----- Arrange -----
            val draft = LibraryFilterDraft(
                tabId = tabId,
                filters = LibraryFilters(formats = setOf("Hardcover")),
                tagSearch = "",
            )
            stateFlow.value = LibraryUiState(filterDraft = draft)

            // ----- Act -----
            OnFilterDraftTagSearchChangedAction(query = "kotlin").execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.filterDraft shouldBe draft.copy(tagSearch = "kotlin")
            stateFlow.value.tagSearchField.text shouldBe "kotlin"
        }
    }
}
