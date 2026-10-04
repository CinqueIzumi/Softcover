package nl.rhaydus.softcover.feature.reading.presentation.collector

import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import nl.rhaydus.softcover.core.component.badge.BadgeTone
import nl.rhaydus.softcover.core.component.badge.BadgeUiModel
import nl.rhaydus.softcover.core.component.badge.CoverOverlayCorner
import nl.rhaydus.softcover.core.component.badge.CoverOverlayUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardEyebrow
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.core.component.bookcard.BookCardLead
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardVariant
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookDeadline
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.DeadlineUnit
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.time.Clock

class BookCardModelsCollectorTest {
    private lateinit var dependencies: ReadingScreenDependencies
    private lateinit var stateFlow: MutableStateFlow<ReadingScreenUiState>
    private lateinit var scope: ActionScope<ReadingScreenUiState, ReadingScreenEvent, ReadingLocalVariables>

    private val overlay = CoverOverlayUiModel(
        badge = BadgeUiModel(
            label = "Behind",
            tone = BadgeTone.Primary,
        ),
        corner = CoverOverlayCorner.TopStart,
        grayscale = false,
    )

    @BeforeEach
    fun setUp() {
        stateFlow = MutableStateFlow(ReadingScreenUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(ReadingLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
        dependencies = mockk<ReadingScreenDependencies>(relaxed = true)
    }

    private fun book(id: Int): Book = Book(
        id = id,
        title = "Title $id",
        editions = emptyList(),
        defaultEdition = BookEdition(
            id = 100 + id,
            canonicalId = null,
            bookId = id,
            publisher = null,
            title = null,
            url = null,
            localImagePath = null,
            isbn10 = null,
            isbn13 = null,
            pages = 100,
            audioSeconds = null,
            authors = emptyList(),
            releaseYear = 2020,
            format = "Paperback",
            owned = false,
        ),
        rating = 0.0,
        description = "",
        releaseYear = 2020,
        coverUrl = "",
        authors = emptyList(),
        usersCount = 0,
        ratingsCount = 0,
        bookSeries = null,
        positionsInSeries = emptyList(),
        isCompilation = false,
        userBook = null,
        userBookRead = null,
    )

    private fun List<BookCardUiModel>.card(id: Int): BookCardUiModel = first { (it.key as BookCardKey.Book).bookId == id }

    private fun BookCardUiModel.row(): BookCardVariant.Row =
        variant.shouldBeInstanceOf<BookCardVariant.Row>()

    @Nested
    inner class OnLaunch {
        @Test
        fun `skips the featured book and orders the rows like the source books`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val job = launch {
                BookCardModelsCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(
                books = listOf(
                    book(1),
                    book(2),
                    book(3),
                ),
            )

            // ----- Assert -----
            stateFlow.value.alsoReadingCards.map { it.key } shouldBe
                listOf(BookCardKey.Book(bookId = 2), BookCardKey.Book(bookId = 3))
            stateFlow.value.alsoReadingCards.card(2).key shouldBe BookCardKey.Book(bookId = 2)
            job.cancel()
        }

        @Test
        fun `shows also reading stand-ins while no books have landed`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val job = launch {
                BookCardModelsCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(books = emptyList())

            // ----- Assert -----
            stateFlow.value.alsoReadingCards.shouldNotBeEmpty()
            stateFlow.value.alsoReadingCards.forEach { it.isLoading shouldBe true }
            job.cancel()
        }

        @Test
        fun `maps a single landed book to an empty also reading list`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val job = launch {
                BookCardModelsCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(books = listOf(book(1)))

            // ----- Assert -----
            stateFlow.value.alsoReadingCards shouldBe emptyList()
            job.cancel()
        }

        @Test
        fun `keeps the rows in the order of the source books`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val job = launch {
                BookCardModelsCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(
                books = listOf(
                    book(1),
                    book(5),
                    book(3),
                    book(4),
                ),
            )

            // ----- Assert -----
            stateFlow.value.alsoReadingCards.map { it.key } shouldBe
                listOf(
                    BookCardKey.Book(bookId = 5),
                    BookCardKey.Book(bookId = 3),
                    BookCardKey.Book(bookId = 4),
                )
            job.cancel()
        }

        @Test
        fun `builds real rows rather than stand-ins when there are books beyond the featured one`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val job = launch {
                    BookCardModelsCollector().onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Act -----
                stateFlow.value = stateFlow.value.copy(books = listOf(book(1), book(2)))

                // ----- Assert -----
                stateFlow.value.alsoReadingCards.size shouldBe 1
                stateFlow.value.alsoReadingCards.single().isLoading shouldBe false
                job.cancel()
            }

        @Test
        fun `builds at most three pick up next tiles in order`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val job = launch {
                BookCardModelsCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(wantToReadBooks = (1..5).map { book(it) })

            // ----- Assert -----
            stateFlow.value.pickUpNextCards.map { it.key } shouldBe listOf(
                BookCardKey.Book(bookId = 1),
                BookCardKey.Book(bookId = 2),
                BookCardKey.Book(bookId = 3),
            )
            job.cancel()
        }

        @Test
        fun `shows pick up next stand-ins while the want to read shelf has not loaded`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val job = launch {
                    BookCardModelsCollector().onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Act -----
                stateFlow.value = stateFlow.value.copy(books = listOf(book(1), book(2)))

                // ----- Assert -----
                stateFlow.value.pickUpNextCards.shouldNotBeEmpty()
                stateFlow.value.pickUpNextCards.forEach { it.isLoading shouldBe true }
                job.cancel()
            }

        @Test
        fun `maps a loaded but empty want to read shelf to an empty list`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val job = launch {
                    BookCardModelsCollector().onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Act -----
                stateFlow.value = stateFlow.value.copy(wantToReadBooks = emptyList())

                // ----- Assert -----
                stateFlow.value.pickUpNextCards shouldBe emptyList()
                job.cancel()
            }

        @Test
        fun `loading to landed swaps the also reading stand-ins for the real rows`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val job = launch {
                    BookCardModelsCollector().onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }
                val loading = stateFlow.value.alsoReadingCards

                // ----- Act -----
                stateFlow.value = stateFlow.value.copy(books = listOf(book(1), book(2), book(3)))

                // ----- Assert -----
                loading.forEach { it.isLoading shouldBe true }
                stateFlow.value.alsoReadingCards.map { it.key } shouldBe
                    listOf(BookCardKey.Book(bookId = 2), BookCardKey.Book(bookId = 3))
                stateFlow.value.alsoReadingCards.forEach { it.isLoading shouldBe false }
                job.cancel()
            }

        @Test
        fun `real pick up next tiles are not loading cards`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val job = launch {
                BookCardModelsCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(wantToReadBooks = listOf(book(1), book(2)))

            // ----- Assert -----
            stateFlow.value.pickUpNextCards.forEach { it.isLoading shouldBe false }
            job.cancel()
        }

        @Test
        fun `a landed shelf that later empties maps to an empty list rather than stand-ins`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val job = launch {
                    BookCardModelsCollector().onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }
                stateFlow.value = stateFlow.value.copy(wantToReadBooks = listOf(book(1)))

                // ----- Act -----
                stateFlow.value = stateFlow.value.copy(wantToReadBooks = emptyList())

                // ----- Assert -----
                stateFlow.value.pickUpNextCards shouldBe emptyList()
                job.cancel()
            }

        @Test
        fun `a shelf that loads after being null replaces the loading state`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val job = launch {
                    BookCardModelsCollector().onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }
                val loading = stateFlow.value.pickUpNextCards

                // ----- Act -----
                stateFlow.value = stateFlow.value.copy(wantToReadBooks = listOf(book(7)))

                // ----- Assert -----
                loading.forEach { it.isLoading shouldBe true }
                stateFlow.value.pickUpNextCards.map { it.key } shouldBe
                    listOf(BookCardKey.Book(bookId = 7))
                job.cancel()
            }

