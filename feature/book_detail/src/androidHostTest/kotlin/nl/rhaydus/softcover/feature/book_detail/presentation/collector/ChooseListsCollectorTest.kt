package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.cover.CoverSource
import nl.rhaydus.softcover.core.component.lists.ListMembership
import nl.rhaydus.softcover.core.component.sheet.SheetHeaderTrailing
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.ListBook
import nl.rhaydus.softcover.core.domain.preview.PreviewData
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

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
        books = listOf(
            ListBook(
                listBookId = id,
                listId = id,
                bookId = bookId,
                editionId = 1,
            ),
        ),
    )

    @Nested
    inner class OnLaunch {
        @Test
        fun `a book with no editions takes its cover from the default edition`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val defaultEdition = PreviewData.baseEdition.copy(
                id = 42,
                url = "https://example.com/default.jpg",
                localImagePath = null,
            )
            val book = PreviewData.baseBook.copy(
                editions = emptyList(),
                defaultEdition = defaultEdition,
                coverUrl = "https://example.com/fallback.jpg",
                userBook = null,
            )
            val collector = ChooseListsCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(
                book = book,
                userLists = listOf(
                    buildList(
                        id = 2,
                        name = "Winter reading",
                        slug = "winter-reading",
                        bookId = book.id,
                    ),
                ),
            )

            // ----- Assert -----
            val trailing = stateFlow.value.chooseListsSheet?.scaffold?.header?.trailing
                .shouldBeInstanceOf<SheetHeaderTrailing.Jacket>()
            trailing.cover.source shouldBe CoverSource.Remote(
                url = "https://example.com/default.jpg",
                persistEditionId = 42,
            )
            job.cancel()
        }

        @Test
        fun `titles the sheet after the book and leaves the built-in Owned list out of the rows`() = runTest(UnconfinedTestDispatcher()) {
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
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(
                book = book,
                userLists = listOf(ownedList, customList),
            )

            // ----- Assert -----
            val rowIds = stateFlow.value.chooseListsSheet?.rows.orEmpty().map { it.listId }
            rowIds shouldNotContain ownedList.id
            rowIds shouldBe listOf(customList.id)
            stateFlow.value.chooseListsSheet?.scaffold?.header?.section?.headlineText shouldBe "Shelve ${book.title}"
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
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

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
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

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

        @Test
        fun `a pending list keeps its pre-write row while its data changes underneath`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val book = PreviewData.baseBook
            val onList = buildList(
                id = 2,
                name = "Winter reading",
                slug = "winter-reading",
                bookId = book.id,
            )
            val offList = onList.copy(books = emptyList())

            val collector = ChooseListsCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            stateFlow.value = stateFlow.value.copy(
                book = book,
                userLists = listOf(onList),
            )
            stateFlow.value = stateFlow.value.copy(listsBeingMutated = setOf(onList.id))

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(userLists = listOf(offList))

            // ----- Assert -----
            val row = stateFlow.value.chooseListsSheet?.rows?.first { it.listId == onList.id }.shouldNotBeNull()
            row.isPending shouldBe true
            row.membership shouldBe ListMembership.ALL
            job.cancel()
        }
    }
}
