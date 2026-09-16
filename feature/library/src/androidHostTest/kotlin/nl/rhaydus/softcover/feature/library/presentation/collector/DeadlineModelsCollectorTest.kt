package nl.rhaydus.softcover.feature.library.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlin.time.Clock
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookDeadline
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.DeadlineUnit
import nl.rhaydus.softcover.core.domain.model.UserBookRead
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope

class DeadlineModelsCollectorTest {
    private lateinit var dependencies: LibraryDependencies
    private lateinit var stateFlow: MutableStateFlow<LibraryUiState>
    private lateinit var scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>

    private val testDispatcher = UnconfinedTestDispatcher()

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

    private fun today(): LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())

    private fun buildDeadline(
        bookId: Int,
        unit: DeadlineUnit,
        daysFromToday: Int,
    ): BookDeadline = BookDeadline(
        bookId = bookId,
        deadlineDate = today().plus(DatePeriod(days = daysFromToday)),
        setAt = today(),
        initialPerDay = 10f,
        unit = unit,
    )

    private fun stubEdition(
        pages: Int? = null,
        audioSeconds: Int? = null,
    ): BookEdition = mockk<BookEdition>(relaxed = true).also { edition ->
        every {
            edition.pages
        } returns pages

        every {
            edition.audioSeconds
        } returns audioSeconds
    }

    private fun stubUserBookRead(
        currentPage: Int? = null,
        currentSeconds: Int? = null,
    ): UserBookRead = mockk<UserBookRead>(relaxed = true).also { read ->
        every {
            read.currentPage
        } returns currentPage

        every {
            read.currentSeconds
        } returns currentSeconds
    }

    private fun stubBook(
        id: Int,
        edition: BookEdition?,
        userBookRead: UserBookRead? = null,
    ): Book = mockk<Book>(relaxed = true).also { book ->
        every {
            book.id
        } returns id

        every {
            book.currentEdition
        } returns edition

        every {
            book.userBookRead
        } returns userBookRead
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `produces progress, badge, and summary entries for a book with a tracked deadline`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val edition = stubEdition(pages = 100)
            val userBookRead = stubUserBookRead(currentPage = 0)
            val book = stubBook(
                id = 1,
                edition = edition,
                userBookRead = userBookRead,
            )
            val deadline = buildDeadline(
                bookId = 1,
                unit = DeadlineUnit.PAGES,
                daysFromToday = 10,
            )
            val collector = DeadlineModelsCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = LibraryUiState(
                booksByTab = mapOf("all" to listOf(book)),
                deadlines = mapOf(1 to deadline),
            )

            // ----- Assert -----
            stateFlow.value.deadlineProgressByBook.keys shouldBe setOf(1)
            stateFlow.value.deadlineBadges.keys shouldBe setOf(1)
            stateFlow.value.deadlineSummaries.keys shouldBe setOf(1)
            stateFlow.value.deadlineSummaries.getValue(1).paceText shouldBe "10 pages/day"
            job.cancel()
        }

        @Test
        fun `produces no entry for a book with no tracked deadline`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val edition = stubEdition(pages = 100)
            val book = stubBook(
                id = 1,
                edition = edition,
            )
            val collector = DeadlineModelsCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = LibraryUiState(
                booksByTab = mapOf("all" to listOf(book)),
                deadlines = emptyMap(),
            )

            // ----- Assert -----
            stateFlow.value.deadlineProgressByBook shouldBe emptyMap()
            stateFlow.value.deadlineBadges shouldBe emptyMap()
            stateFlow.value.deadlineSummaries shouldBe emptyMap()
            job.cancel()
        }

        @Test
        fun `produces no entry for a book with no current edition`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val book = stubBook(
                id = 1,
                edition = null,
            )
            val deadline = buildDeadline(
                bookId = 1,
                unit = DeadlineUnit.PAGES,
                daysFromToday = 10,
            )
            val collector = DeadlineModelsCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = LibraryUiState(
                booksByTab = mapOf("all" to listOf(book)),
                deadlines = mapOf(1 to deadline),
            )

            // ----- Assert -----
            stateFlow.value.deadlineProgressByBook shouldBe emptyMap()
            job.cancel()
        }

        @Test
        fun `a seconds-unit deadline paces in the h m shape`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val edition = stubEdition(audioSeconds = 36000)
            val userBookRead = stubUserBookRead(currentSeconds = 0)
            val book = stubBook(
                id = 1,
                edition = edition,
                userBookRead = userBookRead,
            )
            val deadline = buildDeadline(
                bookId = 1,
                unit = DeadlineUnit.SECONDS,
                daysFromToday = 10,
            )
            val collector = DeadlineModelsCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = LibraryUiState(
                booksByTab = mapOf("all" to listOf(book)),
                deadlines = mapOf(1 to deadline),
            )

            // ----- Assert -----
            stateFlow.value.deadlineSummaries.getValue(1).paceText shouldBe "1h 0m/day"
            job.cancel()
        }

        @Test
        fun `falls back to zero current progress when userBookRead is null`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val edition = stubEdition(pages = 50)
            val book = stubBook(
                id = 1,
                edition = edition,
                userBookRead = null,
            )
            val deadline = buildDeadline(
                bookId = 1,
                unit = DeadlineUnit.PAGES,
                daysFromToday = 10,
            )
            val collector = DeadlineModelsCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = LibraryUiState(
                booksByTab = mapOf("all" to listOf(book)),
                deadlines = mapOf(1 to deadline),
            )

            // ----- Assert -----
            stateFlow.value.deadlineSummaries.getValue(1).paceText shouldBe "5 pages/day"
            job.cancel()
        }

        @Test
        fun `dedups a book present in two tabs into one entry`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val edition = stubEdition(pages = 100)
            val userBookRead = stubUserBookRead(currentPage = 0)
            val book = stubBook(
                id = 1,
                edition = edition,
                userBookRead = userBookRead,
            )
            val deadline = buildDeadline(
                bookId = 1,
                unit = DeadlineUnit.PAGES,
                daysFromToday = 10,
            )
            val collector = DeadlineModelsCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = LibraryUiState(
                booksByTab = mapOf(
                    "all" to listOf(book),
                    "currentlyReading" to listOf(book),
                ),
                deadlines = mapOf(1 to deadline),
            )

            // ----- Assert -----
            stateFlow.value.deadlineProgressByBook.keys shouldBe setOf(1)
            job.cancel()
        }
    }
}
