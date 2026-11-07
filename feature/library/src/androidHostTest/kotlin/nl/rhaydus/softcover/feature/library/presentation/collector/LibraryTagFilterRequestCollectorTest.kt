package nl.rhaydus.softcover.feature.library.presentation.collector

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.presentation.model.LibraryTab
import nl.rhaydus.softcover.core.presentation.navigation.LibraryFilterRequests
import nl.rhaydus.softcover.core.presentation.navigation.LibraryTagFilterRequest
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilterDraft
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilters
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.softcover.feature.library.presentation.state.TagSelection
import nl.rhaydus.softcover.feature.library.presentation.state.UserTagKey
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class LibraryTagFilterRequestCollectorTest {
    private val readTab = LibraryTab.Status.of(UserBookStatus.READ)
    private val readingTab = LibraryTab.Status.of(UserBookStatus.CURRENTLY_READING)
    private val request = LibraryTagFilterRequest(
        category = TagCategory.GENRE,
        name = "Fantasy",
    )
    private val expectedKey = UserTagKey(
        category = TagCategory.GENRE,
        name = "Fantasy",
    )

    private lateinit var requests: LibraryFilterRequests
    private lateinit var dependencies: LibraryDependencies
    private lateinit var stateFlow: MutableStateFlow<LibraryUiState>
    private lateinit var scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>

    @BeforeEach
    fun setUp() {
        requests = LibraryFilterRequests()

        stateFlow = MutableStateFlow(
            LibraryUiState(
                visibleTabs = persistentListOf(LibraryTab.All, readingTab, readTab),
                tabsLoaded = true,
            ),
        )

        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LibraryLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        dependencies = mockk<LibraryDependencies>(relaxed = true).also { mock ->
            every {
                mock.libraryFilterRequests
            } returns requests
        }
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `nothing is applied before the tabs are loaded`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = stateFlow.value.copy(tabsLoaded = false)
            requests.request(request = request)

            val job = launch {
                LibraryTagFilterRequestCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            val before = stateFlow.value

            // ----- Assert -----
            before.filtersByTab shouldBe persistentMapOf()
            requests.pending.value shouldBe request
            job.cancel()
        }

        @Test
        fun `a request made before the tabs load is applied once they load`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = stateFlow.value.copy(tabsLoaded = false)
            requests.request(request = request)

            val job = launch {
                LibraryTagFilterRequestCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(tabsLoaded = true)

            // ----- Assert -----
            stateFlow.value.selectedTabId shouldBe readTab.id
            requests.pending.value.shouldBeNull()
            job.cancel()
        }

        @Test
        fun `with the Read tab visible it selects Read and filters to only the requested tag`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val job = launch {
                LibraryTagFilterRequestCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            requests.request(request = request)

            // ----- Assert -----
            stateFlow.value.selectedTabId shouldBe readTab.id

            stateFlow.value.filtersByTab[readTab.id] shouldBe LibraryFilters(
                yourTags = TagSelection(included = persistentSetOf(expectedKey)),
            )

            job.cancel()
        }

        @Test
        fun `it replaces a filter already set on the Read tab`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val existing = LibraryFilters(
                yourTags = TagSelection(
                    included = persistentSetOf(
                        UserTagKey(
                            category = TagCategory.MOOD,
                            name = "Cozy",
                        ),
                    ),
                ),
                formats = persistentSetOf("Audiobook"),
                ratingMin = 4.0,
            )

            stateFlow.value = stateFlow.value.copy(filtersByTab = persistentMapOf(readTab.id to existing))

            val job = launch {
                LibraryTagFilterRequestCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            requests.request(request = request)

            // ----- Assert -----
            stateFlow.value.filtersByTab[readTab.id] shouldBe LibraryFilters(
                yourTags = TagSelection(included = persistentSetOf(expectedKey)),
            )

            job.cancel()
        }

        @Test
        fun `with the Read tab hidden it lands on All`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = stateFlow.value.copy(visibleTabs = persistentListOf(LibraryTab.All, readingTab))

            val job = launch {
                LibraryTagFilterRequestCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            requests.request(request = request)

            // ----- Assert -----
            stateFlow.value.selectedTabId shouldBe LibraryTab.All.id

            stateFlow.value.filtersByTab[LibraryTab.All.id] shouldBe LibraryFilters(
                yourTags = TagSelection(included = persistentSetOf(expectedKey)),
            )

            job.cancel()
        }

        @Test
        fun `it resets the draft, sheet, search and selection and consumes the request`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = stateFlow.value.copy(
                filterDraft = LibraryFilterDraft(tabId = readingTab.id),
                isFilterSheetExpanded = true,
                searchQuery = "dune",
                selectionMode = true,
                selectedBookIds = persistentSetOf(1, 2),
                isRearranging = true,
            )

            val job = launch {
                LibraryTagFilterRequestCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            requests.request(request = request)

            // ----- Assert -----
            val state = stateFlow.value
            state.filterDraft.shouldBeNull()
            state.isFilterSheetExpanded shouldBe false
            state.searchQuery shouldBe ""
            state.selectionMode shouldBe false
            state.selectedBookIds shouldBe persistentSetOf()
            state.isRearranging shouldBe false
            requests.pending.value.shouldBeNull()
            job.cancel()
        }
    }
}
