package nl.rhaydus.softcover.feature.library.presentation.collector

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.matchers.types.shouldNotBeSameInstanceAs
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import nl.rhaydus.softcover.core.component.badge.CoverOverlayUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardDecorations
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.core.component.bookcard.BookCardLead
import nl.rhaydus.softcover.core.component.bookcard.BookCardLinePart
import nl.rhaydus.softcover.core.component.bookcard.BookCardProgress
import nl.rhaydus.softcover.core.component.bookcard.BookCardSelection
import nl.rhaydus.softcover.core.component.bookcard.BookCardTrailing
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardVariant
import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookDeadline
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.DateStyle
import nl.rhaydus.softcover.core.domain.model.DeadlineUnit
import nl.rhaydus.softcover.core.domain.model.LibraryGridLayout
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.domain.model.UserBookRead
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.presentation.model.LibraryTab
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.mapper.standInCards
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.time.Clock

class BookCardModelsCollectorTest {
    private lateinit var dependencies: LibraryDependencies
    private lateinit var stateFlow: MutableStateFlow<LibraryUiState>
    private lateinit var scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>

    private val testDispatcher = UnconfinedTestDispatcher()

    private val readTab = LibraryTab.Status.of(UserBookStatus.READ)
    private val listTab = LibraryTab.CustomList(
        listId = 5,
        listName = "Ranked",
    )

    @BeforeEach
    fun setUp() {
        stateFlow = MutableStateFlow(LibraryUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LibraryLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
        dependencies = mockk<LibraryDependencies>(relaxed = true).also { mock ->
            every {
                mock.defaultDispatcher
            } returns testDispatcher
        }
    }

    private fun ImmutableList<BookCardUiModel>.cardWithId(id: Int): BookCardUiModel = first {
        when (val key = it.key) {
            is BookCardKey.Book -> key.bookId == id
            is BookCardKey.Edition -> key.editionId == id
            is BookCardKey.Series -> key.seriesId == id
        }
    }

    private fun TestScope.launchCollector(): Job = launch {
        BookCardModelsCollector().onLaunch(
            scope = scope,
            dependencies = dependencies,
        )
    }

    private fun BookCardUiModel.coverBadge(): CoverOverlayUiModel? = when (val shape = variant) {
        is BookCardVariant.CoverOnly -> shape.cover.badge
        is BookCardVariant.Column -> shape.cover?.badge
        is BookCardVariant.Row -> (shape.lead as? BookCardLead.Cover)?.cover?.badge
    }

    private fun stubBook(
        id: Int,
        title: String = "Title $id",
        authors: List<Author> = emptyList(),
        status: BookStatus = BookStatus.Read,
        progress: Float? = null,
        seriesText: String? = null,
        releaseYear: Int = -1,
        usersCount: Int = 0,
        rating: Double = 0.0,
        currentPage: Int? = null,
        currentSeconds: Int? = null,
        edition: BookEdition? = stubCurrentEdition(),
    ): Book = mockk<Book>(relaxed = true).also { book ->
        every {
            book.id
        } returns id

        every {
            book.title
        } returns title

        every {
            book.authors
        } returns authors

        every {
            book.status
        } returns status

        every {
            book.seriesText
        } returns seriesText

        every {
            book.releaseYear
        } returns releaseYear

        every {
            book.usersCount
        } returns usersCount

        every {
            book.rating
        } returns rating

        every {
            book.currentEdition
        } returns edition

        every {
            book.authorString
        } returns (edition?.authorString?.takeIf { it.isNotBlank() } ?: authors.joinToString(", ") { it.name })

        every {
            book.userBookRead
        } returns if (progress != null || currentPage != null || currentSeconds != null) {
            mockk<UserBookRead>(relaxed = true).also { read ->
                every {
                    read.progress
                } returns (progress ?: 0f)

                every {
                    read.currentPage
                } returns currentPage

                every {
                    read.currentSeconds
                } returns currentSeconds
            }
        } else {
            null
        }
    }

    private fun stubCurrentEdition(
        pages: Int? = null,
        audioSeconds: Int? = null,
        authorString: String = "",
    ): BookEdition = mockk<BookEdition>(relaxed = true).also { edition ->
        every {
            edition.pages
        } returns pages

        every {
            edition.audioSeconds
        } returns audioSeconds

        every {
            edition.authorString
        } returns authorString
    }

    private fun buildDeadline(
        bookId: Int,
        unit: DeadlineUnit,
        daysFromToday: Int,
    ): BookDeadline {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

        return BookDeadline(
            bookId = bookId,
            deadlineDate = today.plus(DatePeriod(days = daysFromToday)),
            setAt = today,
            initialPerDay = 10f,
            unit = unit,
        )
    }

    private fun stubEdition(
        id: Int,
        title: String? = "Edition $id",
        authors: List<Author> = emptyList(),
    ): BookEdition = mockk<BookEdition>(relaxed = true).also { edition ->
        every {
            edition.id
        } returns id

        every {
            edition.title
        } returns title

        every {
            edition.authors
        } returns authors

        every {
            edition.authorString
        } returns authors.joinToString(", ") { it.name }
    }

    private fun stubList(
        id: Int,
        ranked: Boolean,
    ): BookList = mockk<BookList>(relaxed = true).also { list ->
        every {
            list.id
        } returns id

        every {
            list.ranked
        } returns ranked
    }

    private fun author(name: String) = Author(
        id = 1,
        name = name,
    )

    private fun bookState(
        books: List<Book>,
        tab: LibraryTab = LibraryTab.All,
        layout: LibraryGridLayout = LibraryGridLayout.GRID_TWO_COLUMNS,
        selectionMode: Boolean = false,
        isRearranging: Boolean = false,
        sortMode: LibrarySortMode? = null,
    ) = LibraryUiState(
        visibleTabs = listOf(tab),
        booksByTab = mapOf(tab.id to books),
        gridLayout = layout,
        selectionMode = selectionMode,
        isRearranging = isRearranging,
        sortModeByTab = sortMode?.let { mapOf(tab.id to it) } ?: emptyMap(),
    )

    @Nested
    inner class Ordering {
        @Test
        fun `book cards follow the display order`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val job = launchCollector()
            val first = stubBook(id = 1)
            val second = stubBook(id = 2)
            val third = stubBook(id = 3)

            // ----- Act -----
            stateFlow.value = bookState(books = listOf(first, second, third)).copy(
                displayBooksByTab = mapOf("all" to listOf(third, first)),
            )

            // ----- Assert -----
            stateFlow.value.bookCardsByTab.getValue("all").set.cards.map { it.key } shouldBe
                listOf(BookCardKey.Book(bookId = 3), BookCardKey.Book(bookId = 1))
            job.cancel()
        }

        @Test
        fun `book cards fall back to the raw list order without display books`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val job = launchCollector()

            // ----- Act -----
            stateFlow.value = bookState(
                books = listOf(stubBook(id = 2), stubBook(id = 1)),
            )

            // ----- Assert -----
            stateFlow.value.bookCardsByTab.getValue("all").set.cards.map { it.key } shouldBe
                listOf(BookCardKey.Book(bookId = 2), BookCardKey.Book(bookId = 1))
            job.cancel()
        }

