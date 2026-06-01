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
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.ListBook
import nl.rhaydus.softcover.core.lists.domain.usecase.GetAllUserListsUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetEnabledListIdsAsFlowUseCase
import nl.rhaydus.softcover.core.presentation.model.LibraryTab
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BookListsCollectorTest {

    private lateinit var getAllUserListsUseCase: GetAllUserListsUseCase
    private lateinit var getEnabledListIdsAsFlowUseCase: GetEnabledListIdsAsFlowUseCase
    private lateinit var dependencies: LibraryDependencies
    private lateinit var stateFlow: MutableStateFlow<LibraryUiState>
    private lateinit var scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>
    private lateinit var listsFlow: MutableSharedFlow<List<BookList>>
    private lateinit var enabledListIdsFlow: MutableSharedFlow<Set<Int>>

    @BeforeEach
    fun setUp() {
        listsFlow = MutableSharedFlow()
        enabledListIdsFlow = MutableSharedFlow()
        getAllUserListsUseCase = mockk()
        getEnabledListIdsAsFlowUseCase = mockk()
        stateFlow = MutableStateFlow(LibraryUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LibraryLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        every {
            getAllUserListsUseCase()
        } returns listsFlow

        every {
            getEnabledListIdsAsFlowUseCase()
        } returns enabledListIdsFlow

        dependencies = mockk<LibraryDependencies>(relaxed = true).also { mock ->
            every {
                mock.getAllUserListsUseCase
            } returns getAllUserListsUseCase

            every {
                mock.getEnabledListIdsAsFlowUseCase
            } returns getEnabledListIdsAsFlowUseCase
        }
    }

    private fun stubEdition(): BookEdition = mockk {
        every { url } returns "https://example.com/cover.jpg"
        every { localImagePath } returns null
    }

    private fun stubListBook(edition: BookEdition?): ListBook = mockk {
        every { this@mockk.edition } returns edition
        every { this@mockk.editionId } returns 0
        every { this@mockk.addedAt } returns null
        every { this@mockk.book } returns null
    }

    private fun stubBookList(id: Int, name: String, listBooks: List<ListBook>): BookList = mockk {
        every {
            this@mockk.id
        } returns id

        every {
            this@mockk.name
        } returns name

        every {
            books
        } returns listBooks
    }

    @Nested
    inner class OnLaunch {

        @Test
        fun `writes editions of enabled list into editionsByTab keyed by list tab id`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val edition = stubEdition()
            val listBook = stubListBook(edition = edition)
            val list = stubBookList(id = 5, name = "Owned", listBooks = listOf(listBook))
            val tabId = LibraryTab.CustomList(listId = 5, listName = "Owned").id
            val collector = BookListsCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            listsFlow.emit(listOf(list))
            enabledListIdsFlow.emit(setOf(5))

            // ----- Assert -----
            stateFlow.value.editionsByTab[tabId] shouldBe listOf(edition)
            job.cancel()
        }

        @Test
        fun `skips list books with null edition`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val listBookNoEdition = stubListBook(edition = null)
            val listBookWithEdition = stubListBook(edition = stubEdition())
            val list = stubBookList(id = 1, name = "Owned", listBooks = listOf(listBookNoEdition, listBookWithEdition))
            val tabId = LibraryTab.CustomList(listId = 1, listName = "Owned").id
            val collector = BookListsCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            listsFlow.emit(listOf(list))
            enabledListIdsFlow.emit(setOf(1))

            // ----- Assert -----
            stateFlow.value.editionsByTab[tabId]?.size shouldBe 1
            job.cancel()
        }

        @Test
        fun `disabled lists are not written into editionsByTab`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val edition = stubEdition()
            val listBook = stubListBook(edition = edition)
            val disabledList = stubBookList(id = 99, name = "Hidden", listBooks = listOf(listBook))
            val disabledTabId = LibraryTab.CustomList(listId = 99, listName = "Hidden").id
            val collector = BookListsCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            listsFlow.emit(listOf(disabledList))
            enabledListIdsFlow.emit(emptySet())

            // ----- Assert -----
            stateFlow.value.editionsByTab.containsKey(disabledTabId) shouldBe false
            job.cancel()
        }

        @Test
        fun `strips previously enabled list tab when it is disabled on next emission`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val edition = stubEdition()
            val listBook = stubListBook(edition = edition)
            val list = stubBookList(id = 3, name = "Owned", listBooks = listOf(listBook))
            val tabId = LibraryTab.CustomList(listId = 3, listName = "Owned").id
            val collector = BookListsCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            listsFlow.emit(listOf(list))
            enabledListIdsFlow.emit(setOf(3))
            stateFlow.value.editionsByTab.containsKey(tabId) shouldBe true

            // ----- Act -----
            enabledListIdsFlow.emit(emptySet())

            // ----- Assert -----
            stateFlow.value.editionsByTab.containsKey(tabId) shouldBe false
            job.cancel()
        }

        @Test
        fun `editionsByTab is not modified before both flows emit`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = BookListsCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act & Assert -----
            stateFlow.value.editionsByTab shouldBe emptyMap()
            job.cancel()
        }

        @Test
        fun `multiple enabled lists are all written into editionsByTab`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val edition1 = stubEdition()
            val edition2 = stubEdition()
            val list1 = stubBookList(id = 10, name = "List A", listBooks = listOf(stubListBook(edition = edition1)))
            val list2 = stubBookList(id = 11, name = "List B", listBooks = listOf(stubListBook(edition = edition2)))
            val tabId1 = LibraryTab.CustomList(listId = 10, listName = "List A").id
            val tabId2 = LibraryTab.CustomList(listId = 11, listName = "List B").id
            val collector = BookListsCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            listsFlow.emit(listOf(list1, list2))
            enabledListIdsFlow.emit(setOf(10, 11))

            // ----- Assert -----
            stateFlow.value.editionsByTab[tabId1] shouldBe listOf(edition1)
            stateFlow.value.editionsByTab[tabId2] shouldBe listOf(edition2)
            job.cancel()
        }
    }
}
