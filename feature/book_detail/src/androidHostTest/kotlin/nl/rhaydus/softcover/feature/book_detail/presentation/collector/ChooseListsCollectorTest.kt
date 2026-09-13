package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.ListBook
import nl.rhaydus.softcover.core.domain.preview.PreviewData
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

class ChooseListsCollectorTest {
    private lateinit var dependencies: BookDetailDependencies
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        stateFlow = MutableStateFlow(BookDetailUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(BookDetailLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        dependencies = mockk<BookDetailDependencies>(relaxed = true)
    }

    private fun buildList(
        id: Int,
        name: String,
        slug: String,
        bookId: Int,
    ) = BookList(
        id = id,
        name = name,
        slug = slug,
        books = listOf(ListBook(
            listBookId = id,
            listId = id,
            bookId = bookId,
            editionId = 1,
        ),),
    )

    @Nested
    inner class OnLaunch {
        @Test
        fun `chooseListsSheet is populated for a single book once the book and lists have arrived`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val book = PreviewData.baseBook
            val ownedList = buildList(
                id = 1,
                name = "Owned",
                slug = "owned",
                bookId = book.id,
            )
            val customList = buildList(
                id = 2,
                name = "Winter reading",
                slug = "winter-reading",
                bookId = book.id,
            )

            val collector = ChooseListsCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(
                book = book,
                userLists = listOf(ownedList, customList),
            )

            // ----- Assert -----
            stateFlow.value.chooseListsSheet shouldNotBe null
            stateFlow.value.chooseListsSheet?.variant?.name shouldBe book.title
            job.cancel()
        }

        @Test
        fun `the built-in Owned list is excluded from rows`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val book = PreviewData.baseBook
            val ownedList = buildList(
                id = 1,
                name = "Owned",
                slug = "owned",
                bookId = book.id,
            )
            val customList = buildList(
                id = 2,
                name = "Winter reading",
                slug = "winter-reading",
                bookId = book.id,
            )

            val collector = ChooseListsCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(
                book = book,
                userLists = listOf(ownedList, customList),
            )

            // ----- Assert -----
            val rowIds = stateFlow.value.chooseListsSheet?.rows.orEmpty().map { it.listId }
            rowIds shouldNotContain ownedList.id
            rowIds shouldBe listOf(customList.id)
            job.cancel()
        }

        @Test
        fun `chooseListsSheet stays null while book has not loaded`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val customList = buildList(
                id = 2,
                name = "Winter reading",
                slug = "winter-reading",
                bookId = 1,
            )

            val collector = ChooseListsCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(userLists = listOf(customList))

            // ----- Assert -----
            stateFlow.value.chooseListsSheet shouldBe null
            job.cancel()
        }

        @Test
        fun `listsBeingMutated marks the matching row as pending`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val book = PreviewData.baseBook
            val customList = buildList(
                id = 2,
                name = "Winter reading",
                slug = "winter-reading",
                bookId = book.id,
            )

            val collector = ChooseListsCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            stateFlow.value = stateFlow.value.copy(
                book = book,
                userLists = listOf(customList),
            )

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(listsBeingMutated = setOf(customList.id))

            // ----- Assert -----
            stateFlow.value.chooseListsSheet?.rows?.first { it.listId == customList.id }?.isPending shouldBe true
            job.cancel()
        }
    }
}
