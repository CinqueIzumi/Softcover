package nl.rhaydus.softcover.feature.library.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.domain.model.Tag
import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilterOptions
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilterValue
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope

class FilterChipModelsCollectorTest {
    private lateinit var dependencies: LibraryDependencies
    private lateinit var stateFlow: MutableStateFlow<LibraryUiState>
    private lateinit var scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>

    private val testDispatcher = UnconfinedTestDispatcher()

    private val tagFiction = Tag(
        id = 1,
        name = "Fiction",
        category = TagCategory.GENRE,
    )

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
        fun `filterChipsByTab is keyed per tab and filterChipsFor returns the matching entry`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val allOptions = LibraryFilterOptions(formats = listOf("ebook"))
            val readOptions = LibraryFilterOptions(readYears = listOf(2021))
            val collector = FilterChipModelsCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = LibraryUiState(filterOptionsByTab = mapOf(
                "all" to allOptions,
                "read" to readOptions,
            ),)

            // ----- Assert -----
            stateFlow.value.filterChipsFor("all").formatChips.map { it.key } shouldBe listOf("format:ebook")
            stateFlow.value.filterChipsFor("read").readYearChips.map { it.key } shouldBe listOf("readYear:2021")
            stateFlow.value.filterChipsFor("missing").isEmpty shouldBe true
            job.cancel()
        }

        @Test
        fun `filterValueByChipKey round-trips every chip key across every facet to the value that built it`() =
            runTest(testDispatcher) {
                // ----- Arrange -----
                val options = LibraryFilterOptions(
                    supportsOwnedFilter = true,
                    formats = listOf("ebook"),
                    releaseYears = listOf(2020),
                    readYears = listOf(2019),
                    tags = listOf(tagFiction),
                    ratingBuckets = listOf(4.0),
                )
                val collector = FilterChipModelsCollector()
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Act -----
                stateFlow.value = LibraryUiState(filterOptionsByTab = mapOf("all" to options))

                // ----- Assert -----
                val chips = stateFlow.value.filterChipsFor("all")
                val valueByKey = stateFlow.value.filterValueByChipKey
                val allChips = chips.ownershipChips + chips.formatChips + chips.releaseYearChips +
                    chips.readYearChips + chips.tagChips + chips.ratingChips

                allChips.forEach { chip -> (chip.key in valueByKey) shouldBe true }
                valueByKey["owned:true"] shouldBe LibraryFilterValue.Owned(owned = true)
                valueByKey["format:ebook"] shouldBe LibraryFilterValue.Format(value = "ebook")
                valueByKey["releaseYear:2020"] shouldBe LibraryFilterValue.ReleaseYear(year = 2020)
                valueByKey["readYear:2019"] shouldBe LibraryFilterValue.ReadYear(year = 2019)
                valueByKey["tag:1"] shouldBe LibraryFilterValue.Tag(tag = tagFiction)
                valueByKey["rating:4.0"] shouldBe LibraryFilterValue.RatingMin(threshold = 4.0)
                job.cancel()
            }

        @Test
        fun `unrelated state change that leaves filterOptionsByTab equal does not retrigger recompute`() =
            runTest(testDispatcher) {
                // ----- Arrange -----
                val options = LibraryFilterOptions(formats = listOf("ebook"))
                val collector = FilterChipModelsCollector()
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                stateFlow.value = LibraryUiState(filterOptionsByTab = mapOf("all" to options))
                val chipsByTabAfterFirstEmit = stateFlow.value.filterChipsByTab
                val valueByKeyAfterFirstEmit = stateFlow.value.filterValueByChipKey

                // ----- Act -----
                // isArrangeSheetExpanded is not part of FilterChipModelsSnapshot, so this must not
                // retrigger the collector's compute — the maps must stay the same instances.
                stateFlow.value = stateFlow.value.copy(isArrangeSheetExpanded = true)

                // ----- Assert -----
                (stateFlow.value.filterChipsByTab === chipsByTabAfterFirstEmit) shouldBe true
                (stateFlow.value.filterValueByChipKey === valueByKeyAfterFirstEmit) shouldBe true
                job.cancel()
            }
    }
}
