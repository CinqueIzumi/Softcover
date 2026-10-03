package nl.rhaydus.softcover.feature.library.presentation.collector

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.matchers.types.shouldNotBeSameInstanceAs
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.badge.CoverOverlayUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardDecorations
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.core.component.bookcard.BookCardProgress
import nl.rhaydus.softcover.core.component.bookcard.BookCardVariant
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.selection.SelectionMarkUiModel
import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.LibraryGridLayout
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.domain.model.UserBookRead
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.presentation.model.LibraryTab
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

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

    private fun TestScope.launchCollector(): Job = launch {
        BookCardModelsCollector().onLaunch(
            scope = scope,
            dependencies = dependencies,
        )
    }

    private fun stubBook(
        id: Int,
        title: String = "Title $id",
        authors: List<Author> = emptyList(),
        status: BookStatus = BookStatus.Read,
        progress: Float? = null,
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
            book.userBookRead
        } returns progress?.let { value ->
            mockk<UserBookRead>(relaxed = true).also { read ->
                every {
                    read.progress
                } returns value
            }
        }
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
    inner class Layouts {
        @Test
        fun `grid two and three columns map to the stacked variant`() = runTest(testDispatcher) {
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
                val card = stateFlow.value.bookCardsByTab.getValue("all").getValue(1)
                card.variant.shouldBeInstanceOf<BookCardVariant.Stacked>()
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
                val card = stateFlow.value.bookCardsByTab.getValue("all").getValue(1)
                card.variant.shouldBeInstanceOf<BookCardVariant.CoverOnly>()
                job.cancel()
            }
        }

        @Test
        fun `list layouts produce empty maps for books and editions`() = runTest(testDispatcher) {
            val layouts = listOf(
                LibraryGridLayout.LIST_COMPACT,
                LibraryGridLayout.LIST_LARGE,
            )

            layouts.forEach { layout ->
                // ----- Arrange -----
                setUp()
                val job = launchCollector()

                // ----- Act -----
                stateFlow.value = bookState(
                    books = listOf(stubBook(id = 1)),
                    layout = layout,
                ).copy(editionsByTab = mapOf(listTab.id to listOf(stubEdition(id = 9))))

                // ----- Assert -----
                stateFlow.value.bookCardsByTab shouldBe emptyMap()
                stateFlow.value.editionCardsByTab shouldBe emptyMap()
                job.cancel()
            }
        }

        @Test
        fun `switching from a grid layout to a list layout clears previously built cards`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val job = launchCollector()
            stateFlow.value = bookState(books = listOf(stubBook(id = 1)))
            stateFlow.value.bookCardsByTab.isEmpty() shouldBe false

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(gridLayout = LibraryGridLayout.LIST_COMPACT)

            // ----- Assert -----
            stateFlow.value.bookCardsByTab shouldBe emptyMap()
            job.cancel()
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
            val progress = stateFlow.value.bookCardsByTab.getValue("all").getValue(1).decorations.progress
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
            stateFlow.value.bookCardsByTab.getValue("all").getValue(1).decorations.progress shouldBe
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
            stateFlow.value.bookCardsByTab.getValue("all").getValue(1).decorations.progress shouldBe
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
            stateFlow.value.editionCardsByTab.getValue(listTab.id).getValue(7).decorations.progress shouldBe
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
            stateFlow.value.bookCardsByTab.getValue(readTab.id).getValue(1).decorations.pressBehaviour shouldBe
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
            stateFlow.value.bookCardsByTab.getValue(readTab.id).getValue(1).decorations.pressBehaviour shouldBe
                BookCardDecorations.Press.Tap
            job.cancel()
        }

        @Test
        fun `a non-rearranging tab supports tap and long press`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val job = launchCollector()

            // ----- Act -----
            stateFlow.value = bookState(books = listOf(stubBook(id = 1)))

            // ----- Assert -----
            stateFlow.value.bookCardsByTab.getValue("all").getValue(1).decorations.pressBehaviour shouldBe
                BookCardDecorations.Press.TapAndLongPress
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
            stateFlow.value.bookCardsByTab.getValue("all").getValue(1).decorations.pressBehaviour shouldBe
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
            stateFlow.value.editionCardsByTab.getValue(listTab.id).getValue(7).decorations.pressBehaviour shouldBe
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
            stateFlow.value.editionCardsByTab.getValue(listTab.id).getValue(7).decorations.pressBehaviour shouldBe
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
            stateFlow.value.editionCardsByTab.getValue(listTab.id).getValue(7).decorations.pressBehaviour shouldBe
                BookCardDecorations.Press.Tap
            job.cancel()
        }
    }

    @Nested
    inner class Byline {
        @Test
        fun `uses the first author prefixed with By`() = runTest(testDispatcher) {
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
            val variant = stateFlow.value.bookCardsByTab.getValue("all").getValue(1).variant
                .shouldBeInstanceOf<BookCardVariant.Stacked>()
            variant.text.title shouldBe "Piranesi"
            variant.text.byline shouldBe "By Susanna Clarke"
            job.cancel()
        }

        @Test
        fun `is null when the book has no author`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val job = launchCollector()

            // ----- Act -----
            stateFlow.value = bookState(books = listOf(stubBook(id = 1)))

            // ----- Assert -----
            val variant = stateFlow.value.bookCardsByTab.getValue("all").getValue(1).variant
                .shouldBeInstanceOf<BookCardVariant.Stacked>()
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
            val variant = stateFlow.value.bookCardsByTab.getValue("all").getValue(1).variant
                .shouldBeInstanceOf<BookCardVariant.Stacked>()
            variant.text.byline.shouldBeNull()
            job.cancel()
        }

        @Test
        fun `edition cards use the edition title, empty when null, and its first author`() = runTest(testDispatcher) {
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
            val cards = stateFlow.value.editionCardsByTab.getValue(listTab.id)
            val withAuthor = cards.getValue(7).variant.shouldBeInstanceOf<BookCardVariant.Stacked>()
            withAuthor.text.title shouldBe "Audio"
            withAuthor.text.byline shouldBe "By Ann"
            cards.getValue(7).key shouldBe BookCardKey.Edition(editionId = 7)
            val without = cards.getValue(8).variant.shouldBeInstanceOf<BookCardVariant.Stacked>()
            without.text.title shouldBe ""
            without.text.byline.shouldBeNull()
            job.cancel()
        }
    }

    @Nested
    inner class Overlays {
        @Test
        fun `picks up cover, selection and countdown by book id`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val cover = mockk<CoverUiModel>(relaxed = true)
            val mark = mockk<SelectionMarkUiModel>(relaxed = true)
            val badge = mockk<CoverOverlayUiModel>(relaxed = true)
            val job = launchCollector()

            // ----- Act -----
            stateFlow.value = bookState(books = listOf(stubBook(id = 1), stubBook(id = 2))).copy(
                bookCovers = mapOf(1 to cover),
                selectionMarks = persistentMapOf(1 to mark),
                deadlineCountdownOverlays = persistentMapOf(1 to badge),
            )

            // ----- Assert -----
            val cards = stateFlow.value.bookCardsByTab.getValue("all")
            val first = cards.getValue(1)
            first.variant.shouldBeInstanceOf<BookCardVariant.Stacked>().cover shouldBe cover
            first.decorations.selection shouldBe mark
            first.decorations.coverBadge shouldBe badge
            val second = cards.getValue(2)
            second.variant.shouldBeInstanceOf<BookCardVariant.Stacked>().cover.shouldBeNull()
            second.decorations.selection.shouldBeNull()
            second.decorations.coverBadge.shouldBeNull()
            job.cancel()
        }

        @Test
        fun `a cover-only card carries the cover from the state map`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val cover = mockk<CoverUiModel>(relaxed = true)
            val job = launchCollector()

            // ----- Act -----
            stateFlow.value = bookState(
                books = listOf(stubBook(id = 1)),
                layout = LibraryGridLayout.GRID_TWO_COLUMNS_COVER_ONLY,
            ).copy(bookCovers = mapOf(1 to cover))

            // ----- Assert -----
            val variant = stateFlow.value.bookCardsByTab.getValue("all").getValue(1).variant
                .shouldBeInstanceOf<BookCardVariant.CoverOnly>()
            variant.cover.shouldNotBeNull() shouldBe cover
            job.cancel()
        }

        @Test
        fun `an edition card picks up its cover by edition id`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val cover = mockk<CoverUiModel>(relaxed = true)
            val job = launchCollector()

            // ----- Act -----
            stateFlow.value = LibraryUiState(
                visibleTabs = listOf(listTab),
                editionsByTab = mapOf(listTab.id to listOf(stubEdition(id = 7))),
                editionCovers = mapOf(7 to cover),
            )

            // ----- Assert -----
            val variant = stateFlow.value.editionCardsByTab.getValue(listTab.id).getValue(7).variant
                .shouldBeInstanceOf<BookCardVariant.Stacked>()
            variant.cover shouldBe cover
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

        @Test
        fun `a tab keeps its inner map when only another tab's input changes`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val booksA = listOf(stubBook(id = 1))
            val booksB = listOf(stubBook(id = 2))
            val job = launchCollector()
            stateFlow.value = twoTabState(
                booksA = booksA,
                booksB = booksB,
            )
            val before = stateFlow.value.bookCardsByTab

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(
                selectionMarks = persistentMapOf(2 to mockk<SelectionMarkUiModel>(relaxed = true)),
            )

            // ----- Assert -----
            val after = stateFlow.value.bookCardsByTab
            after.getValue(tabA.id) shouldBeSameInstanceAs before.getValue(tabA.id)
            after.getValue(tabB.id) shouldNotBeSameInstanceAs before.getValue(tabB.id)
            job.cancel()
        }

        @Test
        fun `a tab whose input changed is rebuilt with the new value`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val mark = mockk<SelectionMarkUiModel>(relaxed = true)
            val job = launchCollector()
            stateFlow.value = twoTabState(
                booksA = listOf(stubBook(id = 1)),
                booksB = listOf(stubBook(id = 2)),
            )

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(selectionMarks = persistentMapOf(2 to mark))

            // ----- Assert -----
            stateFlow.value.bookCardsByTab.getValue(tabB.id).getValue(2).decorations.selection shouldBe mark
            job.cancel()
        }

        @Test
        fun `a tab keeps its edition map when only another tab's covers change`() = runTest(testDispatcher) {
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
                editionCovers = mapOf(8 to mockk<CoverUiModel>(relaxed = true)),
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
        fun `switching to a list layout and back to a grid rebuilds the cards`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val books = listOf(stubBook(id = 1))
            val job = launchCollector()
            stateFlow.value = bookState(
                books = books,
                layout = LibraryGridLayout.GRID_TWO_COLUMNS,
            )
            val before = stateFlow.value.bookCardsByTab.getValue("all")

            // ----- Act -----
            stateFlow.value = bookState(
                books = books,
                layout = LibraryGridLayout.LIST_COMPACT,
            )
            val onList = stateFlow.value.bookCardsByTab
            stateFlow.value = bookState(
                books = books,
                layout = LibraryGridLayout.GRID_TWO_COLUMNS,
            )

            // ----- Assert -----
            onList.isEmpty() shouldBe true
            val after = stateFlow.value.bookCardsByTab.getValue("all")
            after shouldNotBeSameInstanceAs before
            after.getValue(1).variant.shouldBeInstanceOf<BookCardVariant.Stacked>()
            job.cancel()
        }

        @Test
        fun `switching between stacked and cover-only grids does not serve stale variants`() =
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
                stateFlow.value.bookCardsByTab.getValue("all").getValue(1).variant
                    .shouldBeInstanceOf<BookCardVariant.CoverOnly>()
                job.cancel()
            }
    }
}
