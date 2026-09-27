package nl.rhaydus.softcover.feature.library.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilterValue
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilters
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ActiveFilterChipsCollectorTest {
    private lateinit var dependencies: LibraryDependencies
    private lateinit var stateFlow: MutableStateFlow<LibraryUiState>
    private lateinit var scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>

    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeEach
    fun setUp() {
        stateFlow = MutableStateFlow(LibraryUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LibraryLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
        dependencies = mockk<LibraryDependencies>(relaxed = true).also { mock ->
            every {
                mock.defaultDispatcher
            } returns testDispatcher
        }
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `activeFilterChipsByTab and activeFilterValueByChipKey are populated per tab`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val collector = ActiveFilterChipsCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = LibraryUiState(
                filtersByTab = mapOf(
                    "all" to LibraryFilters(formats = setOf("ebook")),
                    "read" to LibraryFilters(readYear = 2021),
                ),
            )

            // ----- Assert -----
            stateFlow.value.activeFilterChipsFor("all").chips.map { it.key } shouldBe listOf("format:ebook")
            stateFlow.value.activeFilterChipsFor("read").chips.map { it.key } shouldBe listOf("readYear:2021")
            stateFlow.value.activeFilterValueByChipKey["format:ebook"] shouldBe
                LibraryFilterValue.Format(value = "ebook")
            job.cancel()
        }

        @Test
        fun `a tab absent from filtersByTab falls back to an empty active-filter model`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val collector = ActiveFilterChipsCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = LibraryUiState(filtersByTab = mapOf("all" to LibraryFilters(formats = setOf("ebook"))))

            // ----- Assert -----
            stateFlow.value.activeFilterChipsFor("missing").chips shouldBe emptyList()
            stateFlow.value.activeFilterChipsFor("missing").clearAll shouldBe null
            job.cancel()
        }

        @Test
        fun `unrelated state change that leaves filtersByTab equal does not retrigger recompute`() =
            runTest(testDispatcher) {
                // ----- Arrange -----
                val collector = ActiveFilterChipsCollector()
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                stateFlow.value = LibraryUiState(filtersByTab = mapOf("all" to LibraryFilters(formats = setOf("ebook"))))
                val chipsByTabAfterFirstEmit = stateFlow.value.activeFilterChipsByTab
                val valueByKeyAfterFirstEmit = stateFlow.value.activeFilterValueByChipKey

                // ----- Act -----
                stateFlow.value = stateFlow.value.copy(isArrangeSheetExpanded = true)

                // ----- Assert -----
                (stateFlow.value.activeFilterChipsByTab === chipsByTabAfterFirstEmit) shouldBe true
                (stateFlow.value.activeFilterValueByChipKey === valueByKeyAfterFirstEmit) shouldBe true
                job.cancel()
            }
    }
}