        @Test
        fun `builds fewer tiles when fewer books are wanted`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val job = launch {
                BookCardModelsCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(wantToReadBooks = listOf(book(1)))

            // ----- Assert -----
            stateFlow.value.pickUpNextCards.size shouldBe 1
            job.cancel()
        }

        @Test
        fun `a failed save flips the row into its error state and clearing it restores the row`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val job = launch {
                    BookCardModelsCollector().onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }
                stateFlow.value = stateFlow.value.copy(
                    books = listOf(
                        book(1),
                        book(2),
                        book(3),
                    ),
                )

                // ----- Act -----
                stateFlow.value = stateFlow.value.copy(failedMutationBookIds = setOf(2))
                val failed = stateFlow.value.alsoReadingCards
                stateFlow.value = stateFlow.value.copy(failedMutationBookIds = emptySet())

                // ----- Assert -----
                failed.card(2).decorations.shakeOnError shouldBe true
                failed.card(2).row().text.eyebrow?.tone shouldBe BookCardEyebrow.Tone.Error
                failed.card(3).decorations.shakeOnError shouldBe false
                stateFlow.value.alsoReadingCards.card(2).decorations.shakeOnError shouldBe false
                job.cancel()
            }

        @Test
        fun `a deadline change adds the summary to the row`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val job = launch {
                BookCardModelsCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }
            stateFlow.value = stateFlow.value.copy(
                books = listOf(
                    book(1),
                    book(2),
                ),
            )
            val before = stateFlow.value.alsoReadingCards.card(2).row().deadline

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(
                deadlines = mapOf(
                    2 to BookDeadline(
                        bookId = 2,
                        deadlineDate = today.plus(DatePeriod(days = 10)),
                        setAt = today,
                        initialPerDay = 10f,
                        unit = DeadlineUnit.PAGES,
                    ),
                ),
            )

            // ----- Assert -----
            before shouldBe null
            stateFlow.value.alsoReadingCards.card(2).row().deadline?.text?.substringAfter(" • ") shouldBe "10 pages/day"
            job.cancel()
        }

        @Test
        fun `a cover overlay change reaches the row badge`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val job = launch {
                BookCardModelsCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }
            stateFlow.value = stateFlow.value.copy(
                books = listOf(
                    book(1),
                    book(2),
                ),
            )

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(deadlineCoverOverlays = mapOf(2 to overlay))

            // ----- Assert -----
            val lead = stateFlow.value.alsoReadingCards.card(2)
                .variant.shouldBeInstanceOf<BookCardVariant.Row>()
                .lead.shouldBeInstanceOf<BookCardLead.Cover>()
            lead.cover.badge shouldBe overlay
            job.cancel()
        }
    }
}
