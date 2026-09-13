package nl.rhaydus.softcover.feature.library.presentation.collector

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
import nl.rhaydus.softcover.core.component.lists.ChooseListsVariant
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.preview.PreviewData
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope

class ChooseListsCollectorTest {
    private lateinit var dependencies: LibraryDependencies
    private lateinit var stateFlow: MutableStateFlow<LibraryUiState>
    private lateinit var scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>

    @BeforeEach
    fun setUp() {
        stateFlow = MutableStateFlow(LibraryUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LibraryLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        dependencies = mockk<LibraryDependencies>(relaxed = true)
    }

    private fun bookWithId(id: Int): Book {
        val edition = PreviewData.baseEdition.copy(
            id = id,
            bookId = id,
        )

        return PreviewData.baseBook.copy(
            id = id,
            editions = listOf(edition),
            defaultEdition = edition,
        )
    }

    private fun ownedList(id: Int = 1): BookList = BookList(
        id = id,
        name = "Owned",
        slug = "owned",
        books = emptyList(),
    )

    private fun customList(
        id: Int,
        name: String,
    ): BookList = BookList(
        id = id,
        name = name,
        slug = "custom-$id",
        books = emptyList(),
    )

    @Nested
    inner class OnLaunch {
        @Test
        fun `non-empty selection populates the sheet and the jacket editions in the same emission`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val book1 = bookWithId(id = 1)
                val book2 = bookWithId(id = 2)
                val collector = ChooseListsCollector()
                val job = launch { collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                ) }

                // ----- Act -----
                stateFlow.value = stateFlow.value.copy(
                    booksByTab = mapOf("all" to listOf(book1, book2)),
                    selectedBookIds = setOf(1, 2),
                    customLists = listOf(customList(
                        id = 10,
                        name = "Winter reading",
                    ),),
                )

                // ----- Assert -----
                stateFlow.value.chooseListsSheet shouldNotBe null
                stateFlow.value.chooseListsSheet?.variant shouldBe ChooseListsVariant.ManyBooks(
                    bookCount = 2,
                    coverCount = 2,
                )
                stateFlow.value.chooseListsJacketEditions shouldBe listOf(book1.currentEdition, book2.currentEdition)
                job.cancel()
            }

        @Test
        fun `empty selection leaves the sheet null even when custom lists are present`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val collector = ChooseListsCollector()
                val job = launch { collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                ) }

                // ----- Act -----
                stateFlow.value = stateFlow.value.copy(
                    customLists = listOf(customList(
                        id = 10,
                        name = "Winter reading",
                    ),),
                    selectedBookIds = emptySet(),
                )

                // ----- Assert -----
                stateFlow.value.chooseListsSheet shouldBe null
                job.cancel()
            }

        @Test
        fun `jacket editions resolve through booksByTab and are capped at three`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val books = (1..5).map { id -> bookWithId(id = id) }
            val collector = ChooseListsCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(
                booksByTab = mapOf("all" to books),
                selectedBookIds = setOf(1, 2, 3, 4, 5),
            )

            // ----- Assert -----
            stateFlow.value.chooseListsJacketEditions.size shouldBe 3
            stateFlow.value.chooseListsJacketEditions shouldBe books.take(3).map { it.currentEdition }
            job.cancel()
        }

        @Test
        fun `the built-in Owned list is excluded from the sheet's rows`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val book = bookWithId(id = 1)
            val owned = ownedList(id = 1)
            val custom = customList(
                id = 2,
                name = "Winter reading",
            )
            val collector = ChooseListsCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(
                booksByTab = mapOf("all" to listOf(book)),
                selectedBookIds = setOf(1),
                customLists = listOf(owned, custom),
            )

            // ----- Assert -----
            stateFlow.value.chooseListsSheet?.rows?.map { it.listId } shouldBe listOf(2)
            job.cancel()
        }
    }
}
