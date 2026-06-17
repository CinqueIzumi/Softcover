package nl.rhaydus.softcover.feature.book_detail.presentation.flows

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.deadlines.domain.usecase.ObserveBookDeadlineUseCase
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookDeadline
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.DeadlineUnit
import nl.rhaydus.softcover.core.domain.model.UserBookRead
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

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
        stateFlow = MutableStateFlow(BookDetailUiState())
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
                this@mockk.userBookRead
            } returns userBookRead
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
        fun `sets deadlineProgress when book and deadline are present`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val book = stubBook(
                id = 1,
                pages = 300,
                currentPage = 0,
            )
            stateFlow.value = BookDetailUiState(book = book)
            val deadline = buildDeadline(
                bookId = 1,
                initialPerDay = 10f,
            )

            val collector = BookDeadlineCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            deadlineFlow.emit(deadline)

            // ----- Assert -----
            stateFlow.value.deadlineProgress shouldNotBe null
            stateFlow.value.deadline shouldBe deadline
            job.cancel()
        }

        @Test
        fun `deadlineProgress is null when deadline emits null`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val book = stubBook(
                id = 1,
                pages = 300,
                currentPage = 0,
            )
            stateFlow.value = BookDetailUiState(book = book)

            val collector = BookDeadlineCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            deadlineFlow.emit(null)

            // ----- Assert -----
            stateFlow.value.deadlineProgress shouldBe null
            stateFlow.value.deadline shouldBe null
            job.cancel()
        }

        @Test
        fun `deadlineProgress is null when book has no pages`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val book = stubBook(
                id = 1,
                pages = null,
                currentPage = 0,
            )
            stateFlow.value = BookDetailUiState(book = book)
            val deadline = buildDeadline(bookId = 1)

            val collector = BookDeadlineCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            deadlineFlow.emit(deadline)

            // ----- Assert -----
            stateFlow.value.deadlineProgress shouldBe null
            stateFlow.value.deadline shouldBe deadline
            job.cancel()
        }

        @Test
        fun `requiredPerDay recomputes when currentPage on the book changes`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val bookId = 1
            val totalPages = 300

            // First book: currentPage = 0 → 300 pages remaining / 30 days = 10 p/day
            val bookBefore = stubBook(
                id = bookId,
                pages = totalPages,
                currentPage = 0,
            )
            stateFlow.value = BookDetailUiState(book = bookBefore)

            val deadline = buildDeadline(
                bookId = bookId,
                initialPerDay = 10f,
            )

            val collector = BookDeadlineCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            deadlineFlow.emit(deadline)
            val progressBefore = stateFlow.value.deadlineProgress!!.requiredPerDay

            // ----- Act -----
            // Simulate reading 150 pages → 150 remaining / 30 days = 5 p/day
            val bookAfter = stubBook(
                id = bookId,
                pages = totalPages,
                currentPage = 150,
            )
            stateFlow.value = stateFlow.value.copy(book = bookAfter)

            // ----- Assert -----
            val progressAfter = stateFlow.value.deadlineProgress!!.requiredPerDay
            (progressAfter < progressBefore) shouldBe true
            job.cancel()
        }

        @Test
        fun `state deadline and deadlineProgress are both null before any emission`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val book = stubBook(
                id = 1,
                pages = 300,
                currentPage = 0,
            )
            stateFlow.value = BookDetailUiState(book = book)

            val collector = BookDeadlineCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act & Assert -----
            stateFlow.value.deadline shouldBe null
            stateFlow.value.deadlineProgress shouldBe null
            job.cancel()
        }

        @Test
        fun `deadlineProgress is null when book is null`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = BookDetailUiState(book = null)

            val collector = BookDeadlineCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act & Assert -----
            stateFlow.value.deadlineProgress shouldBe null
            job.cancel()
        }
    }
}
