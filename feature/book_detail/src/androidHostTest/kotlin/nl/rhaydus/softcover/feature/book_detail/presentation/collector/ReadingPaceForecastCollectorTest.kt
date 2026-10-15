package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.DeadlineUnit
import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.core.domain.model.UserBookRead
import nl.rhaydus.softcover.core.personal.domain.model.ReadingJournalEntry
import nl.rhaydus.softcover.core.personal.domain.usecase.ObserveReadingJournalHistoryUseCase
import nl.rhaydus.softcover.core.personal.domain.usecase.RefreshReadingJournalHistoryUseCase
import nl.rhaydus.softcover.feature.book_detail.presentation.buildBookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ReadingPaceForecastCollectorTest {
    private lateinit var observeReadingJournalHistoryUseCase: ObserveReadingJournalHistoryUseCase
    private lateinit var refreshReadingJournalHistoryUseCase: RefreshReadingJournalHistoryUseCase
    private lateinit var dependencies: BookDetailDependencies
    private val historyFlows = mutableMapOf<Int, MutableStateFlow<List<ReadingJournalEntry>>>()
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        observeReadingJournalHistoryUseCase = mockk()
        refreshReadingJournalHistoryUseCase = mockk()
        stateFlow = MutableStateFlow(buildBookDetailUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(BookDetailLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        every {
            observeReadingJournalHistoryUseCase(bookId = any())
        } answers { historyFlow(firstArg()) }

        coEvery {
            refreshReadingJournalHistoryUseCase(bookId = any())
        } returns Result.success(Unit)

        dependencies = mockk<BookDetailDependencies>(relaxed = true).also { mock ->
            every {
                mock.observeReadingJournalHistoryUseCase
            } returns observeReadingJournalHistoryUseCase

            every {
                mock.refreshReadingJournalHistoryUseCase
            } returns refreshReadingJournalHistoryUseCase
        }
    }

    private fun historyFlow(bookId: Int): MutableStateFlow<List<ReadingJournalEntry>> =
        historyFlows.getOrPut(bookId) { MutableStateFlow(emptyList()) }

    private fun entry(
        day: Int,
        pages: Int? = null,
        seconds: Int? = null,
    ): ReadingJournalEntry = ReadingJournalEntry(
        date = LocalDate(
            2026,
            1,
            day,
        ),
        pages = pages,
        seconds = seconds,
    )

    private fun stubBook(
        id: Int = 1,
        status: BookStatus = BookStatus.Reading,
        pages: Int? = 300,
        audioSeconds: Int? = null,
        currentPage: Int = 100,
        currentSeconds: Int = 0,
    ): Book {
        val edition = mockk<BookEdition> {
            every {
                this@mockk.pages
            } returns pages

            every {
                this@mockk.audioSeconds
            } returns audioSeconds
        }
        val userBookRead = mockk<UserBookRead> {
            every {
                this@mockk.currentPage
            } returns currentPage

            every {
                this@mockk.currentSeconds
            } returns currentSeconds
        }
        val userBook = mockk<UserBook> {
            every {
                this@mockk.status
            } returns status
        }

        return mockk<Book> {
            every {
                this@mockk.id
            } returns id

            every {
                this@mockk.currentEdition
            } returns edition

            every {
                this@mockk.currentRead
            } returns userBookRead

            every {
                this@mockk.userBook
            } returns userBook
        }
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `computes the pace forecast from the observed history`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            historyFlow(1).value = listOf(
                entry(
                    day = 1,
                    pages = 20,
                ),
                entry(
                    day = 2,
                    pages = 50,
                ),
                entry(
                    day = 3,
                    pages = 50,
                ),
                entry(
                    day = 4,
                    pages = 90,
                ),
            )
            stateFlow.value = buildBookDetailUiState().copy(book = stubBook(currentPage = 90))
            val collector = ReadingPaceForecastCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            val forecast = stateFlow.value.readingPaceForecast.shouldNotBeNull()
            forecast.avgPerReadingDay shouldBe 30f
            forecast.remainingUnits shouldBe 210
            forecast.forecastReadingDays shouldBe 7
            forecast.unit shouldBe DeadlineUnit.PAGES
            job.cancel()
        }

        @Test
        fun `computes an audiobook forecast in seconds when the edition has no page count`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            historyFlow(1).value = listOf(
                entry(
                    day = 1,
                    seconds = 1800,
                ),
                entry(
                    day = 2,
                    seconds = 3600,
                ),
            )
            stateFlow.value = buildBookDetailUiState().copy(
                book = stubBook(
                    pages = null,
                    audioSeconds = 7200,
                    currentSeconds = 3600,
                ),
            )
            val collector = ReadingPaceForecastCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            val forecast = stateFlow.value.readingPaceForecast.shouldNotBeNull()
            forecast.unit shouldBe DeadlineUnit.SECONDS
            forecast.remainingUnits shouldBe 3600
            forecast.forecastReadingDays shouldBe 2
            job.cancel()
        }

        @Test
        fun `updates the pace when the history emits a new entry`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            historyFlow(1).value = listOf(
                entry(
                    day = 1,
                    pages = 50,
                ),
            )
            stateFlow.value = buildBookDetailUiState().copy(book = stubBook(currentPage = 100))
            val collector = ReadingPaceForecastCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }
            val forecastBefore = stateFlow.value.readingPaceForecast.shouldNotBeNull()

            // ----- Act -----
            historyFlow(1).value = listOf(
                entry(
                    day = 1,
                    pages = 50,
                ),
                entry(
                    day = 2,
                    pages = 150,
                ),
            )

            // ----- Assert -----
            forecastBefore.forecastReadingDays shouldBe 4
            val forecastAfter = stateFlow.value.readingPaceForecast.shouldNotBeNull()
            forecastAfter.avgPerReadingDay shouldBe 75f
            forecastAfter.forecastReadingDays shouldBe 3
            job.cancel()
        }

        @Test
        fun `refreshes once per book id despite progress changes`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(book = stubBook(currentPage = 10))
            val collector = ReadingPaceForecastCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = buildBookDetailUiState().copy(book = stubBook(currentPage = 40))

            // ----- Assert -----
            coVerify(exactly = 1) {
                refreshReadingJournalHistoryUseCase(bookId = 1)
            }
            job.cancel()
        }

        @Test
        fun `refreshes again when the book id changes`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(book = stubBook(id = 1))
            val collector = ReadingPaceForecastCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = buildBookDetailUiState().copy(book = stubBook(id = 2))

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
                entry(
                    day = 1,
                    pages = 25,
                ),
            )
            stateFlow.value = buildBookDetailUiState().copy(book = stubBook(currentPage = 0))
            val collector = ReadingPaceForecastCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            val forecast = stateFlow.value.readingPaceForecast.shouldNotBeNull()
            forecast.avgPerReadingDay shouldBe 25f
            job.cancel()
        }

        @Test
        fun `gives no pace and skips history when the book is not being read`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            historyFlow(1).value = listOf(
                entry(
                    day = 1,
                    pages = 25,
                ),
            )
            stateFlow.value = buildBookDetailUiState().copy(book = stubBook(status = BookStatus.WantToRead))
            val collector = ReadingPaceForecastCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.readingPaceForecast shouldBe null
            verify(exactly = 0) {
                observeReadingJournalHistoryUseCase(bookId = any())
            }
            coVerify(exactly = 0) {
                refreshReadingJournalHistoryUseCase(bookId = any())
            }
            job.cancel()
        }

        @Test
        fun `clears the pace when the book stops being read`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            historyFlow(1).value = listOf(
                entry(
                    day = 1,
                    pages = 25,
                ),
            )
            stateFlow.value = buildBookDetailUiState().copy(book = stubBook(currentPage = 0))
            val collector = ReadingPaceForecastCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = buildBookDetailUiState().copy(book = stubBook(status = BookStatus.Read))

            // ----- Assert -----
            stateFlow.value.readingPaceForecast shouldBe null
            job.cancel()
        }
    }
}
