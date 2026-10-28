package nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.book.domain.usecase.MarkBookAsReadUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.RecordBookProgressUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.ShelfMutationOutcome
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.progresssheet.presentation.ProgressEntry
import nl.rhaydus.softcover.feature.book_detail.presentation.buildBookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BookDetailProgressSheetHostTest {
    private lateinit var recordBookProgressUseCase: RecordBookProgressUseCase
    private lateinit var markBookAsReadUseCase: MarkBookAsReadUseCase
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var localVariablesFlow: MutableStateFlow<BookDetailLocalVariables>
    private lateinit var eventChannel: Channel<BookDetailEvent>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        recordBookProgressUseCase = mockk()
        markBookAsReadUseCase = mockk()
        stateFlow = MutableStateFlow(buildBookDetailUiState())
        localVariablesFlow = MutableStateFlow(BookDetailLocalVariables())
        eventChannel = Channel(Channel.BUFFERED)
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = localVariablesFlow,
            eventChannel = eventChannel,
        )
    }

    private fun stubDependencies(testScope: TestScope): BookDetailDependencies {
        val dispatcher = UnconfinedTestDispatcher(testScope.testScheduler)
        return mockk<BookDetailDependencies>(relaxed = true).also { mock ->
            every {
                mock.recordBookProgressUseCase
            } returns recordBookProgressUseCase
            every {
                mock.markBookAsReadUseCase
            } returns markBookAsReadUseCase
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

    private fun stubBook(id: Int = 42): Book = mockk<Book>().also { mock ->
        every {
            mock.id
        } returns id
        every {
            mock.userBook
        } returns null
    }

    private fun pageEntry(): ProgressEntry = ProgressEntry(
        newPage = 100,
        newSeconds = null,
        actionAt = null,
    )

    private fun stubRecord(result: Result<ShelfMutationOutcome?>) {
        coEvery {
            recordBookProgressUseCase(
                book = any(),
                newPage = any(),
                newSeconds = any(),
                actionAt = any(),
            )
        } returns result
    }

    @Nested
    inner class Record {
        @Test
        fun `passes the entry's page, seconds and actionAt to the use case`() = runTest {
            // ----- Arrange -----
            val book = stubBook()
            val dependencies = stubDependencies(this)
            stubRecord(Result.success(null))

            // ----- Act -----
            BookDetailProgressSheetHost.record(
                book = book,
                entry = ProgressEntry(
                    newPage = 120,
                    newSeconds = 3_600,
                    actionAt = "2020-01-02T09:15:00",
                ),
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                recordBookProgressUseCase(
                    book = book,
                    newPage = 120,
                    newSeconds = 3_600,
                    actionAt = "2020-01-02T09:15:00",
                )
            }
        }

        @Test
        fun `sends BookMarkedAsReadEvent when the use case returns Applied`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)
            stubRecord(Result.success(ShelfMutationOutcome.Applied))

            // ----- Act -----
            BookDetailProgressSheetHost.record(
                book = stubBook(),
                entry = pageEntry(),
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            eventChannel.tryReceive().getOrNull().shouldBeInstanceOf<BookDetailEvent.BookMarkedAsReadEvent>()
        }

        @Test
        fun `sends ProgressUpdatedEvent when the use case returns NoChange`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)
            stubRecord(Result.success(ShelfMutationOutcome.NoChange))

            // ----- Act -----
            BookDetailProgressSheetHost.record(
                book = stubBook(),
                entry = pageEntry(),
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            eventChannel.tryReceive().getOrNull().shouldBeInstanceOf<BookDetailEvent.ProgressUpdatedEvent>()
        }

        @Test
        fun `sends ProgressUpdatedEvent when the use case returns no outcome`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)
            stubRecord(Result.success(null))

            // ----- Act -----
            BookDetailProgressSheetHost.record(
                book = stubBook(),
                entry = pageEntry(),
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            eventChannel.tryReceive().getOrNull().shouldBeInstanceOf<BookDetailEvent.ProgressUpdatedEvent>()
        }

        @Test
        fun `adds the book id to failedMutationBookIds and sends no event when the use case fails`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)
            stubRecord(Result.failure(RuntimeException("api error")))

            // ----- Act -----
            BookDetailProgressSheetHost.record(
                book = stubBook(id = 42),
                entry = pageEntry(),
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.failedMutationBookIds.contains(42) shouldBe true
            eventChannel.tryReceive().getOrNull().shouldBeNull()
        }

        @Test
        fun `cancels the prior job for the same book and stores the new one`() = runTest {
            // ----- Arrange -----
            val priorJob = Job()
            localVariablesFlow.value = BookDetailLocalVariables(
                bookMutationJobs = mapOf(42 to priorJob),
            )
            val dependencies = stubDependencies(this)
            stubRecord(Result.success(null))

            // ----- Act -----
            BookDetailProgressSheetHost.record(
                book = stubBook(id = 42),
                entry = pageEntry(),
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            priorJob.isCancelled shouldBe true
            (localVariablesFlow.value.bookMutationJobs[42] === priorJob) shouldBe false
        }

        @Test
        fun `leaves another book's job running`() = runTest {
            // ----- Arrange -----
            val otherJob = Job()
            localVariablesFlow.value = BookDetailLocalVariables(
                bookMutationJobs = mapOf(7 to otherJob),
            )
            val dependencies = stubDependencies(this)
            stubRecord(Result.success(null))

            // ----- Act -----
            BookDetailProgressSheetHost.record(
                book = stubBook(id = 42),
                entry = pageEntry(),
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            otherJob.isCancelled shouldBe false
            localVariablesFlow.value.bookMutationJobs.keys shouldBe setOf(7, 42)
        }
    }

    @Nested
    inner class MarkAsRead {
        @Test
        fun `marks the book as read at the given actionAt`() = runTest {
            // ----- Arrange -----
            val book = stubBook()
            val dependencies = stubDependencies(this)
            coEvery {
                markBookAsReadUseCase(
                    book = any(),
                    editionId = any(),
                    actionAt = any(),
                )
            } returns Result.success(ShelfMutationOutcome.Applied)

            // ----- Act -----
            BookDetailProgressSheetHost.markAsRead(
                book = book,
                actionAt = "2020-01-02T09:15:00",
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                markBookAsReadUseCase(
                    book = book,
                    editionId = any(),
                    actionAt = "2020-01-02T09:15:00",
                )
            }
        }
    }
}
