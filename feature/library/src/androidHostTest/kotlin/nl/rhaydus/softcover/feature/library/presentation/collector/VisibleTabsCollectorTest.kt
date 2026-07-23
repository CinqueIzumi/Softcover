package nl.rhaydus.softcover.feature.library.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.designsystem.presentation.model.LibraryTab
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.lists.domain.usecase.GetAllUserListsUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetEnabledListIdsAsFlowUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetEnabledStatusCodesAsFlowUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetLibraryTabOrderAsFlowUseCase
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope

class VisibleTabsCollectorTest {
    private lateinit var getEnabledStatusCodesAsFlowUseCase: GetEnabledStatusCodesAsFlowUseCase
    private lateinit var getEnabledListIdsAsFlowUseCase: GetEnabledListIdsAsFlowUseCase
    private lateinit var getAllUserListsUseCase: GetAllUserListsUseCase
    private lateinit var getLibraryTabOrderAsFlowUseCase: GetLibraryTabOrderAsFlowUseCase
    private lateinit var dependencies: LibraryDependencies
    private lateinit var stateFlow: MutableStateFlow<LibraryUiState>
    private lateinit var scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>
    private lateinit var statusCodesFlow: MutableSharedFlow<Set<Int>>
    private lateinit var enabledListIdsFlow: MutableSharedFlow<Set<Int>>
    private lateinit var listsFlow: MutableSharedFlow<List<BookList>>
    private lateinit var tabOrderFlow: MutableSharedFlow<List<String>>

    @BeforeEach
    fun setUp() {
        statusCodesFlow = MutableSharedFlow()
        enabledListIdsFlow = MutableSharedFlow()
        listsFlow = MutableSharedFlow()
        tabOrderFlow = MutableSharedFlow()
        getEnabledStatusCodesAsFlowUseCase = mockk()
        getEnabledListIdsAsFlowUseCase = mockk()
        getAllUserListsUseCase = mockk()
        getLibraryTabOrderAsFlowUseCase = mockk()
        stateFlow = MutableStateFlow(LibraryUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LibraryLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        every {
            getEnabledStatusCodesAsFlowUseCase()
        } returns statusCodesFlow

        every {
            getEnabledListIdsAsFlowUseCase()
        } returns enabledListIdsFlow

        every {
            getAllUserListsUseCase()
        } returns listsFlow

        every {
            getLibraryTabOrderAsFlowUseCase()
        } returns tabOrderFlow

        dependencies = mockk<LibraryDependencies>(relaxed = true).also { mock ->
            every {
                mock.getEnabledStatusCodesAsFlowUseCase
            } returns getEnabledStatusCodesAsFlowUseCase

            every {
                mock.getEnabledListIdsAsFlowUseCase
            } returns getEnabledListIdsAsFlowUseCase

            every {
                mock.getAllUserListsUseCase
            } returns getAllUserListsUseCase

            every {
                mock.getLibraryTabOrderAsFlowUseCase
            } returns getLibraryTabOrderAsFlowUseCase
        }
    }

    private fun stubBookList(
        id: Int,
        name: String,
    ): BookList = mockk {
        every {
            this@mockk.id
        } returns id

        every {
            this@mockk.name
        } returns name
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `always starts with All tab first`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = VisibleTabsCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            statusCodesFlow.emit(emptySet())
            enabledListIdsFlow.emit(emptySet())
            listsFlow.emit(emptyList())
            tabOrderFlow.emit(emptyList())

            // ----- Assert -----
            stateFlow.value.visibleTabs.first() shouldBe LibraryTab.All
            job.cancel()
        }

        @Test
        fun `includes CURRENTLY_READING tab even when not in enabled status set`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = VisibleTabsCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            statusCodesFlow.emit(emptySet())
            enabledListIdsFlow.emit(emptySet())
            listsFlow.emit(emptyList())
            tabOrderFlow.emit(emptyList())

            // ----- Assert -----
            stateFlow.value.visibleTabs.any { it is LibraryTab.Status && it.status == UserBookStatus.CURRENTLY_READING } shouldBe true
            job.cancel()
        }

