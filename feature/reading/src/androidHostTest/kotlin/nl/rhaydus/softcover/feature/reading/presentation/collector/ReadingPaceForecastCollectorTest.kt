package nl.rhaydus.softcover.feature.reading.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.DeadlineUnit
import nl.rhaydus.softcover.core.domain.model.UserBookRead
import nl.rhaydus.softcover.core.personal.domain.model.ReadingJournalEntry
import nl.rhaydus.softcover.core.personal.domain.usecase.GetReadingJournalHistoryUseCase
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.toad.ActionScope

class ReadingPaceForecastCollectorTest {
    private lateinit var getReadingJournalHistoryUseCase: GetReadingJournalHistoryUseCase
    private lateinit var stateFlow: MutableStateFlow<ReadingScreenUiState>
    private lateinit var scope: ActionScope<ReadingScreenUiState, ReadingScreenEvent, ReadingLocalVariables>

    @BeforeEach
    fun setUp() {
        getReadingJournalHistoryUseCase = mockk()
        stateFlow = MutableStateFlow(ReadingScreenUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(ReadingLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    private fun buildDependencies(testScope: TestScope): ReadingScreenDependencies {
        val dispatcher = UnconfinedTestDispatcher(testScope.testScheduler)
        return mockk<ReadingScreenDependencies>(relaxed = true).also { mock ->
            every {
                mock.getReadingJournalHistoryUseCase
            } returns getReadingJournalHistoryUseCase

            every {
                mock.coroutineScope
            } returns testScope
            every {
                mock.mainDispatcher
            } returns dispatcher

            every {
                mock.launch(any())
            } answers { callOriginal() }
        }
    }

    private fun date(day: Int): LocalDate = LocalDate(
        2026,
        1,
        day,
    )

    private fun journalEntry(
        date: LocalDate,
        pages: Int? = null,
        seconds: Int? = null,
    ): ReadingJournalEntry = ReadingJournalEntry(
        date = date,
        pages = pages,
        seconds = seconds,
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
        id: Int = 1,
        edition: BookEdition,
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
        fun `computes average pace and forecast days from a normal multi-day history`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val edition = stubEdition(pages = 300)
            val userBookRead = stubUserBookRead(currentPage = 90)
            val book = stubBook(
                edition = edition,
                userBookRead = userBookRead,
            )
            val history = listOf(
                journalEntry(
                    date = date(1),
                    pages = 20,
                ),
                journalEntry(
                    date = date(2),
                    pages = 50,
                ),
                journalEntry(
                    date = date(3),
                    pages = 50,
                ),
                journalEntry(
                    date = date(4),
                    pages = 90,
                ),
            )
            coEvery {
                getReadingJournalHistoryUseCase(bookId = any())
            } returns Result.success(history)
            stateFlow.value = ReadingScreenUiState(books = listOf(book))
            val dependencies = buildDependencies(this)
            val collector = ReadingPaceForecastCollector()

            // ----- Act -----
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Assert -----
            stateFlow.value.featuredBookPace?.avgPerReadingDay shouldBe 30f
            stateFlow.value.featuredBookPace?.forecastReadingDays shouldBe 7
            job.cancel()
        }

        @Test
        fun `excludes a zero-delta day from the average pace`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val edition = stubEdition(pages = 180)
            val userBookRead = stubUserBookRead(currentPage = 90)
            val book = stubBook(
                edition = edition,
                userBookRead = userBookRead,
            )
            val history = listOf(
                journalEntry(
                    date = date(1),
                    pages = 30,
                ),
                journalEntry(
                    date = date(2),
                    pages = 30,
                ),
                journalEntry(
                    date = date(3),
                    pages = 30,
                ),
                journalEntry(
                    date = date(4),
                    pages = 90,
                ),
            )
            coEvery {
                getReadingJournalHistoryUseCase(bookId = any())
            } returns Result.success(history)
            stateFlow.value = ReadingScreenUiState(books = listOf(book))
            val dependencies = buildDependencies(this)
            val collector = ReadingPaceForecastCollector()

            // ----- Act -----
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Assert -----
            stateFlow.value.featuredBookPace?.avgPerReadingDay shouldBe 45f
            stateFlow.value.featuredBookPace?.forecastReadingDays shouldBe 2
            job.cancel()
        }

        @Test
        fun `computes a forecast from a single reading day`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val edition = stubEdition(pages = 100)
            val book = stubBook(edition = edition)
            val history = listOf(
                journalEntry(
                    date = date(1),
                    pages = 25,
                ),
            )
            coEvery {
                getReadingJournalHistoryUseCase(bookId = any())
            } returns Result.success(history)
            stateFlow.value = ReadingScreenUiState(books = listOf(book))
            val dependencies = buildDependencies(this)
            val collector = ReadingPaceForecastCollector()

            // ----- Act -----
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Assert -----
            stateFlow.value.featuredBookPace?.avgPerReadingDay shouldBe 25f
            stateFlow.value.featuredBookPace?.forecastReadingDays shouldBe 4
            job.cancel()
        }

        @Test
        fun `does not set featuredBookPace when the book is already finished`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val edition = stubEdition(pages = 200)
            val userBookRead = stubUserBookRead(currentPage = 200)
            val book = stubBook(
                edition = edition,
                userBookRead = userBookRead,
            )
            val history = listOf(
                journalEntry(
                    date = date(1),
                    pages = 50,
                ),
            )
            coEvery {
                getReadingJournalHistoryUseCase(bookId = any())
            } returns Result.success(history)
            stateFlow.value = ReadingScreenUiState(books = listOf(book))
            val dependencies = buildDependencies(this)
            val collector = ReadingPaceForecastCollector()

            // ----- Act -----
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Assert -----
            stateFlow.value.featuredBookPace shouldBe null
            job.cancel()
        }

