package nl.rhaydus.softcover.feature.library.presentation.collector

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.header.PageMastheadSubtitle
import nl.rhaydus.softcover.core.component.statistic.StatNumberFormat
import nl.rhaydus.softcover.core.component.statistic.StatNumberSuffix
import nl.rhaydus.softcover.core.component.statistic.StatNumberUiModel
import nl.rhaydus.softcover.core.component.statistic.StatNumberVariant
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
                visibleTabs = persistentListOf(LibraryTab.All, readTab),
                tabStatsByTab = persistentMapOf(
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
        fun `zero count produces an empty message chosen by the tab type`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val emptyStats = LibraryTabStats(
                itemCount = 0,
                totalPages = 0,
            )
            val tabs = listOf(readTab, wantToReadTab, currentlyReadingTab, customListTab)
            stateFlow.value = LibraryUiState(
                visibleTabs = tabs.toImmutableList(),
                tabStatsByTab = (tabs.associate { it.id to emptyStats }).toImmutableMap(),
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
            val mastheads = stateFlow.value.mastheads
            mastheads.mapValues { it.value.subtitle } shouldBe mapOf(
                readTab.id to PageMastheadSubtitle.Text("Nothing finished yet — the page is still open."),
                wantToReadTab.id to PageMastheadSubtitle.Text("No titles set aside — discover one next."),
                currentlyReadingTab.id to PageMastheadSubtitle.Text("No book open — pick one up."),
                customListTab.id to PageMastheadSubtitle.Text("No titles yet — your story starts here."),
            )
            mastheads.values.none { it.isLoading } shouldBe true
            job.cancel()
        }

        @Test
        fun `formatted count includes the page count when totalPages is positive`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                stateFlow.value = LibraryUiState(
                    visibleTabs = persistentListOf(readTab),
                    tabStatsByTab = persistentMapOf(
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
                val counts = stateFlow.value.mastheads.getValue(readTab.id).subtitle as PageMastheadSubtitle.Counts
                counts.titles shouldBe StatNumberUiModel(
                    value = 5.0,
                    format = StatNumberFormat.Plain,
                    variant = StatNumberVariant.Body,
                    suffix = StatNumberSuffix.Plural(
                        one = " title",
                        other = " titles",
                    ),
                )
                counts.pages shouldBe StatNumberUiModel(
                    value = 250.0,
                    variant = StatNumberVariant.Body,
                    prefix = " · ",
                    suffix = StatNumberSuffix.Plural(
                        one = " page",
                        other = " pages",
                    ),
                )
                job.cancel()
            }

        @Test
        fun `formatted count omits the page count when totalPages is zero`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                stateFlow.value = LibraryUiState(
                    visibleTabs = persistentListOf(readTab),
                    tabStatsByTab = persistentMapOf(
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
                val counts = stateFlow.value.mastheads.getValue(readTab.id).subtitle as PageMastheadSubtitle.Counts
                counts.titles.value shouldBe 5.0
                counts.pages.shouldBeNull()
                job.cancel()
            }

        @Test
        fun `a tab whose stats have not landed shows the loading stand-in counts`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                stateFlow.value = LibraryUiState(visibleTabs = persistentListOf(readTab))
                val collector = MastheadCollector()

                // ----- Act -----
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Assert -----
                val counts = stateFlow.value.mastheads.getValue(readTab.id).subtitle as PageMastheadSubtitle.Counts
                counts.titles.value shouldBe 128.0
                counts.titles.isLoading shouldBe true
                counts.pages.shouldNotBeNull().value shouldBe 34_000.0
                counts.pages.shouldNotBeNull().isLoading shouldBe true
                job.cancel()
            }

        @Test
        fun `only the visible tabs whose stats have not landed are loading`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                stateFlow.value = LibraryUiState(
                    visibleTabs = persistentListOf(LibraryTab.All, readTab, wantToReadTab),
                    tabStatsByTab = persistentMapOf(
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
        fun `a tab stops loading once its stats land`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(
                visibleTabs = persistentListOf(readTab),
                tabStatsByTab = persistentMapOf(),
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
                tabStatsByTab = persistentMapOf(
                    readTab.id to LibraryTabStats(
                        itemCount = 4,
                        totalPages = 0,
                    ),
                ),
            )

            // ----- Assert -----
            stateFlow.value.mastheads.getValue(readTab.id).isLoading shouldBe false
            val counts = stateFlow.value.mastheads.getValue(readTab.id).subtitle as PageMastheadSubtitle.Counts
            counts.titles.value shouldBe 4.0
            counts.titles.isLoading shouldBe false
            job.cancel()
        }

        @Test
        fun `the refresh action is disabled and the eyebrow changes only while loading`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                stateFlow.value = LibraryUiState(
                    visibleTabs = persistentListOf(readTab),
                    isLoading = true,
                )
                val collector = MastheadCollector()
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }
                val loadingEyebrow = stateFlow.value.mastheads.getValue(readTab.id).eyebrow
                stateFlow.value.mastheadRefresh.enabled shouldBe false

                // ----- Act -----
                stateFlow.value = stateFlow.value.copy(isLoading = false)

                // ----- Assert -----
                stateFlow.value.mastheadRefresh.enabled shouldBe true
                stateFlow.value.mastheads.getValue(readTab.id).eyebrow shouldNotBe loadingEyebrow
                job.cancel()
            }
    }
}
