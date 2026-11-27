package nl.rhaydus.softcover.feature.reading.presentation.screenmodel

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.book.domain.usecase.RecordBookProgressUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.ShelfMutationOutcome
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.preview.PreviewData
import nl.rhaydus.softcover.core.progresssheet.presentation.ProgressEntry
import nl.rhaydus.softcover.core.progresssheet.presentation.mood.MoodMomentSheetSlice
import nl.rhaydus.softcover.core.progresssheet.presentation.mood.PendingMoodMoment
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ReadingProgressSheetHostTest {
    private lateinit var recordBookProgress: RecordBookProgressUseCase
    private lateinit var moodMomentSheetSlice: MoodMomentSheetSlice
    private val eventChannel = Channel<ReadingScreenEvent>(Channel.BUFFERED)
    private lateinit var stateFlow: MutableStateFlow<ReadingScreenUiState>
    private lateinit var localVariablesFlow: MutableStateFlow<ReadingLocalVariables>
    private lateinit var scope: ActionScope<ReadingScreenUiState, ReadingScreenEvent, ReadingLocalVariables>

    @BeforeEach
    fun setUp() {
        recordBookProgress = mockk()
        moodMomentSheetSlice = mockk()

        coEvery {
            moodMomentSheetSlice.openAfterProgress<ReadingScreenUiState, ReadingScreenEvent, ReadingLocalVariables>(
                pending = any(),
                host = any(),
                scope = any(),
            )
        } just Runs

        stubOutcome(Result.success(null))
        stateFlow = MutableStateFlow(ReadingScreenUiState())
        localVariablesFlow = MutableStateFlow(ReadingLocalVariables())

        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = localVariablesFlow,
            eventChannel = eventChannel,
        )
    }

    private fun stubOutcome(result: Result<ShelfMutationOutcome?>) {
        coEvery {
            recordBookProgress(
                book = any(),
                newPage = any(),
                newSeconds = any(),
                actionAt = any(),
            )
        } returns result
    }

    private fun stubDependencies(testScope: TestScope): ReadingScreenDependencies {
        val dispatcher = UnconfinedTestDispatcher(testScope.testScheduler)

        return mockk<ReadingScreenDependencies>(relaxed = true).also { mock ->
            every {
                mock.recordBookProgressUseCase
            } returns recordBookProgress

            every {
                mock.coroutineScope
            } returns testScope

            every {
                mock.mainDispatcher
            } returns dispatcher

            every {
                mock.moodMomentSheetSlice
            } returns moodMomentSheetSlice

            every {
                mock.launch(any())
            } answers { callOriginal() }
        }
    }

    private fun stubBook(id: Int = 7): Book = mockk<Book>(relaxed = true).also { mock ->
        every {
            mock.id
        } returns id
    }

    private val entry = ProgressEntry(
        newPage = 50,
        newSeconds = null,
        actionAt = null,
    )

    @Nested
    inner class Record {
        @Test
        fun `records the entry's page, seconds and backdate for the book`() = runTest {
            // ----- Arrange -----
            val book = stubBook()

            val backdated = ProgressEntry(
                newPage = 150,
                newSeconds = 3600,
                actionAt = "2026-07-21T21:00:00",
            )

            val dependencies = stubDependencies(this)

            // ----- Act -----
            ReadingProgressSheetHost.record(
                book = book,
                entry = backdated,
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                recordBookProgress(
                    book = book,
                    newPage = 150,
                    newSeconds = 3600,
                    actionAt = "2026-07-21T21:00:00",
                )
            }
        }

        @Test
        fun `prompts for a verdict and sends no event when the outcome is Applied`() = runTest {
            // ----- Arrange -----
            val book = stubBook()
            stubOutcome(Result.success(ShelfMutationOutcome.Applied))
            val dependencies = stubDependencies(this)

            // ----- Act -----
            ReadingProgressSheetHost.record(
                book = book,
                entry = entry,
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.verdictPromptBook shouldBe book
            eventChannel.tryReceive().getOrNull().shouldBeNull()
        }

        @Test
        fun `sends ProgressUpdatedEvent and no verdict prompt when the outcome is NoChange`() = runTest {
            // ----- Arrange -----
            stubOutcome(Result.success(ShelfMutationOutcome.NoChange))
            val dependencies = stubDependencies(this)

            // ----- Act -----
            ReadingProgressSheetHost.record(
                book = stubBook(),
                entry = entry,
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            eventChannel.tryReceive().getOrNull().shouldBeInstanceOf<ReadingScreenEvent.ProgressUpdatedEvent>()
            stateFlow.value.verdictPromptBook.shouldBeNull()
            stateFlow.value.failedMutationBookIds.isEmpty() shouldBe true
        }

        @Test
        fun `sends ProgressUpdatedEvent when the use case returns no outcome`() = runTest {
            // ----- Arrange -----
            stubOutcome(Result.success(null))
            val dependencies = stubDependencies(this)

            // ----- Act -----
            ReadingProgressSheetHost.record(
                book = stubBook(),
                entry = entry,
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            eventChannel.tryReceive().getOrNull().shouldBeInstanceOf<ReadingScreenEvent.ProgressUpdatedEvent>()
            stateFlow.value.verdictPromptBook.shouldBeNull()
        }

        @Test
        fun `flags the book as failed and sends no event when recording fails`() = runTest {
            // ----- Arrange -----
            stubOutcome(Result.failure(RuntimeException("network error")))
            val dependencies = stubDependencies(this)

            // ----- Act -----
            ReadingProgressSheetHost.record(
                book = stubBook(id = 7),
                entry = entry,
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.failedMutationBookIds.contains(7) shouldBe true
            stateFlow.value.verdictPromptBook.shouldBeNull()
            eventChannel.tryReceive().getOrNull().shouldBeNull()
        }

        @Test
        fun `cancels the prior job for the same book id and stores a new one`() = runTest {
            // ----- Arrange -----
            val priorJob = Job()

            localVariablesFlow.value = ReadingLocalVariables(
                bookMutationJobs = mapOf(7 to priorJob),
            )

            val dependencies = stubDependencies(this)

            // ----- Act -----
            ReadingProgressSheetHost.record(
                book = stubBook(id = 7),
                entry = entry,
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            priorJob.isCancelled shouldBe true
            localVariablesFlow.value.bookMutationJobs[7].shouldNotBeNull() shouldNotBe priorJob
        }

        @Test
        fun `leaves the job of a different book running`() = runTest {
            // ----- Arrange -----
            val otherJob = Job()

            localVariablesFlow.value = ReadingLocalVariables(
                bookMutationJobs = mapOf(8 to otherJob),
            )

            val dependencies = stubDependencies(this)

            // ----- Act -----
            ReadingProgressSheetHost.record(
                book = stubBook(id = 7),
                entry = entry,
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            otherJob.isCancelled shouldBe false
            localVariablesFlow.value.bookMutationJobs[8] shouldBe otherJob
        }

        @Test
        fun `stores a pending mood moment for the book when the use case returns no outcome`() = runTest {
            // ----- Arrange -----
            val book = PreviewData.baseBook
            val dependencies = stubDependencies(this)

            // ----- Act -----
            ReadingProgressSheetHost.record(
                book = book,
                entry = entry,
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            localVariablesFlow.value.pendingMoodMoment shouldBe PendingMoodMoment(
                bookId = book.id,
                page = 50,
                seconds = null,
                percent = 50,
            )
        }

        @Test
        fun `stores no pending mood moment when the outcome is Applied`() = runTest {
            // ----- Arrange -----
            stubOutcome(Result.success(ShelfMutationOutcome.Applied))
            val dependencies = stubDependencies(this)

            // ----- Act -----
            ReadingProgressSheetHost.record(
                book = PreviewData.baseBook,
                entry = entry,
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            localVariablesFlow.value.pendingMoodMoment.shouldBeNull()
        }

        @Test
        fun `stores no pending mood moment when the outcome is NoChange`() = runTest {
            // ----- Arrange -----
            stubOutcome(Result.success(ShelfMutationOutcome.NoChange))
            val dependencies = stubDependencies(this)

            // ----- Act -----
            ReadingProgressSheetHost.record(
                book = PreviewData.baseBook,
                entry = entry,
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            localVariablesFlow.value.pendingMoodMoment.shouldBeNull()
        }

        @Test
        fun `stores no pending mood moment when recording fails`() = runTest {
            // ----- Arrange -----
            stubOutcome(Result.failure(RuntimeException("network error")))
            val dependencies = stubDependencies(this)

            // ----- Act -----
            ReadingProgressSheetHost.record(
                book = PreviewData.baseBook,
                entry = entry,
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            localVariablesFlow.value.pendingMoodMoment.shouldBeNull()
        }

        @Test
        fun `clears a previous pending mood moment when the new record yields none`() = runTest {
            // ----- Arrange -----
            stubOutcome(Result.success(ShelfMutationOutcome.NoChange))

            localVariablesFlow.value = ReadingLocalVariables(
                pendingMoodMoment = PendingMoodMoment(
                    bookId = 1,
                    page = 10,
                    seconds = null,
                    percent = 10,
                ),
            )

            val dependencies = stubDependencies(this)

            // ----- Act -----
            ReadingProgressSheetHost.record(
                book = PreviewData.baseBook,
                entry = entry,
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            localVariablesFlow.value.pendingMoodMoment.shouldBeNull()
        }
    }

    @Nested
    inner class AwaitPendingWrite {
        @Test
        fun `suspends until the job of the book being updated completes`() = runTest {
            // ----- Arrange -----
            val job = Job()
            stateFlow.value = ReadingScreenUiState(bookToUpdate = stubBook(id = 7))
            localVariablesFlow.value = ReadingLocalVariables(bookMutationJobs = mapOf(7 to job))
            val dependencies = stubDependencies(this)
            var returned = false

            val waiter = launch {
                ReadingProgressSheetHost.awaitPendingWrite(
                    dependencies = dependencies,
                    scope = scope,
                )

                returned = true
            }

            // ----- Act -----
            runCurrent()
            val returnedWhileRunning = returned
            job.complete()
            runCurrent()

            // ----- Assert -----
            returnedWhileRunning shouldBe false
            returned shouldBe true
            waiter.isCompleted shouldBe true
        }

        @Test
        fun `returns immediately when the book has no job`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(bookToUpdate = stubBook(id = 7))
            localVariablesFlow.value = ReadingLocalVariables(bookMutationJobs = mapOf(8 to Job()))
            val dependencies = stubDependencies(this)

            // ----- Act -----
            ReadingProgressSheetHost.awaitPendingWrite(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.bookToUpdate.shouldNotBeNull()
        }
    }

    @Nested
    inner class MarkAsRead {
        @Test
        fun `sends MarkAsReadRequestedEvent carrying the book and backdate and records nothing`() = runTest {
            // ----- Arrange -----
            val book = stubBook()
            val dependencies = stubDependencies(this)

            // ----- Act -----
            ReadingProgressSheetHost.markAsRead(
                book = book,
                actionAt = "2026-07-21T21:00:00",
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            val sent = eventChannel.tryReceive().getOrNull()
                .shouldBeInstanceOf<ReadingScreenEvent.MarkAsReadRequestedEvent>()

            sent.book shouldBe book
            sent.actionAt shouldBe "2026-07-21T21:00:00"

            coVerify(exactly = 0) {
                recordBookProgress(
                    book = any(),
                    newPage = any(),
                    newSeconds = any(),
                    actionAt = any(),
                )
            }
        }

        @Test
        fun `carries a null backdate for just now`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)

            // ----- Act -----
            ReadingProgressSheetHost.markAsRead(
                book = stubBook(),
                actionAt = null,
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            val sent = eventChannel.tryReceive().getOrNull()
                .shouldBeInstanceOf<ReadingScreenEvent.MarkAsReadRequestedEvent>()

            sent.actionAt.shouldBeNull()
        }
    }

    @Nested
    inner class AfterClose {
        @Test
        fun `clears bookToUpdate and leaves the rest of the state alone`() = runTest {
            // ----- Arrange -----
            val prompted = stubBook(id = 3)

            stateFlow.value = ReadingScreenUiState(
                bookToUpdate = stubBook(),
                verdictPromptBook = prompted,
            )

            val dependencies = stubDependencies(this)

            // ----- Act -----
            ReadingProgressSheetHost.afterClose(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.bookToUpdate.shouldBeNull()
            stateFlow.value.verdictPromptBook shouldBe prompted
        }

        @Test
        fun `opens the mood moment sheet for the pending moment and clears it`() = runTest {
            // ----- Arrange -----
            val pending = PendingMoodMoment(
                bookId = 7,
                page = 50,
                seconds = null,
                percent = 50,
            )

            localVariablesFlow.value = ReadingLocalVariables(pendingMoodMoment = pending)
            val dependencies = stubDependencies(this)

            // ----- Act -----
            ReadingProgressSheetHost.afterClose(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            localVariablesFlow.value.pendingMoodMoment.shouldBeNull()

            coVerify(exactly = 1) {
                moodMomentSheetSlice.openAfterProgress<ReadingScreenUiState, ReadingScreenEvent, ReadingLocalVariables>(
                    pending = pending,
                    host = ReadingMoodMomentSheetHost,
                    scope = scope,
                )
            }
        }

        @Test
        fun `does not open the mood moment sheet without a pending moment`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)

            // ----- Act -----
            ReadingProgressSheetHost.afterClose(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 0) {
                moodMomentSheetSlice.openAfterProgress<ReadingScreenUiState, ReadingScreenEvent, ReadingLocalVariables>(
                    pending = any(),
                    host = any(),
                    scope = any(),
                )
            }
        }
    }
}