        @Test
        fun `an empty display list yields no cards rather than the raw list`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val job = launchCollector()

            // ----- Act -----
            stateFlow.value = bookState(books = listOf(stubBook(id = 1))).copy(
                displayBooksByTab = mapOf("all" to emptyList()),
            )

            // ----- Assert -----
            stateFlow.value.bookCardsByTab.getValue("all").set.cards.shouldBeEmpty()
            job.cancel()
        }

        @Test
        fun `edition cards follow the display order`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val job = launchCollector()
            val first = stubEdition(id = 7)
            val second = stubEdition(id = 8)

            // ----- Act -----
            stateFlow.value = LibraryUiState(
                visibleTabs = listOf(listTab),
                editionsByTab = mapOf(listTab.id to listOf(first, second)),
                displayEditionsByTab = mapOf(listTab.id to listOf(second, first)),
            )

            // ----- Assert -----
            stateFlow.value.editionCardsByTab.getValue(listTab.id).set.cards.map { it.key } shouldBe
                listOf(BookCardKey.Edition(editionId = 8), BookCardKey.Edition(editionId = 7))
            job.cancel()
        }

        @Test
        fun `edition cards fall back to the raw list order without display editions`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val job = launchCollector()

            // ----- Act -----
            stateFlow.value = LibraryUiState(
                visibleTabs = listOf(listTab),
                editionsByTab = mapOf(listTab.id to listOf(stubEdition(id = 9), stubEdition(id = 7))),
            )

            // ----- Assert -----
            stateFlow.value.editionCardsByTab.getValue(listTab.id).set.cards.map { it.key } shouldBe
                listOf(BookCardKey.Edition(editionId = 9), BookCardKey.Edition(editionId = 7))
            job.cancel()
        }
    }

    @Nested
    inner class Layouts {
        @Test
        fun `grid two and three columns map to the column variant`() = runTest(testDispatcher) {
            val layouts = listOf(
                LibraryGridLayout.GRID_TWO_COLUMNS,
                LibraryGridLayout.GRID_THREE_COLUMNS,
            )

            layouts.forEach { layout ->
                // ----- Arrange -----
                setUp()
                val job = launchCollector()

                // ----- Act -----
                stateFlow.value = bookState(
                    books = listOf(stubBook(id = 1)),
                    layout = layout,
                )

                // ----- Assert -----
                val card = stateFlow.value.bookCardsByTab.getValue("all").set.cards.cardWithId(1)
                card.variant.shouldBeInstanceOf<BookCardVariant.Column>()
                card.key shouldBe BookCardKey.Book(bookId = 1)
                job.cancel()
            }
        }

        @Test
        fun `cover-only layouts map to the cover-only variant`() = runTest(testDispatcher) {
            val layouts = listOf(
                LibraryGridLayout.GRID_TWO_COLUMNS_COVER_ONLY,
                LibraryGridLayout.GRID_THREE_COLUMNS_COVER_ONLY,
            )

            layouts.forEach { layout ->
                // ----- Arrange -----
                setUp()
                val job = launchCollector()

                // ----- Act -----
                stateFlow.value = bookState(
                    books = listOf(stubBook(id = 1)),
                    layout = layout,
                )

                // ----- Assert -----
                val card = stateFlow.value.bookCardsByTab.getValue("all").set.cards.cardWithId(1)
                card.variant.shouldBeInstanceOf<BookCardVariant.CoverOnly>()
                job.cancel()
            }
        }

        @Test
        fun `list layouts map books and editions to row variants of the matching size`() = runTest(testDispatcher) {
            val sizes = mapOf(
                LibraryGridLayout.LIST_COMPACT to BookCardVariant.Row.Size.Compact,
                LibraryGridLayout.LIST_LARGE to BookCardVariant.Row.Size.Large,
            )

            sizes.forEach { (layout, size) ->
                // ----- Arrange -----
                setUp()
                val job = launchCollector()

                // ----- Act -----
                stateFlow.value = bookState(
                    books = listOf(stubBook(id = 1)),
                    layout = layout,
                ).copy(
                    visibleTabs = listOf(LibraryTab.All, listTab),
                    editionsByTab = mapOf(listTab.id to listOf(stubEdition(id = 9))),
                )

                // ----- Assert -----
                val book = stateFlow.value.bookCardsByTab.getValue("all").set.cards.cardWithId(1)
                book.variant.shouldBeInstanceOf<BookCardVariant.Row>().size shouldBe size
                val edition = stateFlow.value.editionCardsByTab.getValue(listTab.id).set.cards.cardWithId(9)
                edition.variant.shouldBeInstanceOf<BookCardVariant.Row>().size shouldBe size
                job.cancel()
            }
        }
    }

    @Nested
    inner class Progress {
        @Test
        fun `a reading book with progress shows a value without a label`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val job = launchCollector()

            // ----- Act -----
            stateFlow.value = bookState(
                books = listOf(
                    stubBook(
                        id = 1,
                        status = BookStatus.Reading,
                        progress = 50f,
                    ),
                ),
            )

            // ----- Assert -----
            val progress = stateFlow.value.bookCardsByTab.getValue("all").set.cards.cardWithId(1).decorations.progress
            progress shouldBe BookCardProgress.Value(
                fraction = 0.5f,
                label = null,
            )
            job.cancel()
        }

        @Test
        fun `a reading book without a read record is reserved`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val job = launchCollector()

            // ----- Act -----
            stateFlow.value = bookState(
                books = listOf(
                    stubBook(
                        id = 1,
                        status = BookStatus.Reading,
                        progress = null,
                    ),
                ),
            )

            // ----- Assert -----
            stateFlow.value.bookCardsByTab.getValue("all").set.cards.cardWithId(1).decorations.progress shouldBe
                BookCardProgress.Reserved
            job.cancel()
        }

        @Test
        fun `a book that is not being read is reserved even with progress`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val job = launchCollector()

            // ----- Act -----
            stateFlow.value = bookState(
                books = listOf(
                    stubBook(
                        id = 1,
                        status = BookStatus.Read,
                        progress = 100f,
                    ),
                ),
            )

            // ----- Assert -----
            stateFlow.value.bookCardsByTab.getValue("all").set.cards.cardWithId(1).decorations.progress shouldBe
                BookCardProgress.Reserved
            job.cancel()
        }

        @Test
        fun `edition cards always reserve the progress slot`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val job = launchCollector()

            // ----- Act -----
            stateFlow.value = LibraryUiState(
                visibleTabs = listOf(listTab),
                editionsByTab = mapOf(listTab.id to listOf(stubEdition(id = 7))),
            )

            // ----- Assert -----
            stateFlow.value.editionCardsByTab.getValue(listTab.id).set.cards.cardWithId(7).decorations.progress shouldBe
                BookCardProgress.Reserved
            job.cancel()
        }
    }

    @Nested
    inner class PressBehaviour {
        @Test
        fun `a rearranging status tab is inert`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val job = launchCollector()

            // ----- Act -----
            stateFlow.value = bookState(
                books = listOf(stubBook(id = 1)),
                tab = readTab,
                isRearranging = true,
                sortMode = LibrarySortMode.MANUAL,
            )

            // ----- Assert -----
            stateFlow.value.bookCardsByTab.getValue(readTab.id).set.cards.cardWithId(1).decorations.pressBehaviour shouldBe
                BookCardDecorations.Press.None
            job.cancel()
        }

        @Test
        fun `selection mode wins over rearranging and is tap only`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val job = launchCollector()

            // ----- Act -----
            stateFlow.value = bookState(
                books = listOf(stubBook(id = 1)),
                tab = readTab,
                selectionMode = true,
                isRearranging = true,
                sortMode = LibrarySortMode.MANUAL,
            )

            // ----- Assert -----
            stateFlow.value.bookCardsByTab.getValue(readTab.id).set.cards.cardWithId(1).decorations.pressBehaviour shouldBe
                BookCardDecorations.Press.Tap
            job.cancel()
        }

        @Test
        fun `the All tab is never inert even when rearranging on manual sort`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val job = launchCollector()

            // ----- Act -----
            stateFlow.value = bookState(
                books = listOf(stubBook(id = 1)),
                isRearranging = true,
                sortMode = LibrarySortMode.MANUAL,
            )

            // ----- Assert -----
            stateFlow.value.bookCardsByTab.getValue("all").set.cards.cardWithId(1).decorations.pressBehaviour shouldBe
                BookCardDecorations.Press.TapAndLongPress
            job.cancel()
        }

        @Test
        fun `a book shelf with no matching visible tab gets no cards`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val job = launchCollector()

            // ----- Act -----
            stateFlow.value = LibraryUiState(
                visibleTabs = emptyList(),
                booksByTab = mapOf(readTab.id to listOf(stubBook(id = 1))),
                isRearranging = true,
                sortModeByTab = mapOf(readTab.id to LibrarySortMode.MANUAL),
            )

            // ----- Assert -----
            stateFlow.value.bookCardsByTab.containsKey(readTab.id) shouldBe false
            job.cancel()
        }

        @Test
        fun `a rearranging ranked edition list is inert`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val job = launchCollector()

            // ----- Act -----
            stateFlow.value = LibraryUiState(
                visibleTabs = listOf(listTab),
                editionsByTab = mapOf(listTab.id to listOf(stubEdition(id = 7))),
                customLists = listOf(
                    stubList(
                        id = 5,
                        ranked = true,
                    ),
                ),
                sortModeByTab = mapOf(listTab.id to LibrarySortMode.ORDER),
                isRearranging = true,
            )

            // ----- Assert -----
            stateFlow.value.editionCardsByTab.getValue(listTab.id).set.cards.cardWithId(7).decorations.pressBehaviour shouldBe
                BookCardDecorations.Press.None
            job.cancel()
        }

        @Test
        fun `an unranked edition list stays tappable while rearranging`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val job = launchCollector()

            // ----- Act -----
            stateFlow.value = LibraryUiState(
                visibleTabs = listOf(listTab),
                editionsByTab = mapOf(listTab.id to listOf(stubEdition(id = 7))),
                customLists = listOf(
                    stubList(
                        id = 5,
                        ranked = false,
                    ),
                ),
                sortModeByTab = mapOf(listTab.id to LibrarySortMode.ORDER),
                isRearranging = true,
            )

            // ----- Assert -----
            stateFlow.value.editionCardsByTab.getValue(listTab.id).set.cards.cardWithId(7).decorations.pressBehaviour shouldBe
                BookCardDecorations.Press.Tap
            job.cancel()
        }

        @Test
        fun `an edition list that is not rearranging is tap only`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val job = launchCollector()

            // ----- Act -----
            stateFlow.value = LibraryUiState(
                visibleTabs = listOf(listTab),
                editionsByTab = mapOf(listTab.id to listOf(stubEdition(id = 7))),
                customLists = listOf(
                    stubList(
                        id = 5,
                        ranked = true,
                    ),
                ),
            )

            // ----- Assert -----
            stateFlow.value.editionCardsByTab.getValue(listTab.id).set.cards.cardWithId(7).decorations.pressBehaviour shouldBe
                BookCardDecorations.Press.Tap
            job.cancel()
        }
    }

    @Nested
    inner class Byline {
        @Test
        fun `lists every author prefixed with By`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val job = launchCollector()

            // ----- Act -----
            stateFlow.value = bookState(
                books = listOf(
                    stubBook(
                        id = 1,
                        title = "Piranesi",
                        authors = listOf(author("Susanna Clarke"), author("Someone Else")),
                    ),
                ),
            )

            // ----- Assert -----
            val variant = stateFlow.value.bookCardsByTab.getValue("all").set.cards.cardWithId(1).variant
                .shouldBeInstanceOf<BookCardVariant.Column>()
            variant.text.title shouldBe "Piranesi"
            variant.text.byline shouldBe "By Susanna Clarke, Someone Else"
            job.cancel()
        }

        @Test
        fun `is null when the book has no author`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val job = launchCollector()

            // ----- Act -----
            stateFlow.value = bookState(books = listOf(stubBook(id = 1)))

            // ----- Assert -----
            val variant = stateFlow.value.bookCardsByTab.getValue("all").set.cards.cardWithId(1).variant
                .shouldBeInstanceOf<BookCardVariant.Column>()
            variant.text.byline.shouldBeNull()
            job.cancel()
        }

        @Test
        fun `is null when the first author name is blank`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val job = launchCollector()

            // ----- Act -----
            stateFlow.value = bookState(
                books = listOf(
                    stubBook(
                        id = 1,
                        authors = listOf(author("  ")),
                    ),
                ),
            )

            // ----- Assert -----
            val variant = stateFlow.value.bookCardsByTab.getValue("all").set.cards.cardWithId(1).variant
                .shouldBeInstanceOf<BookCardVariant.Column>()
            variant.text.byline.shouldBeNull()
            job.cancel()
        }

        @Test
        fun `edition cards use the edition title, empty when null, and its authors`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val job = launchCollector()

            // ----- Act -----
            stateFlow.value = LibraryUiState(
                visibleTabs = listOf(listTab),
                editionsByTab = mapOf(
                    listTab.id to listOf(
                        stubEdition(
                            id = 7,
                            title = "Audio",
                            authors = listOf(author("Ann")),
                        ),
                        stubEdition(
                            id = 8,
                            title = null,
                        ),
                    ),
                ),
            )

            // ----- Assert -----
            val cards = stateFlow.value.editionCardsByTab.getValue(listTab.id).set.cards
            val withAuthor = cards.cardWithId(7).variant.shouldBeInstanceOf<BookCardVariant.Column>()
            withAuthor.text.title shouldBe "Audio"
            withAuthor.text.byline shouldBe "By Ann"
            cards.cardWithId(7).key shouldBe BookCardKey.Edition(editionId = 7)
            val without = cards.cardWithId(8).variant.shouldBeInstanceOf<BookCardVariant.Column>()
            without.text.title shouldBe ""
            without.text.byline.shouldBeNull()
            job.cancel()
        }
    }

    @Nested
    inner class SelectionMarks {
        @Test
        fun `outside selection mode no card carries a mark`() = runTest(testDispatcher) {
            LibraryGridLayout.entries.forEach { layout ->
                // ----- Arrange -----
                setUp()
                val job = launchCollector()

                // ----- Act -----
                stateFlow.value = bookState(
                    books = listOf(stubBook(id = 1)),
                    layout = layout,
                    selectionMode = false,
                )

                // ----- Assert -----
                stateFlow.value.bookCardsByTab.getValue("all").set.cards.cardWithId(1).decorations.selection.shouldBeNull()
                job.cancel()
            }
        }

        @Test
        fun `only the selected books are marked selected`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val job = launchCollector()

            // ----- Act -----
            stateFlow.value = bookState(
                books = listOf(stubBook(id = 1), stubBook(id = 2)),
                selectionMode = true,
            ).copy(selectedBookIds = setOf(2))

            // ----- Assert -----
            val cards = stateFlow.value.bookCardsByTab.getValue("all").set.cards
            cards.cardWithId(1).decorations.selection.shouldBeInstanceOf<BookCardSelection.Multiple>().selected shouldBe false
            cards.cardWithId(2).decorations.selection.shouldBeInstanceOf<BookCardSelection.Multiple>().selected shouldBe true
            job.cancel()
        }
    }

    @Nested
    inner class CompactBookRow {
        private fun compactState(book: Book) = bookState(
            books = listOf(book),
            layout = LibraryGridLayout.LIST_COMPACT,
        )

        private fun LibraryUiState.row(): BookCardVariant.Row = bookCardsByTab.getValue("all").set.cards.cardWithId(1).variant
            .shouldBeInstanceOf<BookCardVariant.Row>()

        @Test
        fun `is coverless with the title and an all-authors byline`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val job = launchCollector()

            // ----- Act -----
            stateFlow.value = compactState(
                stubBook(
                    id = 1,
                    title = "Dune",
                    authors = listOf(author("Frank Herbert"), author("Someone Else")),
                ),
            )

            // ----- Assert -----
            val row = stateFlow.value.row()
            row.lead.shouldBeNull()
            row.text.title shouldBe "Dune"
            row.text.byline shouldBe "By Frank Herbert, Someone Else"
            job.cancel()
        }

        @Test
        fun `carries no progress, cover badge, eyebrow, stats or deadline summary`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val job = launchCollector()

            // ----- Act -----
            stateFlow.value = compactState(
                stubBook(
                    id = 1,
                    status = BookStatus.Reading,
                    progress = 40f,
                    seriesText = "Series #1",
                ),
            )

            // ----- Assert -----
            val card = stateFlow.value.bookCardsByTab.getValue("all").set.cards.cardWithId(1)
            card.decorations.progress.shouldBeNull()
            card.coverBadge().shouldBeNull()
            val row = card.variant.shouldBeInstanceOf<BookCardVariant.Row>()
            row.text.eyebrow.shouldBeNull()
            row.text.supportingLines shouldBe emptyList()
            row.deadline.shouldBeNull()
            job.cancel()
        }

        @Test
        fun `a tracked deadline becomes a trailing badge`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val job = launchCollector()

            // ----- Act -----
            stateFlow.value = compactState(
                stubBook(
                    id = 1,
                    edition = stubCurrentEdition(pages = 100),
                ),
            ).copy(
                deadlines = mapOf(
                    1 to buildDeadline(
                        bookId = 1,
                        unit = DeadlineUnit.PAGES,
                        daysFromToday = 10,
                    ),
                ),
            )

            // ----- Assert -----
            stateFlow.value.row().trailing.shouldBeInstanceOf<BookCardTrailing.Badge>()
            job.cancel()
        }

        @Test
        fun `a deadline on a book without a current edition yields no badge`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val job = launchCollector()

            // ----- Act -----
            stateFlow.value = compactState(
                stubBook(
                    id = 1,
                    edition = null,
                ),
            ).copy(
                deadlines = mapOf(
                    1 to buildDeadline(
                        bookId = 1,
                        unit = DeadlineUnit.PAGES,
                        daysFromToday = 10,
                    ),
                ),
            )

            // ----- Assert -----
            stateFlow.value.row().trailing.shouldBeNull()
            job.cancel()
        }
    }

    @Nested
    inner class LargeBookRow {
        private fun largeState(book: Book) = bookState(
            books = listOf(book),
            layout = LibraryGridLayout.LIST_LARGE,
        )

        private fun LibraryUiState.card(): BookCardUiModel = bookCardsByTab.getValue("all").set.cards.cardWithId(1)

        private fun LibraryUiState.row(): BookCardVariant.Row = card().variant
            .shouldBeInstanceOf<BookCardVariant.Row>()

        @Test
        fun `leads with the cover and has no trailing`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val job = launchCollector()

            // ----- Act -----
            stateFlow.value = largeState(
                stubBook(
                    id = 1,
                    title = "Dune",
                ),
            )

            // ----- Assert -----
            val row = stateFlow.value.row()
            val lead = row.lead.shouldBeInstanceOf<BookCardLead.Cover>()
            lead.cover.model.coverlessTitle shouldBe "Dune"
            row.trailing.shouldBeNull()
            job.cancel()
        }

        @Test
        fun `the series becomes an uppercase eyebrow`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val job = launchCollector()

            // ----- Act -----
            stateFlow.value = largeState(
                stubBook(
                    id = 1,
                    seriesText = "Dune Chronicles #1",
                ),
            )

            // ----- Assert -----
            stateFlow.value.row().text.eyebrow.shouldNotBeNull().text shouldBe "DUNE CHRONICLES #1"
            job.cancel()
        }

        @Test
        fun `a missing or blank series has no eyebrow`() = runTest(testDispatcher) {
            listOf(null, "  ").forEach { series ->
                // ----- Arrange -----
                setUp()
                val job = launchCollector()

                // ----- Act -----
                stateFlow.value = largeState(
                    stubBook(
                        id = 1,
                        seriesText = series,
                    ),
                )

                // ----- Assert -----
                stateFlow.value.row().text.eyebrow.shouldBeNull()
                job.cancel()
            }
        }

        @Test
        fun `the byline is the current edition's author string, falling back to the book authors`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val job = launchCollector()

            // ----- Act -----
            stateFlow.value = largeState(
                stubBook(
                    id = 1,
                    authors = listOf(author("Ignored Author")),
                    edition = stubCurrentEdition(authorString = "Frank Herbert, Brian Herbert"),
                ),
            )

            // ----- Assert -----
            stateFlow.value.row().text.byline shouldBe "By Frank Herbert, Brian Herbert"
            job.cancel()
        }

        @Test
        fun `the byline falls back to the book authors without a current edition author string`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val job = launchCollector()

            // ----- Act -----
            stateFlow.value = largeState(
                stubBook(
                    id = 1,
                    authors = listOf(author("Ann"), author("Bob")),
                    edition = stubCurrentEdition(authorString = ""),
                ),
            )

            // ----- Assert -----
            stateFlow.value.row().text.byline shouldBe "By Ann, Bob"
            job.cancel()
        }

        @Test
        fun `a blank author string or no current edition has no byline`() = runTest(testDispatcher) {
            listOf(stubCurrentEdition(authorString = ""), null).forEach { edition ->
                // ----- Arrange -----
                setUp()
                val job = launchCollector()

                // ----- Act -----
                stateFlow.value = largeState(
                    stubBook(
                        id = 1,
                        edition = edition,
                    ),
                )

                // ----- Assert -----
                stateFlow.value.row().text.byline.shouldBeNull()
                job.cancel()
            }
        }

        @Test
        fun `the stats line carries year, readers and rating`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val job = launchCollector()

            // ----- Act -----
            stateFlow.value = largeState(
                stubBook(
                    id = 1,
                    releaseYear = 1965,
                    usersCount = 12,
                    rating = 4.2,
                ),
            )

            // ----- Assert -----
            stateFlow.value.row().text.supportingLines shouldBe listOf(
                persistentListOf(
                    BookCardLinePart.Text(text = "1965"),
                    BookCardLinePart.Text(text = "12 readers"),
                    BookCardLinePart.Rating(
                        label = "4.2",
                        contentDescription = "Rated 4.2 out of 5",
                    ),
                ),
            )
            job.cancel()
        }

        @Test
        fun `a reading book shows a percentage-labelled progress`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val job = launchCollector()

            // ----- Act -----
            stateFlow.value = largeState(
                stubBook(
                    id = 1,
                    status = BookStatus.Reading,
                    progress = 40f,
                ),
            )

            // ----- Assert -----
            stateFlow.value.card().decorations.progress shouldBe BookCardProgress.Value(
                fraction = 0.4f,
                label = "40%",
            )
            job.cancel()
        }

        @Test
        fun `progress is null when the book is not being read or has no read record`() = runTest(testDispatcher) {
            val books = listOf(
                stubBook(
                    id = 1,
                    status = BookStatus.Read,
                    progress = 100f,
                ),
                stubBook(
                    id = 1,
                    status = BookStatus.Reading,
                    progress = null,
                ),
            )

            books.forEach { book ->
                // ----- Arrange -----
                setUp()
                val job = launchCollector()

                // ----- Act -----
                stateFlow.value = largeState(book)

                // ----- Assert -----
                stateFlow.value.card().decorations.progress.shouldBeNull()
                job.cancel()
            }
        }

        @Test
        fun `a deadline lifts onto the row as a summary and never as a cover badge`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val job = launchCollector()

            // ----- Act -----
            stateFlow.value = largeState(
                stubBook(
                    id = 1,
                    edition = stubCurrentEdition(pages = 100),
                    currentPage = 0,
                ),
            ).copy(
                deadlines = mapOf(
                    1 to buildDeadline(
                        bookId = 1,
                        unit = DeadlineUnit.PAGES,
                        daysFromToday = 10,
                    ),
                ),
            )

            // ----- Assert -----
            stateFlow.value.row().deadline.shouldNotBeNull().text.substringAfter(" • ") shouldBe "10 pages/day"
            stateFlow.value.card().coverBadge().shouldBeNull()
            job.cancel()
        }

        @Test
        fun `a seconds-unit deadline paces in hours and minutes`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val job = launchCollector()

            // ----- Act -----
            stateFlow.value = largeState(
                stubBook(
                    id = 1,
                    edition = stubCurrentEdition(audioSeconds = 36_000),
                    currentSeconds = 0,
                ),
            ).copy(
                deadlines = mapOf(
                    1 to buildDeadline(
                        bookId = 1,
                        unit = DeadlineUnit.SECONDS,
                        daysFromToday = 10,
                    ),
                ),
            )

            // ----- Assert -----
            stateFlow.value.row().deadline.shouldNotBeNull().text.substringAfter(" • ") shouldBe "1h 0m/day"
            job.cancel()
        }

        @Test
        fun `the pace counts down from the current page`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val job = launchCollector()

            // ----- Act -----
            stateFlow.value = largeState(
                stubBook(
                    id = 1,
                    edition = stubCurrentEdition(pages = 100),
                    currentPage = 50,
                ),
            ).copy(
                deadlines = mapOf(
                    1 to buildDeadline(
                        bookId = 1,
                        unit = DeadlineUnit.PAGES,
                        daysFromToday = 10,
                    ),
                ),
            )

            // ----- Assert -----
            stateFlow.value.row().deadline.shouldNotBeNull().text.substringAfter(" • ") shouldBe "5 pages/day"
            job.cancel()
        }

        @Test
        fun `a book without a deadline or without a current edition has no summary`() = runTest(testDispatcher) {
            val deadline = buildDeadline(
                bookId = 1,
                unit = DeadlineUnit.PAGES,
                daysFromToday = 10,
            )
            val cases = listOf(
                stubBook(id = 1) to emptyMap(),
                stubBook(
                    id = 1,
                    edition = null,
                ) to mapOf(1 to deadline),
            )

            cases.forEach { (book, deadlines) ->
                // ----- Arrange -----
                setUp()
                val job = launchCollector()

                // ----- Act -----
                stateFlow.value = largeState(book).copy(deadlines = deadlines)

                // ----- Assert -----
                stateFlow.value.row().deadline.shouldBeNull()
                job.cancel()
            }
        }
    }

    @Nested
    inner class GridDeadlines {
        @Test
        fun `a deadline becomes a cover badge on the grid cards`() = runTest(testDispatcher) {
            LibraryGridLayout.entries.filter { it.name.startsWith("GRID") }.forEach { layout ->
                // ----- Arrange -----
                setUp()
                val job = launchCollector()

                // ----- Act -----
                stateFlow.value = bookState(
                    books = listOf(
                        stubBook(
                            id = 1,
                            edition = stubCurrentEdition(pages = 100),
                        ),
                    ),
                    layout = layout,
                ).copy(
                    deadlines = mapOf(
                        1 to buildDeadline(
                            bookId = 1,
                            unit = DeadlineUnit.PAGES,
                            daysFromToday = 10,
                        ),
                    ),
                )

                // ----- Assert -----
                stateFlow.value.bookCardsByTab.getValue("all").set.cards.cardWithId(1).coverBadge().shouldNotBeNull()
                job.cancel()
            }
        }
    }

    @Nested
    inner class EditionRows {
        private fun editionState(
            layout: LibraryGridLayout,
            edition: BookEdition,
        ) = LibraryUiState(
            visibleTabs = listOf(listTab),
            editionsByTab = mapOf(listTab.id to listOf(edition)),
            gridLayout = layout,
        )

        private fun LibraryUiState.card(): BookCardUiModel = editionCardsByTab.getValue(listTab.id).set.cards.cardWithId(7)

        @Test
        fun `the compact row is coverless with the title and author byline`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val job = launchCollector()

            // ----- Act -----
            stateFlow.value = editionState(
                layout = LibraryGridLayout.LIST_COMPACT,
                edition = stubEdition(
                    id = 7,
                    title = "Dune (Deluxe)",
                    authors = listOf(author("Frank Herbert")),
                ),
            )

            // ----- Assert -----
            val row = stateFlow.value.card().variant.shouldBeInstanceOf<BookCardVariant.Row>()
            row.size shouldBe BookCardVariant.Row.Size.Compact
            row.lead.shouldBeNull()
            row.text.title shouldBe "Dune (Deluxe)"
            row.text.byline shouldBe "By Frank Herbert"
            job.cancel()
        }

        @Test
        fun `the large row leads with the edition cover`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val job = launchCollector()

            // ----- Act -----
            stateFlow.value = editionState(
                layout = LibraryGridLayout.LIST_LARGE,
                edition = stubEdition(
                    id = 7,
                    title = "Dune (Deluxe)",
                ),
            )

            // ----- Assert -----
            val row = stateFlow.value.card().variant.shouldBeInstanceOf<BookCardVariant.Row>()
            row.size shouldBe BookCardVariant.Row.Size.Large
            row.lead.shouldBeInstanceOf<BookCardLead.Cover>().cover.model.coverlessTitle shouldBe "Dune (Deluxe)"
            job.cancel()
        }

        @Test
        fun `list rows carry no progress slot while grid cards reserve one`() = runTest(testDispatcher) {
            val expected = mapOf(
                LibraryGridLayout.LIST_COMPACT to null,
                LibraryGridLayout.LIST_LARGE to null,
                LibraryGridLayout.GRID_TWO_COLUMNS to BookCardProgress.Reserved,
            )

            expected.forEach { (layout, progress) ->
                // ----- Arrange -----
                setUp()
                val job = launchCollector()

                // ----- Act -----
                stateFlow.value = editionState(
                    layout = layout,
                    edition = stubEdition(id = 7),
                )

                // ----- Assert -----
                stateFlow.value.card().decorations.progress shouldBe progress
                job.cancel()
            }
        }
    }

    @Nested
    inner class ListPressBehaviour {
        private fun TestScope.pressOf(
            layout: LibraryGridLayout,
            selectionMode: Boolean,
            isRearranging: Boolean,
        ): BookCardDecorations.Press {
            val job = launchCollector()

            stateFlow.value = bookState(
                books = listOf(stubBook(id = 1)),
                tab = readTab,
                layout = layout,
                selectionMode = selectionMode,
                isRearranging = isRearranging,
                sortMode = LibrarySortMode.MANUAL,
            )

            val press = stateFlow.value.bookCardsByTab.getValue(readTab.id).set.cards.cardWithId(1).decorations.pressBehaviour
            job.cancel()
            return press
        }

        @Test
        fun `a compact row supports tap and long press outside selection and rearranging`() =
            runTest(testDispatcher) {
                // ----- Arrange -----
                val layout = LibraryGridLayout.LIST_COMPACT
                val selectionMode = false
                val isRearranging = false

                // ----- Act -----
                val press = pressOf(
                    layout = layout,
                    selectionMode = selectionMode,
                    isRearranging = isRearranging,
                )

                // ----- Assert -----
                press shouldBe BookCardDecorations.Press.TapAndLongPress
            }

        @Test
        fun `a large row is tap only in selection mode`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val layout = LibraryGridLayout.LIST_LARGE
            val selectionMode = true
            val isRearranging = false

            // ----- Act -----
            val press = pressOf(
                layout = layout,
                selectionMode = selectionMode,
                isRearranging = isRearranging,
            )

            // ----- Assert -----
            press shouldBe BookCardDecorations.Press.Tap
        }

        @Test
        fun `a large row is inert while its tab is rearranging`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val layout = LibraryGridLayout.LIST_LARGE
            val selectionMode = false
            val isRearranging = true

            // ----- Act -----
            val press = pressOf(
                layout = layout,
                selectionMode = selectionMode,
                isRearranging = isRearranging,
            )

            // ----- Assert -----
            press shouldBe BookCardDecorations.Press.None
        }
    }

    @Nested
    inner class StandIns {
        @Test
        fun `a tab whose books have not landed has no entry and loading cards follow the live layout`() =
            runTest(testDispatcher) {
                // ----- Arrange -----
                val job = launchCollector()

                // ----- Act -----
                stateFlow.value = LibraryUiState(
                    visibleTabs = listOf(LibraryTab.All),
                    gridLayout = LibraryGridLayout.LIST_LARGE,
                )

                // ----- Assert -----
                stateFlow.value.bookCardsByTab.containsKey("all") shouldBe false
                stateFlow.value.loadingCards.layout shouldBe LibraryGridLayout.LIST_LARGE
                job.cancel()
            }

        @Test
        fun `an empty book list while loading has no entry`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val job = launchCollector()

            // ----- Act -----
            stateFlow.value = bookState(books = emptyList()).copy(isLoading = true)

            // ----- Assert -----
            stateFlow.value.bookCardsByTab.containsKey("all") shouldBe false
            job.cancel()
        }

        @Test
        fun `an empty book list once loading is done has no cards`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val job = launchCollector()

            // ----- Act -----
            stateFlow.value = bookState(books = emptyList()).copy(isLoading = false)

            // ----- Assert -----
            stateFlow.value.bookCardsByTab.getValue("all").set.cards.shouldBeEmpty()
            job.cancel()
        }

        @Test
        fun `a tab whose editions have not landed has no entry`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val job = launchCollector()

            // ----- Act -----
            stateFlow.value = LibraryUiState(visibleTabs = listOf(listTab))

            // ----- Assert -----
            stateFlow.value.editionCardsByTab.containsKey(listTab.id) shouldBe false
            job.cancel()
        }

        @Test
        fun `a tab with a loaded list keeps its cards while another tab is still loading`() =
            runTest(testDispatcher) {
                // ----- Arrange -----
                val job = launchCollector()

                // ----- Act -----
                stateFlow.value = LibraryUiState(
                    visibleTabs = listOf(LibraryTab.All, readTab),
                    booksByTab = mapOf("all" to listOf(stubBook(id = 1))),
                )

                // ----- Assert -----
                stateFlow.value.bookCardsByTab.getValue("all").set.cards.none { it.isLoading } shouldBe true
                stateFlow.value.bookCardsByTab.containsKey(readTab.id) shouldBe false
                job.cancel()
            }

        @Test
        fun `loading cards follow the grid layout and resolve no payload`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val job = launchCollector()

            LibraryGridLayout.entries.forEach { layout ->
                // ----- Act -----
                stateFlow.value = LibraryUiState(
                    visibleTabs = listOf(LibraryTab.All),
                    gridLayout = layout,
                )

                // ----- Assert -----
                val loading = stateFlow.value.loadingCards
                loading.layout shouldBe layout
                loading.set.cards shouldBe layout.standInCards()
                loading.set.payloadByKey.isEmpty() shouldBe true
                loading.set[loading.set.cards.first().key].shouldBeNull()
            }
            job.cancel()
        }
    }

    @Nested
    inner class Payloads {
        @Test
        fun `book payloads resolve each card key to its book`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val job = launchCollector()
            val first = stubBook(id = 1)
            val second = stubBook(id = 2)

            // ----- Act -----
            stateFlow.value = bookState(books = listOf(first, second))

            // ----- Assert -----
            val tabCards = stateFlow.value.bookCardsByTab.getValue("all")
            tabCards.set.payloadByKey.size shouldBe 2
            tabCards.set.payloadByKey.getValue(tabCards.set.cards.cardWithId(1).key) shouldBeSameInstanceAs first
            tabCards.set.payloadByKey.getValue(tabCards.set.cards.cardWithId(2).key) shouldBeSameInstanceAs second
            job.cancel()
        }

        @Test
        fun `edition payloads resolve each card key to its edition`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val job = launchCollector()
            val edition = stubEdition(id = 7)

            // ----- Act -----
            stateFlow.value = LibraryUiState(
                visibleTabs = listOf(listTab),
                editionsByTab = mapOf(listTab.id to listOf(edition)),
                gridLayout = LibraryGridLayout.LIST_COMPACT,
            )

            // ----- Assert -----
            val tabCards = stateFlow.value.editionCardsByTab.getValue(listTab.id)
            tabCards.set.payloadByKey.getValue(tabCards.set.cards.cardWithId(7).key) shouldBeSameInstanceAs edition
            job.cancel()
        }
    }

    @Nested
    inner class Caching {
        private val tabA = LibraryTab.All
        private val tabB = readTab

        private fun twoTabState(
            booksA: List<Book>,
            booksB: List<Book>,
            layout: LibraryGridLayout = LibraryGridLayout.GRID_TWO_COLUMNS,
        ) = LibraryUiState(
            visibleTabs = listOf(tabA, tabB),
            booksByTab = mapOf(
                tabA.id to booksA,
                tabB.id to booksB,
            ),
            gridLayout = layout,
        )

        private fun BookCardUiModel.deadlineDateText(): String = variant.shouldBeInstanceOf<BookCardVariant.Row>()
            .deadline.shouldNotBeNull().text.substringBefore(" • ")

        private fun deadlineFor(bookId: Int) = buildDeadline(
            bookId = bookId,
            unit = DeadlineUnit.PAGES,
            daysFromToday = 10,
        )

        @Test
        fun `a tab keeps its inner map when only another tab's deadline changes`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val job = launchCollector()
            stateFlow.value = twoTabState(
                booksA = listOf(stubBook(id = 1)),
                booksB = listOf(stubBook(id = 2)),
            )
            val before = stateFlow.value.bookCardsByTab

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(deadlines = mapOf(2 to deadlineFor(2)))

            // ----- Assert -----
            val after = stateFlow.value.bookCardsByTab
            after.getValue(tabA.id) shouldBeSameInstanceAs before.getValue(tabA.id)
            after.getValue(tabB.id) shouldNotBeSameInstanceAs before.getValue(tabB.id)
            job.cancel()
        }

        @Test
        fun `a tab keeps its inner map when only another tab's selection changes`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val job = launchCollector()
            stateFlow.value = twoTabState(
                booksA = listOf(stubBook(id = 1)),
                booksB = listOf(stubBook(id = 2)),
            ).copy(selectionMode = true)
            val before = stateFlow.value.bookCardsByTab

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(selectedBookIds = setOf(2))

            // ----- Assert -----
            val after = stateFlow.value.bookCardsByTab
            after.getValue(tabA.id) shouldBeSameInstanceAs before.getValue(tabA.id)
            after.getValue(tabB.id) shouldNotBeSameInstanceAs before.getValue(tabB.id)
            after.getValue(tabB.id).set.cards.cardWithId(2).decorations.selection
                .shouldBeInstanceOf<BookCardSelection.Multiple>().selected shouldBe true
            job.cancel()
        }

        @Test
        fun `a deadline change rebuilds the tab with the new summary`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val job = launchCollector()
            stateFlow.value = bookState(
                books = listOf(
                    stubBook(
                        id = 1,
                        edition = stubCurrentEdition(pages = 100),
                    ),
                ),
                layout = LibraryGridLayout.GRID_TWO_COLUMNS,
            )
            stateFlow.value.bookCardsByTab.getValue("all").set.cards.cardWithId(1).coverBadge().shouldBeNull()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(deadlines = mapOf(1 to deadlineFor(1)))

            // ----- Assert -----
            stateFlow.value.bookCardsByTab.getValue("all").set.cards.cardWithId(1).coverBadge().shouldNotBeNull()
            job.cancel()
        }

        @Test
        fun `a date style change rebuilds every tab with the re-formatted deadline date`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val job = launchCollector()
            stateFlow.value = twoTabState(
                booksA = listOf(
                    stubBook(
                        id = 1,
                        edition = stubCurrentEdition(pages = 100),
                    ),
                ),
                booksB = listOf(
                    stubBook(
                        id = 2,
                        edition = stubCurrentEdition(pages = 100),
                    ),
                ),
                layout = LibraryGridLayout.LIST_LARGE,
            ).copy(
                deadlines = mapOf(
                    1 to deadlineFor(1),
                    2 to deadlineFor(2),
                ),
                dateStyle = DateStyle.DAY_MONTH_YEAR,
            )
            val beforeA = stateFlow.value.bookCardsByTab.getValue(tabA.id).set.cards.cardWithId(1).deadlineDateText()
            val beforeB = stateFlow.value.bookCardsByTab.getValue(tabB.id).set.cards.cardWithId(2).deadlineDateText()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(dateStyle = DateStyle.YEAR_MONTH_DAY)

            // ----- Assert -----
            val after = stateFlow.value.bookCardsByTab
            after.getValue(tabA.id).set.cards.cardWithId(1).deadlineDateText() shouldNotBe beforeA
            after.getValue(tabB.id).set.cards.cardWithId(2).deadlineDateText() shouldNotBe beforeB
            job.cancel()
        }

        @Test
        fun `a layout change rebuilds every tab`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val job = launchCollector()
            stateFlow.value = twoTabState(
                booksA = listOf(stubBook(id = 1)),
                booksB = listOf(stubBook(id = 2)),
                layout = LibraryGridLayout.LIST_COMPACT,
            )
            val before = stateFlow.value.bookCardsByTab

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(gridLayout = LibraryGridLayout.LIST_LARGE)

            // ----- Assert -----
            val after = stateFlow.value.bookCardsByTab
            after.getValue(tabA.id) shouldNotBeSameInstanceAs before.getValue(tabA.id)
            after.getValue(tabB.id) shouldNotBeSameInstanceAs before.getValue(tabB.id)
            after.getValue(tabA.id).set.cards.cardWithId(1).variant.shouldBeInstanceOf<BookCardVariant.Row>().size shouldBe
                BookCardVariant.Row.Size.Large
            job.cancel()
        }

        @Test
        fun `an edition tab keeps its inner map when only the books change`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val job = launchCollector()
            stateFlow.value = LibraryUiState(
                visibleTabs = listOf(listTab, tabB),
                editionsByTab = mapOf(
                    listTab.id to listOf(stubEdition(id = 7)),
                    tabB.id to listOf(stubEdition(id = 8)),
                ),
            )
            val before = stateFlow.value.editionCardsByTab

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(
                editionsByTab = stateFlow.value.editionsByTab + (tabB.id to listOf(stubEdition(id = 9))),
            )

            // ----- Assert -----
            val after = stateFlow.value.editionCardsByTab
            after.getValue(listTab.id) shouldBeSameInstanceAs before.getValue(listTab.id)
            after.getValue(tabB.id) shouldNotBeSameInstanceAs before.getValue(tabB.id)
            job.cancel()
        }

        @Test
        fun `only visible tabs are built`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val job = launchCollector()

            // ----- Act -----
            stateFlow.value = twoTabState(
                booksA = listOf(stubBook(id = 1)),
                booksB = listOf(stubBook(id = 2)),
            ).copy(visibleTabs = listOf(tabA))

            // ----- Assert -----
            stateFlow.value.bookCardsByTab.keys shouldBe setOf(tabA.id)
            job.cancel()
        }

        @Test
        fun `switching between column and cover-only grids does not serve stale variants`() =
            runTest(testDispatcher) {
                // ----- Arrange -----
                val books = listOf(stubBook(id = 1))
                val job = launchCollector()
                stateFlow.value = bookState(
                    books = books,
                    layout = LibraryGridLayout.GRID_TWO_COLUMNS,
                )

                // ----- Act -----
                stateFlow.value = bookState(
                    books = books,
                    layout = LibraryGridLayout.GRID_TWO_COLUMNS_COVER_ONLY,
                )

                // ----- Assert -----
                stateFlow.value.bookCardsByTab.getValue("all").set.cards.cardWithId(1).variant
                    .shouldBeInstanceOf<BookCardVariant.CoverOnly>()
                job.cancel()
            }
    }
}
