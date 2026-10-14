package nl.rhaydus.softcover.feature.reading.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.UserBookRead
import nl.rhaydus.softcover.core.personal.domain.model.ReadingJournalEntry
import nl.rhaydus.softcover.core.personal.domain.usecase.ObserveReadingJournalHistoryUseCase
import nl.rhaydus.softcover.core.personal.domain.usecase.RefreshReadingJournalHistoryUseCase
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ReadingPaceForecastCollectorTest {
    private lateinit var observeReadingJournalHistoryUseCase: ObserveReadingJournalHistoryUseCase
    private lateinit var refreshReadingJournalHistoryUseCase: RefreshReadingJournalHistoryUseCase
    private val historyFlows = mutableMapOf<Int, MutableStateFlow<List<ReadingJournalEntry>>>()
    private lateinit var stateFlow: MutableStateFlow<ReadingScreenUiState>
    private lateinit var scope: ActionScope<ReadingScreenUiState, ReadingScreenEvent, ReadingLocalVariables>

    @BeforeEach
    fun setUp() {
        observeReadingJournalHistoryUseCase = mockk()
        refreshReadingJournalHistoryUseCase = mockk()
        every {
            observeReadingJournalHistoryUseCase(bookId = any())
        } answers { historyFlow(firstArg()) }
        coEvery {
            refreshReadingJournalHistoryUseCase(bookId = any())
        } returns Result.success(Unit)
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
                mock.observeReadingJournalHistoryUseCase
            } returns observeReadingJournalHistoryUseCase

            every {
                mock.refreshReadingJournalHistoryUseCase
            } returns refreshReadingJournalHistoryUseCase

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

    private fun historyFlow(bookId: Int): MutableStateFlow<List<ReadingJournalEntry>> =
        historyFlows.getOrPut(bookId) { MutableStateFlow(emptyList()) }

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
            historyFlow(1).value = history
            stateFlow.value = ReadingScreenUiState(books = persistentListOf(book))
            val dependencies = buildDependencies(this)
            val collector = ReadingPaceForecastCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.featuredBookPace shouldBe "30 pages/day · about 7 days to go"
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
            historyFlow(1).value = history
            stateFlow.value = ReadingScreenUiState(books = persistentListOf(book))
            val dependencies = buildDependencies(this)
            val collector = ReadingPaceForecastCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.featuredBookPace shouldBe "45 pages/day · about 2 days to go"
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
            historyFlow(1).value = history
            stateFlow.value = ReadingScreenUiState(books = persistentListOf(book))
            val dependencies = buildDependencies(this)
            val collector = ReadingPaceForecastCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.featuredBookPace shouldBe "25 pages/day · about 4 days to go"
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
            historyFlow(1).value = history
            stateFlow.value = ReadingScreenUiState(books = persistentListOf(book))
            val dependencies = buildDependencies(this)
            val collector = ReadingPaceForecastCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

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
            historyFlow(1).value = history
            stateFlow.value = ReadingScreenUiState(books = persistentListOf(book))
            val dependencies = buildDependencies(this)
            val collector = ReadingPaceForecastCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

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
            historyFlow(1).value = history
            stateFlow.value = ReadingScreenUiState(books = persistentListOf(book))
            val dependencies = buildDependencies(this)
            val collector = ReadingPaceForecastCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.featuredBookPace shouldBe "0h 30m/day · about 2 days to go"
            job.cancel()
        }

        @Test
        fun `does not set featuredBookPace and never observes or refreshes the journal history when there is no featured book`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                stateFlow.value = ReadingScreenUiState(books = persistentListOf())
                val dependencies = buildDependencies(this)
                val collector = ReadingPaceForecastCollector()

                // ----- Act -----
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Assert -----
                stateFlow.value.featuredBookPace shouldBe null
                verify(exactly = 0) {
                    observeReadingJournalHistoryUseCase(bookId = any())
                }
                coVerify(exactly = 0) {
                    refreshReadingJournalHistoryUseCase(bookId = any())
                }
                job.cancel()
            }

        @Test
        fun `updates the pace when the history emits a new entry`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val book = stubBook(
                edition = stubEdition(pages = 300),
                userBookRead = stubUserBookRead(currentPage = 100),
            )
            historyFlow(1).value = listOf(
                journalEntry(
                    date = date(1),
                    pages = 50,
                ),
            )
            stateFlow.value = ReadingScreenUiState(books = persistentListOf(book))
            val dependencies = buildDependencies(this)
            val collector = ReadingPaceForecastCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }
            val paceBefore = stateFlow.value.featuredBookPace

            // ----- Act -----
            historyFlow(1).value = listOf(
                journalEntry(
                    date = date(1),
                    pages = 50,
                ),
                journalEntry(
                    date = date(2),
                    pages = 150,
                ),
            )

            // ----- Assert -----
            paceBefore shouldBe "50 pages/day · about 4 days to go"
            stateFlow.value.featuredBookPace shouldBe "75 pages/day · about 3 days to go"
            job.cancel()
        }

        @Test
        fun `refreshes once per book id despite progress changes`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val edition = stubEdition(pages = 300)
            stateFlow.value = ReadingScreenUiState(
                books = persistentListOf(
                    stubBook(
                        edition = edition,
                        userBookRead = stubUserBookRead(currentPage = 10),
                    ),
                ),
            )
            val dependencies = buildDependencies(this)
            val collector = ReadingPaceForecastCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = ReadingScreenUiState(
                books = persistentListOf(
                    stubBook(
                        edition = edition,
                        userBookRead = stubUserBookRead(currentPage = 40),
                    ),
                ),
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                refreshReadingJournalHistoryUseCase(bookId = 1)
            }
            job.cancel()
        }

        @Test
        fun `refreshes again when the featured book id changes`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val edition = stubEdition(pages = 300)
            stateFlow.value = ReadingScreenUiState(
                books = persistentListOf(
                    stubBook(
                        id = 1,
                        edition = edition,
                    ),
                ),
            )
            val dependencies = buildDependencies(this)
            val collector = ReadingPaceForecastCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = ReadingScreenUiState(
                books = persistentListOf(
                    stubBook(
                        id = 2,
                        edition = edition,
                    ),
                ),
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                refreshReadingJournalHistoryUseCase(bookId = 1)
            }
            coVerify(exactly = 1) {
                refreshReadingJournalHistoryUseCase(bookId = 2)
            }
            job.cancel()
        }

        @Test
        fun `still shows the pace from local history when the refresh fails`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            coEvery {
                refreshReadingJournalHistoryUseCase(bookId = any())
            } returns Result.failure(IllegalStateException("offline"))
            historyFlow(1).value = listOf(
                journalEntry(
                    date = date(1),
                    pages = 25,
                ),
            )
            stateFlow.value = ReadingScreenUiState(
                books = persistentListOf(stubBook(edition = stubEdition(pages = 100))),
            )
            val dependencies = buildDependencies(this)
            val collector = ReadingPaceForecastCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.featuredBookPace shouldBe "25 pages/day · about 4 days to go"
            job.cancel()
        }
    }
}