        @Test
        fun `does not set featuredBookPace when the journal history is empty`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val edition = stubEdition(pages = 100)
            val book = stubBook(edition = edition)
            coEvery {
                getReadingJournalHistoryUseCase(bookId = any())
            } returns Result.success(emptyList())
            stateFlow.value = ReadingScreenUiState(books = listOf(book))
            val dependencies = buildDependencies(this)
            val collector = ReadingPaceForecastCollector()

            // ----- Act -----
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Assert -----
            stateFlow.value.featuredBookPace shouldBe null
            job.cancel()
        }

        @Test
        fun `does not set featuredBookPace when history resolves to zero active reading days`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val edition = stubEdition(pages = 100)
            val userBookRead = stubUserBookRead(currentPage = 10)
            val book = stubBook(
                edition = edition,
                userBookRead = userBookRead,
            )
            val history = listOf(
                journalEntry(
                    date = date(1),
                    pages = 0,
                ),
                journalEntry(
                    date = date(2),
                    pages = 0,
                ),
            )
            coEvery {
                getReadingJournalHistoryUseCase(bookId = any())
            } returns Result.success(history)
            stateFlow.value = ReadingScreenUiState(books = listOf(book))
            val dependencies = buildDependencies(this)
            val collector = ReadingPaceForecastCollector()

            // ----- Act -----
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Assert -----
            stateFlow.value.featuredBookPace shouldBe null
            job.cancel()
        }

        @Test
        fun `computes an audiobook forecast in seconds when the edition has no page count`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val edition = stubEdition(audioSeconds = 7200)
            val userBookRead = stubUserBookRead(currentSeconds = 3600)
            val book = stubBook(
                edition = edition,
                userBookRead = userBookRead,
            )
            val history = listOf(
                journalEntry(
                    date = date(1),
                    seconds = 1800,
                ),
                journalEntry(
                    date = date(2),
                    seconds = 3600,
                ),
            )
            coEvery {
                getReadingJournalHistoryUseCase(bookId = any())
            } returns Result.success(history)
            stateFlow.value = ReadingScreenUiState(books = listOf(book))
            val dependencies = buildDependencies(this)
            val collector = ReadingPaceForecastCollector()

            // ----- Act -----
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Assert -----
            stateFlow.value.featuredBookPace?.unit shouldBe DeadlineUnit.SECONDS
            stateFlow.value.featuredBookPace?.avgPerReadingDay shouldBe 1800f
            stateFlow.value.featuredBookPace?.forecastReadingDays shouldBe 2
            job.cancel()
        }

        @Test
        fun `does not set featuredBookPace and never invokes the journal history use case when there is no featured book`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                stateFlow.value = ReadingScreenUiState(books = emptyList())
                val dependencies = buildDependencies(this)
                val collector = ReadingPaceForecastCollector()

                // ----- Act -----
                val job = launch { collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                ) }

                // ----- Assert -----
                stateFlow.value.featuredBookPace shouldBe null
                coVerify(exactly = 0) { getReadingJournalHistoryUseCase(bookId = any()) }
                job.cancel()
            }
    }
}
