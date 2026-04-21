package nl.rhaydus.softcover.feature.library.presentation.flows

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.books.domain.usecase.GetAllUserListsUseCase
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.model.LibraryTab
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetEnabledListIdsAsFlowUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetEnabledStatusCodesAsFlowUseCase
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class VisibleTabsCollectorTest {

    private lateinit var getEnabledStatusCodesAsFlowUseCase: GetEnabledStatusCodesAsFlowUseCase
    private lateinit var getEnabledListIdsAsFlowUseCase: GetEnabledListIdsAsFlowUseCase
    private lateinit var getAllUserListsUseCase: GetAllUserListsUseCase
    private lateinit var dependencies: LibraryDependencies
    private lateinit var stateFlow: MutableStateFlow<LibraryUiState>
    private lateinit var scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>
    private lateinit var statusCodesFlow: MutableSharedFlow<Set<Int>>
    private lateinit var enabledListIdsFlow: MutableSharedFlow<Set<Int>>
    private lateinit var listsFlow: MutableSharedFlow<List<BookList>>

    @BeforeEach
    fun setUp() {
        statusCodesFlow = MutableSharedFlow()
        enabledListIdsFlow = MutableSharedFlow()
        listsFlow = MutableSharedFlow()
        getEnabledStatusCodesAsFlowUseCase = mockk()
        getEnabledListIdsAsFlowUseCase = mockk()
        getAllUserListsUseCase = mockk()
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
        }
    }

    private fun stubBookList(id: Int, name: String): BookList = mockk {
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
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            statusCodesFlow.emit(emptySet())
            enabledListIdsFlow.emit(emptySet())
            listsFlow.emit(emptyList())

            // ----- Assert -----
            stateFlow.value.visibleTabs.first() shouldBe LibraryTab.All
            job.cancel()
        }

        @Test
        fun `includes CURRENTLY_READING tab even when not in enabled status set`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = VisibleTabsCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            statusCodesFlow.emit(emptySet())
            enabledListIdsFlow.emit(emptySet())
            listsFlow.emit(emptyList())

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
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            statusCodesFlow.emit(allStatusCodes)
            enabledListIdsFlow.emit(emptySet())
            listsFlow.emit(emptyList())

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
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            // 999 is not a valid UserBookStatus code and must be silently ignored
            statusCodesFlow.emit(setOf(999))
            enabledListIdsFlow.emit(emptySet())
            listsFlow.emit(emptyList())

            // ----- Assert -----
            val statusTabs = stateFlow.value.visibleTabs.filterIsInstance<LibraryTab.Status>()
            statusTabs.map { it.status } shouldBe listOf(UserBookStatus.CURRENTLY_READING)
            job.cancel()
        }

        @Test
        fun `custom list tabs appear after status tabs sorted by name ascending`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val listZebra = stubBookList(id = 1, name = "Zebra")
            val listAlpha = stubBookList(id = 2, name = "Alpha")
            val listMid = stubBookList(id = 3, name = "Middle")
            val collector = VisibleTabsCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            statusCodesFlow.emit(emptySet())
            enabledListIdsFlow.emit(setOf(1, 2, 3))
            listsFlow.emit(listOf(listZebra, listAlpha, listMid))

            // ----- Assert -----
            val listTabs = stateFlow.value.visibleTabs.filterIsInstance<LibraryTab.CustomList>()
            listTabs.map { it.listName } shouldBe listOf("Alpha", "Middle", "Zebra")
            job.cancel()
        }

        @Test
        fun `only enabled list ids appear as list tabs`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val enabledList = stubBookList(id = 10, name = "Owned")
            val disabledList = stubBookList(id = 20, name = "Wishlist")
            val collector = VisibleTabsCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            statusCodesFlow.emit(emptySet())
            enabledListIdsFlow.emit(setOf(10))
            listsFlow.emit(listOf(enabledList, disabledList))

            // ----- Assert -----
            val listTabs = stateFlow.value.visibleTabs.filterIsInstance<LibraryTab.CustomList>()
            listTabs.map { it.listId } shouldBe listOf(10)
            job.cancel()
        }

        @Test
        fun `no list tabs when no lists are enabled`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val someList = stubBookList(id = 1, name = "Owned")
            val collector = VisibleTabsCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            statusCodesFlow.emit(emptySet())
            enabledListIdsFlow.emit(emptySet())
            listsFlow.emit(listOf(someList))

            // ----- Assert -----
            stateFlow.value.visibleTabs.filterIsInstance<LibraryTab.CustomList>() shouldBe emptyList()
            job.cancel()
        }

        @Test
        fun `visibleTabs does not change before all three flows emit`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val initialTabs = stateFlow.value.visibleTabs
            val collector = VisibleTabsCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act & Assert -----
            stateFlow.value.visibleTabs shouldBe initialTabs
            job.cancel()
        }

        @Test
        fun `reacts to updated status codes flow`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = VisibleTabsCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            statusCodesFlow.emit(emptySet())
            enabledListIdsFlow.emit(emptySet())
            listsFlow.emit(emptyList())

            // ----- Act -----
            statusCodesFlow.emit(setOf(UserBookStatus.READ.code))

            // ----- Assert -----
            val statusTabs = stateFlow.value.visibleTabs.filterIsInstance<LibraryTab.Status>()
            statusTabs.any { it.status == UserBookStatus.READ } shouldBe true
            job.cancel()
        }
    }
}
