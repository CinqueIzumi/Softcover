package nl.rhaydus.softcover.feature.library.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilterDraft
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilters
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope

class OnFilterDraftClearAllActionTest {
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
            OnFilterDraftClearAllAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.filterDraft shouldBe null
        }

        @Test
        fun `resets the draft's facet selections to empty`() = runTest {
            // ----- Arrange -----
            val draft = LibraryFilterDraft(
                tabId = tabId,
                filters = LibraryFilters(
                    formats = setOf("Hardcover"),
                    owned = true,
                    ratingMin = 4.0,
                ),
            )
            stateFlow.value = LibraryUiState(filterDraft = draft)

            // ----- Act -----
            OnFilterDraftClearAllAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.filterDraft?.filters shouldBe LibraryFilters()
        }

        @Test
        fun `leaves the tag search query untouched`() = runTest {
            // ----- Arrange -----
            val draft = LibraryFilterDraft(
                tabId = tabId,
                filters = LibraryFilters(formats = setOf("Hardcover")),
                tagSearch = "kotlin",
            )
            stateFlow.value = LibraryUiState(filterDraft = draft)

            // ----- Act -----
            OnFilterDraftClearAllAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.filterDraft?.tagSearch shouldBe "kotlin"
        }

        @Test
        fun `leaves tabId untouched`() = runTest {
            // ----- Arrange -----
            val draft = LibraryFilterDraft(
                tabId = tabId,
                filters = LibraryFilters(formats = setOf("Hardcover")),
            )
            stateFlow.value = LibraryUiState(filterDraft = draft)

            // ----- Act -----
            OnFilterDraftClearAllAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.filterDraft?.tabId shouldBe tabId
        }

        @Test
        fun `is idempotent when the draft's filters are already empty`() = runTest {
            // ----- Arrange -----
            val draft = LibraryFilterDraft(
                tabId = tabId,
                filters = LibraryFilters(),
            )
            stateFlow.value = LibraryUiState(filterDraft = draft)

            // ----- Act -----
            OnFilterDraftClearAllAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.filterDraft shouldBe draft
        }
    }
}
