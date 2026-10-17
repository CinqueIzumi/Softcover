package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import kotlin.time.Clock
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import nl.rhaydus.softcover.core.deadlines.domain.usecase.ObserveBookDeadlineUseCase
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookDeadline
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.DeadlineProgress
import nl.rhaydus.softcover.core.domain.model.DeadlineUnit
import nl.rhaydus.softcover.core.domain.model.UserBookRead
import nl.rhaydus.softcover.core.uibinding.deadline.toDeadlineRowUiModel
import nl.rhaydus.softcover.feature.book_detail.presentation.buildBookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BookDeadlineCollectorTest {
    private lateinit var observeBookDeadlineUseCase: ObserveBookDeadlineUseCase
    private lateinit var dependencies: BookDetailDependencies
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>
    private lateinit var deadlineFlow: MutableSharedFlow<BookDeadline?>

    @BeforeEach
    fun setUp() {
        deadlineFlow = MutableSharedFlow()
        observeBookDeadlineUseCase = mockk()
        stateFlow = MutableStateFlow(buildBookDetailUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(BookDetailLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        every {
            observeBookDeadlineUseCase(bookId = any())
        } returns deadlineFlow

        dependencies = mockk<BookDetailDependencies>(relaxed = true).also { mock ->
            every {
                mock.observeBookDeadlineUseCase
            } returns observeBookDeadlineUseCase
        }
    }

    private fun stubBook(
        id: Int = 1,
        pages: Int? = 300,
        currentPage: Int? = 50,
        paused: Boolean = false,
    ): Book {
        val edition = mockk<BookEdition> {
            every {
                this@mockk.pages
            } returns pages

            every {
                this@mockk.audioSeconds
            } returns null

            every {
                this@mockk.isAudiobook
            } returns false
        }
        val userBookRead = if (currentPage != null) {
            mockk<UserBookRead> {
                every {
                    this@mockk.currentPage
                } returns currentPage

                every {
                    this@mockk.currentSeconds
                } returns null
            }
        } else {
            null
        }
        return mockk {
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
                this@mockk.isPaused
            } returns paused
        }
    }

    private fun buildDeadline(
        bookId: Int = 1,
        deadlineDate: LocalDate = LocalDate(
            2026,
            5,
            20,
        ),
        initialPerDay: Float = 10f,
    ) = BookDeadline(
        bookId = bookId,
        deadlineDate = deadlineDate,
        setAt = LocalDate(
            2026,
            4,
            20,
        ),
        initialPerDay = initialPerDay,
        unit = DeadlineUnit.PAGES,
    )

    @Nested
    inner class OnLaunch {
        @Test
        fun `publishes the deadline and its row from progress and date style`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val book = stubBook(
                id = 1,
                pages = 300,
                currentPage = 0,
            )
            stateFlow.value = buildBookDetailUiState().copy(book = book)
            val deadline = buildDeadline(
                bookId = 1,
                initialPerDay = 10f,
            )

            val collector = BookDeadlineCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            deadlineFlow.emit(deadline)

            // ----- Assert -----
            val expected = DeadlineProgress.compute(
                deadline = deadline,
                current = 0,
                total = 300,
            ).toDeadlineRowUiModel(dateStyle = stateFlow.value.dateStyle)
            stateFlow.value.deadlineRow shouldBe expected
            stateFlow.value.deadline shouldBe deadline
            job.cancel()
        }

        @Test
        fun `a paused book publishes an on hold row without a badge`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val book = stubBook(
                id = 1,
                pages = 300,
                currentPage = 0,
                paused = true,
            )
            stateFlow.value = buildBookDetailUiState().copy(book = book)
            val collector = BookDeadlineCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            deadlineFlow.emit(buildDeadline(bookId = 1))

            // ----- Assert -----
            val row = stateFlow.value.deadlineRow.shouldNotBeNull()
            row.badge.shouldBeNull()
            row.goal shouldBe "On hold. Resume to get back on pace."
            job.cancel()
        }

        @Test
        fun `deadlineRow is null while the deadline is kept when the book has no pages`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val book = stubBook(
                    id = 1,
                    pages = null,
                    currentPage = 0,
                )
                stateFlow.value = buildBookDetailUiState().copy(book = book)
                val deadline = buildDeadline(bookId = 1)

                val collector = BookDeadlineCollector()
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Act -----
                deadlineFlow.emit(deadline)

                // ----- Assert -----
                stateFlow.value.deadlineRow shouldBe null
                stateFlow.value.deadline shouldBe deadline
                job.cancel()
            }

        @Test
        fun `deadlineRow is null when deadline emits null`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val book = stubBook(
                id = 1,
                pages = 300,
                currentPage = 0,
            )
            stateFlow.value = buildBookDetailUiState().copy(book = book)

            val collector = BookDeadlineCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            deadlineFlow.emit(null)

            // ----- Assert -----
            stateFlow.value.deadlineRow shouldBe null
            stateFlow.value.deadline shouldBe null
            job.cancel()
        }

        @Test
        fun `goal recomputes when currentPage on the book changes`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val bookId = 1
            val totalPages = 300
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

            val bookBefore = stubBook(
                id = bookId,
                pages = totalPages,
                currentPage = 0,
            )
            stateFlow.value = buildBookDetailUiState().copy(book = bookBefore)

            val deadline = buildDeadline(
                bookId = bookId,
                deadlineDate = today.plus(DatePeriod(days = 30)),
                initialPerDay = 10f,
            )

            val collector = BookDeadlineCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            deadlineFlow.emit(deadline)
            val goalBefore = stateFlow.value.deadlineRow.shouldNotBeNull().goal

            // ----- Act -----
            // Simulate reading 150 of 300 pages, halving the required pace
            val bookAfter = stubBook(
                id = bookId,
                pages = totalPages,
                currentPage = 150,
            )
            stateFlow.value = stateFlow.value.copy(book = bookAfter)

            // ----- Assert -----
            val goalAfter = stateFlow.value.deadlineRow.shouldNotBeNull().goal
            goalAfter shouldNotBe goalBefore
            job.cancel()
        }

        @Test
        fun `deadlineRow is null when book is null`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(book = null)

            val collector = BookDeadlineCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act & Assert -----
            stateFlow.value.deadlineRow shouldBe null
            job.cancel()
        }
    }
}
