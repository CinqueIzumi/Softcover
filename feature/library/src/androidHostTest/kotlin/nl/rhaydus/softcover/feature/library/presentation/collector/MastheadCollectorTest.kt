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
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryTabStats
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class MastheadCollectorTest {
    private lateinit var dependencies: LibraryDependencies
    private lateinit var stateFlow: MutableStateFlow<LibraryUiState>
    private lateinit var scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>

    private val readTab = LibraryTab.Status.of(UserBookStatus.READ)
    private val wantToReadTab = LibraryTab.Status.of(UserBookStatus.WANT_TO_READ)
    private val currentlyReadingTab = LibraryTab.Status.of(UserBookStatus.CURRENTLY_READING)
    private val customListTab = LibraryTab.CustomList(
        listId = 1,
        listName = "Owned",
    )

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
        fun `emits one masthead entry per visible tab titled after the tab's label`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(
                visibleTabs = listOf(LibraryTab.All, readTab),
                tabStatsByTab = mapOf(
                    LibraryTab.All.id to LibraryTabStats(
                        itemCount = 3,
                        totalPages = 0,
                    ),
                    readTab.id to LibraryTabStats(
                        itemCount = 2,
                        totalPages = 0,
                    ),
                ),
            )
            val collector = MastheadCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.mastheads.keys shouldBe setOf(LibraryTab.All.id, readTab.id)
            stateFlow.value.mastheads.getValue(LibraryTab.All.id).title shouldBe "All"
            stateFlow.value.mastheads.getValue(readTab.id).title shouldBe "Read"
            job.cancel()
        }

        @Test
        fun `a visible tab absent from tabStatsByTab gets the count-shaped stand-in subtitle`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(
                visibleTabs = listOf(LibraryTab.All),
                tabStatsByTab = emptyMap(),
            )
            val collector = MastheadCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.mastheads.getValue(LibraryTab.All.id).subtitle shouldBe MASTHEAD_SUBTITLE_STAND_IN
            job.cancel()
        }

        @Test
        fun `zero count on the Read tab produces the finished-shelf empty message`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                stateFlow.value = LibraryUiState(
                    visibleTabs = listOf(readTab),
                    tabStatsByTab = mapOf(
                        readTab.id to LibraryTabStats(
                            itemCount = 0,
                            totalPages = 0,
                        ),
                    ),
                )
                val collector = MastheadCollector()

                // ----- Act -----
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Assert -----
                stateFlow.value.mastheads.getValue(readTab.id).subtitle shouldBe "Nothing finished yet — the page is still open."
                job.cancel()
            }

        @Test
        fun `zero count on the Want to Read tab produces the wishlist empty message`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                stateFlow.value = LibraryUiState(
                    visibleTabs = listOf(wantToReadTab),
                    tabStatsByTab = mapOf(
                        wantToReadTab.id to LibraryTabStats(
                            itemCount = 0,
                            totalPages = 0,
                        ),
                    ),
                )
                val collector = MastheadCollector()

                // ----- Act -----
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Assert -----
                stateFlow.value.mastheads.getValue(wantToReadTab.id).subtitle shouldBe "No titles set aside — discover one next."
                job.cancel()
            }

        @Test
        fun `zero count on the Currently Reading tab produces the open-book empty message`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                stateFlow.value = LibraryUiState(
                    visibleTabs = listOf(currentlyReadingTab),
                    tabStatsByTab = mapOf(
                        currentlyReadingTab.id to LibraryTabStats(
                            itemCount = 0,
                            totalPages = 0,
                        ),
                    ),
                )
                val collector = MastheadCollector()

                // ----- Act -----
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Assert -----
                stateFlow.value.mastheads.getValue(currentlyReadingTab.id).subtitle shouldBe "No book open — pick one up."
                job.cancel()
            }

        @Test
        fun `zero count on a non-status tab produces the generic empty message`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                stateFlow.value = LibraryUiState(
                    visibleTabs = listOf(customListTab),
                    tabStatsByTab = mapOf(
                        customListTab.id to LibraryTabStats(
                            itemCount = 0,
                            totalPages = 0,
                        ),
                    ),
                )
                val collector = MastheadCollector()

                // ----- Act -----
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Assert -----
                stateFlow.value.mastheads.getValue(customListTab.id).subtitle shouldBe "No titles yet — your story starts here."
                job.cancel()
            }

        @Test
        fun `formatted count includes the page count when totalPages is positive`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                stateFlow.value = LibraryUiState(
                    visibleTabs = listOf(readTab),
                    tabStatsByTab = mapOf(
                        readTab.id to LibraryTabStats(
                            itemCount = 5,
                            totalPages = 250,
                        ),
                    ),
                )
                val collector = MastheadCollector()

                // ----- Act -----
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Assert -----
                stateFlow.value.mastheads.getValue(readTab.id).subtitle shouldBe "5 titles · 250 pages"
                job.cancel()
            }

        @Test
        fun `formatted count omits the page count when totalPages is zero`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                stateFlow.value = LibraryUiState(
                    visibleTabs = listOf(readTab),
                    tabStatsByTab = mapOf(
                        readTab.id to LibraryTabStats(
                            itemCount = 5,
                            totalPages = 0,
                        ),
                    ),
                )
                val collector = MastheadCollector()

                // ----- Act -----
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Assert -----
                stateFlow.value.mastheads.getValue(readTab.id).subtitle shouldBe "5 titles"
                job.cancel()
            }

        @Test
        fun `fallbackMasthead subtitle is the count-shaped stand-in regardless of loading state`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                stateFlow.value = LibraryUiState(
                    visibleTabs = listOf(readTab),
                    tabStatsByTab = mapOf(
                        readTab.id to LibraryTabStats(
                            itemCount = 5,
                            totalPages = 250,
                        ),
                    ),
                    isLoading = false,
                )
                val collector = MastheadCollector()

                // ----- Act -----
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Assert -----
                stateFlow.value.fallbackMasthead.subtitle shouldBe MASTHEAD_SUBTITLE_STAND_IN
                job.cancel()
            }

        @Test
        fun `only the visible tabs whose stats have not landed are loading`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                stateFlow.value = LibraryUiState(
                    visibleTabs = listOf(LibraryTab.All, readTab, wantToReadTab),
                    tabStatsByTab = mapOf(
                        readTab.id to LibraryTabStats(
                            itemCount = 1,
                            totalPages = 0,
                        ),
                    ),
                )
                val collector = MastheadCollector()

                // ----- Act -----
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Assert -----
                stateFlow.value.mastheads.mapValues { it.value.isLoading } shouldBe mapOf(
                    LibraryTab.All.id to true,
                    readTab.id to false,
                    wantToReadTab.id to true,
                )
                job.cancel()
            }

        @Test
        fun `a zero-item tab whose stats landed is not loading and shows its empty message`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                stateFlow.value = LibraryUiState(
                    visibleTabs = listOf(readTab),
                    tabStatsByTab = mapOf(
                        readTab.id to LibraryTabStats(
                            itemCount = 0,
                            totalPages = 0,
                        ),
                    ),
                )
                val collector = MastheadCollector()

                // ----- Act -----
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Assert -----
                stateFlow.value.mastheads.getValue(readTab.id).isLoading shouldBe false
                stateFlow.value.mastheads.getValue(readTab.id).subtitle shouldBe "Nothing finished yet — the page is still open."
                job.cancel()
            }

        @Test
        fun `a tab stops loading once its stats land`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(
                visibleTabs = listOf(readTab),
                tabStatsByTab = emptyMap(),
            )
            val collector = MastheadCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }
            stateFlow.value.mastheads.getValue(readTab.id).isLoading shouldBe true

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(
                tabStatsByTab = mapOf(
                    readTab.id to LibraryTabStats(
                        itemCount = 4,
                        totalPages = 0,
                    ),
                ),
            )

            // ----- Assert -----
            stateFlow.value.mastheads.getValue(readTab.id).isLoading shouldBe false
            stateFlow.value.mastheads.getValue(readTab.id).subtitle shouldBe "4 titles"
            job.cancel()
        }

        @Test
        fun `isLoading true sets the refreshing eyebrow and a disabled Refreshing action`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                stateFlow.value = LibraryUiState(
                    visibleTabs = listOf(readTab),
                    tabStatsByTab = mapOf(
                        readTab.id to LibraryTabStats(
                            itemCount = 5,
                            totalPages = 0,
                        ),
                    ),
                    isLoading = true,
                )
                val collector = MastheadCollector()

                // ----- Act -----
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Assert -----
                stateFlow.value.mastheads.getValue(readTab.id).eyebrow shouldBe "Refreshing your shelf…"
                stateFlow.value.fallbackMasthead.eyebrow shouldBe "Refreshing your shelf…"
                stateFlow.value.mastheadRefresh.label shouldBe "Refreshing…"
                stateFlow.value.mastheadRefresh.enabled shouldBe false
                job.cancel()
            }

        @Test
        fun `isLoading false sets the collection eyebrow and an enabled Refresh action`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                stateFlow.value = LibraryUiState(
                    visibleTabs = listOf(readTab),
                    tabStatsByTab = mapOf(
                        readTab.id to LibraryTabStats(
                            itemCount = 5,
                            totalPages = 0,
                        ),
                    ),
                    isLoading = false,
                )
                val collector = MastheadCollector()

                // ----- Act -----
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Assert -----
                stateFlow.value.mastheads.getValue(readTab.id).eyebrow shouldBe "Your collection"
                stateFlow.value.fallbackMasthead.eyebrow shouldBe "Your collection"
                stateFlow.value.mastheadRefresh.label shouldBe "Refresh"
                stateFlow.value.mastheadRefresh.enabled shouldBe true
                job.cancel()
            }

        @Test
        fun `recomputes the tab's masthead when its stats change`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(
                visibleTabs = listOf(readTab),
                tabStatsByTab = mapOf(
                    readTab.id to LibraryTabStats(
                        itemCount = 0,
                        totalPages = 0,
                    ),
                ),
            )
            val collector = MastheadCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }
            stateFlow.value.mastheads.getValue(readTab.id).subtitle shouldBe "Nothing finished yet — the page is still open."

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(
                tabStatsByTab = mapOf(
                    readTab.id to LibraryTabStats(
                        itemCount = 4,
                        totalPages = 0,
                    ),
                ),
            )

            // ----- Assert -----
            stateFlow.value.mastheads.getValue(readTab.id).subtitle shouldBe "4 titles"
            job.cancel()
        }
    }
}
