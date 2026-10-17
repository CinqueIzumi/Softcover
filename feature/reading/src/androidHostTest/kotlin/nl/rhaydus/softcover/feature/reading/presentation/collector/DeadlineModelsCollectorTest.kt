package nl.rhaydus.softcover.feature.reading.presentation.collector

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlin.time.Clock
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
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
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookDeadline
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.DeadlineUnit
import nl.rhaydus.softcover.core.domain.model.UserBookRead
import nl.rhaydus.softcover.core.domain.util.ObserveCurrentDateUseCase
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class DeadlineModelsCollectorTest {
    private lateinit var dependencies: ReadingScreenDependencies
    private lateinit var stateFlow: MutableStateFlow<ReadingScreenUiState>
    private lateinit var scope: ActionScope<ReadingScreenUiState, ReadingScreenEvent, ReadingLocalVariables>
    private lateinit var observeCurrentDateUseCase: ObserveCurrentDateUseCase
    private lateinit var todayFlow: MutableStateFlow<LocalDate>

    @BeforeEach
    fun setUp() {
        stateFlow = MutableStateFlow(ReadingScreenUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(ReadingLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
        todayFlow = MutableStateFlow(today())
        observeCurrentDateUseCase = mockk()
        every {
            observeCurrentDateUseCase()
        } returns todayFlow

        dependencies = mockk<ReadingScreenDependencies>(relaxed = true).also { mock ->
            every {
                mock.observeCurrentDateUseCase
            } returns observeCurrentDateUseCase
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
            book.currentRead
        } returns userBookRead
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `produces a cover overlay and featured summary for a book with a tracked deadline`() =
            runTest(UnconfinedTestDispatcher()) {
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
                stateFlow.value = ReadingScreenUiState(
                    books = persistentListOf(book),
                    deadlines = persistentMapOf(1 to deadline),
                )

                // ----- Assert -----
                stateFlow.value.deadlineCoverOverlays.keys shouldBe setOf(1)
                stateFlow.value.featuredDeadlineSummary?.text?.substringAfter(" • ") shouldBe "10 pages/day"
                job.cancel()
            }

        @Test
        fun `produces no entry for a book with no tracked deadline`() = runTest(UnconfinedTestDispatcher()) {
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
            stateFlow.value = ReadingScreenUiState(
                books = persistentListOf(book),
                deadlines = persistentMapOf(),
            )

            // ----- Assert -----
            stateFlow.value.deadlineCoverOverlays shouldBe emptyMap()
            stateFlow.value.featuredDeadlineSummary shouldBe null
            job.cancel()
        }

        @Test
        fun `produces no entry for a book with no current edition`() = runTest(UnconfinedTestDispatcher()) {
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
            stateFlow.value = ReadingScreenUiState(
                books = persistentListOf(book),
                deadlines = persistentMapOf(1 to deadline),
            )

            // ----- Assert -----
            stateFlow.value.deadlineCoverOverlays shouldBe emptyMap()
            job.cancel()
        }

        @Test
        fun `a seconds-unit deadline paces in the h m shape`() = runTest(UnconfinedTestDispatcher()) {
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
            stateFlow.value = ReadingScreenUiState(
                books = persistentListOf(book),
                deadlines = persistentMapOf(1 to deadline),
            )

            // ----- Assert -----
            stateFlow.value.featuredDeadlineSummary?.text?.substringAfter(" • ") shouldBe "1h 0m/day"
            job.cancel()
        }

        @Test
        fun `falls back to zero current progress when userBookRead is null`() = runTest(UnconfinedTestDispatcher()) {
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
            stateFlow.value = ReadingScreenUiState(
                books = persistentListOf(book),
                deadlines = persistentMapOf(1 to deadline),
            )

            // ----- Assert -----
            stateFlow.value.featuredDeadlineSummary?.text?.substringAfter(" • ") shouldBe "5 pages/day"
            job.cancel()
        }

        @Test
        fun `featuredDeadlineSummary is null when the featured book has no deadline`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val featuredEdition = stubEdition(pages = 100)
                val featuredBook = stubBook(
                    id = 1,
                    edition = featuredEdition,
                )
                val otherEdition = stubEdition(pages = 200)
                val otherUserBookRead = stubUserBookRead(currentPage = 0)
                val otherBook = stubBook(
                    id = 2,
                    edition = otherEdition,
                    userBookRead = otherUserBookRead,
                )
                val otherDeadline = buildDeadline(
                    bookId = 2,
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
                stateFlow.value = ReadingScreenUiState(
                    books = persistentListOf(featuredBook, otherBook),
                    deadlines = persistentMapOf(2 to otherDeadline),
                )

                // ----- Assert -----
                stateFlow.value.featuredDeadlineSummary shouldBe null
                stateFlow.value.deadlineCoverOverlays.keys shouldBe setOf(2)
                job.cancel()
            }

        @Test
        fun `featuredPaceNudge is visible with the message for an active undismissed deadline`() =
            runTest(UnconfinedTestDispatcher()) {
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
                stateFlow.value = ReadingScreenUiState(
                    books = persistentListOf(book),
                    deadlines = persistentMapOf(1 to deadline),
                )

                // ----- Assert -----
                val nudge = stateFlow.value.featuredPaceNudge.shouldNotBeNull()
                nudge.message shouldBe "Read 10 pages today to stay on pace."
                nudge.visible shouldBe true
                job.cancel()
            }

        @Test
        fun `featuredPaceNudge is hidden but keeps its message when dismissed today`() = runTest(UnconfinedTestDispatcher()) {
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
            stateFlow.value = ReadingScreenUiState(
                books = persistentListOf(book),
                deadlines = persistentMapOf(1 to deadline),
                dismissedPlanTodayByBook = persistentMapOf(1 to today().toString()),
            )

            // ----- Assert -----
            val nudge = stateFlow.value.featuredPaceNudge.shouldNotBeNull()
            nudge.message shouldBe "Read 10 pages today to stay on pace."
            nudge.visible shouldBe false
            job.cancel()
        }

        @Test
        fun `featuredPaceNudge becomes visible again once the dismissal for that book is cleared`() =
            runTest(UnconfinedTestDispatcher()) {
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
                stateFlow.value = ReadingScreenUiState(
                    books = persistentListOf(book),
                    deadlines = persistentMapOf(1 to deadline),
                    dismissedPlanTodayByBook = persistentMapOf(1 to today().toString()),
                )
                val dismissedVisible = stateFlow.value.featuredPaceNudge.shouldNotBeNull().visible
                stateFlow.value = stateFlow.value.copy(dismissedPlanTodayByBook = persistentMapOf())

                // ----- Assert -----
                dismissedVisible shouldBe false
                val nudge = stateFlow.value.featuredPaceNudge.shouldNotBeNull()
                nudge.message shouldBe "Read 10 pages today to stay on pace."
                nudge.visible shouldBe true
                job.cancel()
            }

        @Test
        fun `featuredPaceNudge stays null for an expired deadline`() = runTest(UnconfinedTestDispatcher()) {
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
                daysFromToday = -1,
            )
            val collector = DeadlineModelsCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = ReadingScreenUiState(
                books = persistentListOf(book),
                deadlines = persistentMapOf(1 to deadline),
            )

            // ----- Assert -----
            stateFlow.value.featuredPaceNudge.shouldBeNull()
            job.cancel()
        }

        @Test
        fun `featuredPaceNudge becomes visible again once the date rolls over past the dismissal`() =
            runTest(UnconfinedTestDispatcher()) {
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
                stateFlow.value = ReadingScreenUiState(
                    books = persistentListOf(book),
                    deadlines = persistentMapOf(1 to deadline),
                    dismissedPlanTodayByBook = persistentMapOf(1 to today().toString()),
                )
                val dismissedVisible = stateFlow.value.featuredPaceNudge.shouldNotBeNull().visible
                todayFlow.value = today().plus(DatePeriod(days = 1))

                // ----- Assert -----
                dismissedVisible shouldBe false
                val nudge = stateFlow.value.featuredPaceNudge.shouldNotBeNull()
                nudge.message shouldBe "Read 10 pages today to stay on pace."
                nudge.visible shouldBe true
                job.cancel()
            }

        @Test
        fun `featuredPaceNudge keeps its message and hides once the nudge no longer applies`() =
            runTest(UnconfinedTestDispatcher()) {
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
                stateFlow.value = ReadingScreenUiState(
                    books = persistentListOf(book),
                    deadlines = persistentMapOf(1 to deadline),
                )
                stateFlow.value = stateFlow.value.copy(deadlines = persistentMapOf())

                // ----- Assert -----
                val nudge = stateFlow.value.featuredPaceNudge.shouldNotBeNull()
                nudge.message shouldBe "Read 10 pages today to stay on pace."
                nudge.visible shouldBe false
                job.cancel()
            }
    }
}
