package nl.rhaydus.softcover.feature.reading.presentation.screenmodel

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
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
import nl.rhaydus.softcover.core.book.domain.usecase.RecordBookProgressUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.ShelfMutationOutcome
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.progresssheet.presentation.ProgressEntry
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ReadingProgressSheetHostTest {
    private lateinit var recordBookProgress: RecordBookProgressUseCase
    private val eventChannel = Channel<ReadingScreenEvent>(Channel.BUFFERED)
    private lateinit var stateFlow: MutableStateFlow<ReadingScreenUiState>
    private lateinit var localVariablesFlow: MutableStateFlow<ReadingLocalVariables>
    private lateinit var scope: ActionScope<ReadingScreenUiState, ReadingScreenEvent, ReadingLocalVariables>

    @BeforeEach
    fun setUp() {
        recordBookProgress = mockk()
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
                mock.launch(any())
            } answers { callOriginal() }
        }
    }

    private fun stubBook(id: Int = 7): Book = mockk<Book>().also { mock ->
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
    }
}