        @Test
        fun `status tabs appear in canonical order CR WANT_TO_READ READ DNF`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val allStatusCodes = setOf(
                UserBookStatus.WANT_TO_READ.code,
                UserBookStatus.READ.code,
                UserBookStatus.DID_NOT_FINISH.code,
            )
            val collector = VisibleTabsCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            statusCodesFlow.emit(allStatusCodes)
            enabledListIdsFlow.emit(emptySet())
            listsFlow.emit(emptyList())
            tabOrderFlow.emit(emptyList())

            // ----- Assert -----
            val statusTabs = stateFlow.value.visibleTabs.filterIsInstance<LibraryTab.Status>()
            val statusOrder = statusTabs.map { it.status }

            statusOrder shouldBe listOf(
                UserBookStatus.CURRENTLY_READING,
                UserBookStatus.WANT_TO_READ,
                UserBookStatus.READ,
                UserBookStatus.DID_NOT_FINISH,
            )
            job.cancel()
        }

        @Test
        fun `unknown status code in enabled set produces only CURRENTLY_READING tab`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = VisibleTabsCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            // 999 is not a valid UserBookStatus code and must be silently ignored
            statusCodesFlow.emit(setOf(999))
            enabledListIdsFlow.emit(emptySet())
            listsFlow.emit(emptyList())
            tabOrderFlow.emit(emptyList())

            // ----- Assert -----
            val statusTabs = stateFlow.value.visibleTabs.filterIsInstance<LibraryTab.Status>()
            statusTabs.map { it.status } shouldBe listOf(UserBookStatus.CURRENTLY_READING)
            job.cancel()
        }

        @Test
        fun `custom list tabs appear after status tabs sorted by name ascending`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val listZebra = stubBookList(
                id = 1,
                name = "Zebra",
            )
            val listAlpha = stubBookList(
                id = 2,
                name = "Alpha",
            )
            val listMid = stubBookList(
                id = 3,
                name = "Middle",
            )
            val collector = VisibleTabsCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            statusCodesFlow.emit(emptySet())
            enabledListIdsFlow.emit(setOf(1, 2, 3))
            listsFlow.emit(listOf(listZebra, listAlpha, listMid))
            tabOrderFlow.emit(emptyList())

            // ----- Assert -----
            val listTabs = stateFlow.value.visibleTabs.filterIsInstance<LibraryTab.CustomList>()
            listTabs.map { it.listName } shouldBe listOf("Alpha", "Middle", "Zebra")
            job.cancel()
        }

        @Test
        fun `only enabled list ids appear as list tabs`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val enabledList = stubBookList(
                id = 10,
                name = "Owned",
            )
            val disabledList = stubBookList(
                id = 20,
                name = "Wishlist",
            )
            val collector = VisibleTabsCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            statusCodesFlow.emit(emptySet())
            enabledListIdsFlow.emit(setOf(10))
            listsFlow.emit(listOf(enabledList, disabledList))
            tabOrderFlow.emit(emptyList())

            // ----- Assert -----
            val listTabs = stateFlow.value.visibleTabs.filterIsInstance<LibraryTab.CustomList>()
            listTabs.map { it.listId } shouldBe listOf(10)
            job.cancel()
        }

        @Test
        fun `no list tabs when no lists are enabled`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val someList = stubBookList(
                id = 1,
                name = "Owned",
            )
            val collector = VisibleTabsCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            statusCodesFlow.emit(emptySet())
            enabledListIdsFlow.emit(emptySet())
            listsFlow.emit(listOf(someList))
            tabOrderFlow.emit(emptyList())

            // ----- Assert -----
            stateFlow.value.visibleTabs.filterIsInstance<LibraryTab.CustomList>() shouldBe emptyList()
            job.cancel()
        }

        @Test
        fun `visibleTabs does not change before all four flows emit`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val initialTabs = stateFlow.value.visibleTabs
            val collector = VisibleTabsCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act & Assert -----
            stateFlow.value.visibleTabs shouldBe initialTabs
            job.cancel()
        }

        @Test
        fun `tabsLoaded is false before any flows emit and true after first combined emission`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = VisibleTabsCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act & Assert (before emission) -----
            stateFlow.value.tabsLoaded shouldBe false

            statusCodesFlow.emit(emptySet())
            enabledListIdsFlow.emit(emptySet())
            listsFlow.emit(emptyList())
            tabOrderFlow.emit(emptyList())

            // ----- Assert (after emission) -----
            stateFlow.value.tabsLoaded shouldBe true
            job.cancel()
        }

        @Test
        fun `reacts to updated status codes flow`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = VisibleTabsCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            statusCodesFlow.emit(emptySet())
            enabledListIdsFlow.emit(emptySet())
            listsFlow.emit(emptyList())
            tabOrderFlow.emit(emptyList())

            // ----- Act -----
            statusCodesFlow.emit(setOf(UserBookStatus.READ.code))

            // ----- Assert -----
            val statusTabs = stateFlow.value.visibleTabs.filterIsInstance<LibraryTab.Status>()
            statusTabs.any { it.status == UserBookStatus.READ } shouldBe true
            job.cancel()
        }

        @Test
        fun `empty persistedOrder returns default order with All first then statuses then lists by name`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val listB = stubBookList(
                    id = 2,
                    name = "Beta",
                )
                val listA = stubBookList(
                    id = 1,
                    name = "Alpha",
                )
                val collector = VisibleTabsCollector()
                val job = launch { collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                ) }

                // ----- Act -----
                statusCodesFlow.emit(setOf(UserBookStatus.READ.code))
                enabledListIdsFlow.emit(setOf(1, 2))
                listsFlow.emit(listOf(listB, listA))
                tabOrderFlow.emit(emptyList())

                // ----- Assert -----
                val tabs = stateFlow.value.visibleTabs
                tabs[0] shouldBe LibraryTab.All
                (tabs[1] as LibraryTab.Status).status shouldBe UserBookStatus.CURRENTLY_READING
                (tabs[2] as LibraryTab.Status).status shouldBe UserBookStatus.READ
                (tabs[3] as LibraryTab.CustomList).listName shouldBe "Alpha"
                (tabs[4] as LibraryTab.CustomList).listName shouldBe "Beta"
                job.cancel()
            }

        @Test
        fun `persistedOrder with known ids respects that order and keeps All pinned first`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val listA = stubBookList(
                id = 1,
                name = "Alpha",
            )
            val listB = stubBookList(
                id = 2,
                name = "Beta",
            )
            val readId = "status-${UserBookStatus.READ.code}"
            val listAId = "list-1"
            val listBId = "list-2"
            val collector = VisibleTabsCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            statusCodesFlow.emit(setOf(UserBookStatus.READ.code))
            enabledListIdsFlow.emit(setOf(1, 2))
            listsFlow.emit(listOf(listA, listB))
            tabOrderFlow.emit(listOf(listBId, readId, listAId))

            // ----- Assert -----
            val tabs = stateFlow.value.visibleTabs
            tabs[0] shouldBe LibraryTab.All
            (tabs[1] as LibraryTab.CustomList).listId shouldBe 2
            (tabs[2] as LibraryTab.Status).status shouldBe UserBookStatus.READ
            (tabs[3] as LibraryTab.CustomList).listId shouldBe 1
            job.cancel()
        }

        @Test
        fun `persistedOrder with unknown ids ignores them silently`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = VisibleTabsCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            statusCodesFlow.emit(emptySet())
            enabledListIdsFlow.emit(emptySet())
            listsFlow.emit(emptyList())
            tabOrderFlow.emit(listOf("unknown-id-1", "unknown-id-2"))

            // ----- Assert -----
            val tabs = stateFlow.value.visibleTabs
            tabs[0] shouldBe LibraryTab.All
            tabs.none { it.id == "unknown-id-1" } shouldBe true
            tabs.none { it.id == "unknown-id-2" } shouldBe true
            job.cancel()
        }

        @Test
        fun `new tabs not in persistedOrder are appended at the end in default order`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val listA = stubBookList(
                id = 1,
                name = "Alpha",
            )
            val listB = stubBookList(
                id = 2,
                name = "Beta",
            )
            val listBId = "list-2"
            val collector = VisibleTabsCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // persistedOrder only references listB; listA is a new tab not in the order
            // ----- Act -----
            statusCodesFlow.emit(emptySet())
            enabledListIdsFlow.emit(setOf(1, 2))
            listsFlow.emit(listOf(listA, listB))
            tabOrderFlow.emit(listOf(listBId))

            // ----- Assert -----
            val tabs = stateFlow.value.visibleTabs
            tabs[0] shouldBe LibraryTab.All
            val listTabs = tabs.filterIsInstance<LibraryTab.CustomList>()
            listTabs[0].listId shouldBe 2
            listTabs[1].listId shouldBe 1
            job.cancel()
        }
    }
}
