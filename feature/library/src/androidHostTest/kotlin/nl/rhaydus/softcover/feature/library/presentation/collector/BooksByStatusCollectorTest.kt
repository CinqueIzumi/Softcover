package nl.rhaydus.softcover.feature.library.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.book.domain.usecase.GetSortedBooksByStatusUseCase
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.domain.model.LibrarySortSettings
import nl.rhaydus.softcover.core.domain.model.SortDirection
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetEnabledStatusCodesAsFlowUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetLibrarySortSettingsAsFlowUseCase
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BooksByStatusCollectorTest {
    private lateinit var settingsFlow: MutableStateFlow<Map<String, LibrarySortSettings>>
    private lateinit var enabledStatusesFlow: MutableStateFlow<Set<Int>>
    private lateinit var booksByStatus: Map<UserBookStatus, MutableStateFlow<List<Book>>>
    private lateinit var getSortedBooksByStatusUseCase: GetSortedBooksByStatusUseCase
    private lateinit var dependencies: LibraryDependencies
    private lateinit var stateFlow: MutableStateFlow<LibraryUiState>
    private lateinit var scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>

    private val readTabId = "status-3"
    private val wantToReadTabId = "status-1"
    private val readingTabId = "status-2"
    private val pausedTabId = "status-4"

    @BeforeEach
    fun setUp() {
        settingsFlow = MutableStateFlow(emptyMap())
        enabledStatusesFlow = MutableStateFlow(UserBookStatus.entries.map { it.code }.toSet())
        booksByStatus = UserBookStatus.entries.associateWith { MutableStateFlow(emptyList()) }
        stateFlow = MutableStateFlow(LibraryUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LibraryLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        getSortedBooksByStatusUseCase = mockk()

        every {
            getSortedBooksByStatusUseCase(
                status = any(),
                mode = any(),
                direction = any(),
            )
        } answers {
            booksByStatus.getValue(firstArg<UserBookStatus>()) as Flow<List<Book>>
        }

        dependencies = mockk<LibraryDependencies>(relaxed = true).also { mock ->
            every {
                mock.getLibrarySortSettingsAsFlowUseCase
            } returns mockk<GetLibrarySortSettingsAsFlowUseCase> {
                every {
                    this@mockk()
                } returns settingsFlow
            }

            every {
                mock.getEnabledStatusCodesAsFlowUseCase
            } returns mockk<GetEnabledStatusCodesAsFlowUseCase> {
                every {
                    this@mockk()
                } returns enabledStatusesFlow
            }

            every {
                mock.getSortedBooksByStatusUseCase
            } returns getSortedBooksByStatusUseCase
        }
    }

    private fun TestScope.launchCollector() = launch {
        BooksByStatusCollector().onLaunch(
            scope = scope,
            dependencies = dependencies,
        )
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `groups each status's books under its own tab`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val readBook = mockk<Book>()
            val wantToReadBook = mockk<Book>()
            val job = launchCollector()

            // ----- Act -----
            booksByStatus.getValue(UserBookStatus.READ).value = listOf(readBook)
            booksByStatus.getValue(UserBookStatus.WANT_TO_READ).value = listOf(wantToReadBook)

            // ----- Assert -----
            stateFlow.value.booksByTab.getValue(readTabId) shouldBe listOf(readBook)
            stateFlow.value.booksByTab.getValue(wantToReadTabId) shouldBe listOf(wantToReadBook)
            job.cancel()
        }

        @Test
        fun `the paused status gets its own tab when enabled`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val pausedBook = mockk<Book>()
            val job = launchCollector()

            // ----- Act -----
            booksByStatus.getValue(UserBookStatus.PAUSED).value = listOf(pausedBook)

            // ----- Assert -----
            stateFlow.value.booksByTab.getValue(pausedTabId) shouldBe listOf(pausedBook)
            job.cancel()
        }

        @Test
        fun `the paused tab disappears when its status is disabled`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val job = launchCollector()

            // ----- Act -----
            enabledStatusesFlow.value = setOf(UserBookStatus.READ.code)

            // ----- Assert -----
            stateFlow.value.booksByTab.keys.contains(pausedTabId) shouldBe false
            job.cancel()
        }

        @Test
        fun `a disabled status gets no tab but currently-reading is always kept`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            enabledStatusesFlow.value = emptySet()

            // ----- Act -----
            val job = launchCollector()

            // ----- Assert -----
            stateFlow.value.booksByTab.keys shouldBe setOf(readingTabId)
            job.cancel()
        }

        @Test
        fun `disabling a status removes its tab and leaves non-status tabs alone`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val allBook = mockk<Book>()
            stateFlow.value = LibraryUiState(booksByTab = persistentMapOf("all" to persistentListOf(allBook)))
            val job = launchCollector()

            // ----- Act -----
            enabledStatusesFlow.value = setOf(UserBookStatus.WANT_TO_READ.code)

            // ----- Assert -----
            stateFlow.value.booksByTab.keys shouldBe setOf("all", wantToReadTabId, readingTabId)
            stateFlow.value.booksByTab.getValue("all") shouldBe listOf(allBook)
            job.cancel()
        }

        @Test
        fun `a persisted sort applies only to its own status tab`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val job = launchCollector()

            // ----- Act -----
            settingsFlow.value = mapOf(
                readTabId to LibrarySortSettings(
                    mode = LibrarySortMode.TITLE,
                    direction = SortDirection.DESCENDING,
                ),
            )

            // ----- Assert -----
            verify {
                getSortedBooksByStatusUseCase(
                    status = UserBookStatus.READ,
                    mode = LibrarySortMode.TITLE,
                    direction = SortDirection.DESCENDING,
                )
            }
            verify(exactly = 0) {
                getSortedBooksByStatusUseCase(
                    status = UserBookStatus.WANT_TO_READ,
                    mode = LibrarySortMode.TITLE,
                    direction = any(),
                )
            }
            job.cancel()
        }
    }
}
