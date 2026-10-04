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
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilters
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibrarySelectionPills
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.softcover.feature.library.presentation.state.filterPillButton
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PillButtonsCollectorTest {
    private lateinit var dependencies: LibraryDependencies
    private lateinit var stateFlow: MutableStateFlow<LibraryUiState>
    private lateinit var scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>

    private val readingTab = LibraryTab.Status.of(UserBookStatus.CURRENTLY_READING)
    private val readTab = LibraryTab.Status.of(UserBookStatus.READ)

    @BeforeEach
    fun setUp() {
        stateFlow = MutableStateFlow(LibraryUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LibraryLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
        dependencies = mockk(relaxed = true)
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `a tab with filters gets an active filter pill`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = PillButtonsCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = LibraryUiState(
                visibleTabs = listOf(readingTab),
                filtersByTab = mapOf(readingTab.id to LibraryFilters(formats = setOf("Paperback"))),
            )

            // ----- Assert -----
            stateFlow.value.filterPillByTab[readingTab.id] shouldBe filterPillButton(active = true)
            job.cancel()
        }

        @Test
        fun `a tab without filters gets an inactive filter pill`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = PillButtonsCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = LibraryUiState(visibleTabs = listOf(readingTab))

            // ----- Assert -----
            stateFlow.value.filterPillByTab[readingTab.id] shouldBe filterPillButton(active = false)
            job.cancel()
        }

        @Test
        fun `each visible tab is judged by its own filters`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = PillButtonsCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = LibraryUiState(
                visibleTabs = listOf(readingTab, readTab),
                filtersByTab = mapOf(readTab.id to LibraryFilters(formats = setOf("Ebook"))),
            )

            // ----- Assert -----
            stateFlow.value.filterPillByTab shouldBe mapOf(
                readingTab.id to filterPillButton(active = false),
                readTab.id to filterPillButton(active = true),
            )
            job.cancel()
        }

        @Test
        fun `selectionPills follow bulkActionInProgress`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = PillButtonsCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(bulkActionInProgress = true)

            // ----- Assert -----
            stateFlow.value.selectionPills shouldBe LibrarySelectionPills.of(bulkActionInProgress = true)
            job.cancel()
        }

        @Test
        fun `selectionPills return to clickable when the bulk action finishes`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = PillButtonsCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }
            stateFlow.value = stateFlow.value.copy(bulkActionInProgress = true)

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(bulkActionInProgress = false)

            // ----- Assert -----
            stateFlow.value.selectionPills shouldBe LibrarySelectionPills.of(bulkActionInProgress = false)
            job.cancel()
        }
    }
}
