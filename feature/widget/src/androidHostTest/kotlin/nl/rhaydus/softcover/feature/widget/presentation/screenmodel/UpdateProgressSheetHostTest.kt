package nl.rhaydus.softcover.feature.widget.presentation.screenmodel

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.book.domain.usecase.MarkBookAsReadUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.RecordBookProgressUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.ShelfMutationOutcome
import nl.rhaydus.softcover.core.progresssheet.presentation.ProgressEntry
import nl.rhaydus.softcover.feature.widget.presentation.action.stubDependencies
import nl.rhaydus.softcover.feature.widget.presentation.action.stubPagedBook
import nl.rhaydus.softcover.feature.widget.presentation.event.UpdateProgressEvent
import nl.rhaydus.softcover.feature.widget.presentation.state.UpdateProgressLocalVariables
import nl.rhaydus.softcover.feature.widget.presentation.state.UpdateProgressUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class UpdateProgressSheetHostTest {
    private val eventChannel = Channel<UpdateProgressEvent>(Channel.BUFFERED)
    private lateinit var recordBookProgress: RecordBookProgressUseCase
    private lateinit var markBookAsRead: MarkBookAsReadUseCase
    private lateinit var localVariablesFlow: MutableStateFlow<UpdateProgressLocalVariables>
    private lateinit var scope: ActionScope<UpdateProgressUiState, UpdateProgressEvent, UpdateProgressLocalVariables>

    private val entry = ProgressEntry(
        newPage = 10,
        newSeconds = null,
        actionAt = "2020-01-02",
    )

    @BeforeEach
    fun setUp() {
        recordBookProgress = mockk(relaxed = true)
        markBookAsRead = mockk(relaxed = true)
        localVariablesFlow = MutableStateFlow(UpdateProgressLocalVariables())
        scope = ActionScope(
            stateFlow = MutableStateFlow(UpdateProgressUiState()),
            localVariablesFlow = localVariablesFlow,
            eventChannel = eventChannel,
        )
    }

    private fun stubRecord(result: Result<ShelfMutationOutcome?>) {
        coEvery {
            recordBookProgress(
                book = any(),
                newPage = any(),
                newSeconds = any(),
                actionAt = any(),
            )
        } returns result
    }

    private fun stubRecordGated(gate: CompletableDeferred<Unit>) {
        coEvery {
            recordBookProgress(
                book = any(),
                newPage = any(),
                newSeconds = any(),
                actionAt = any(),
            )
        } coAnswers {
            gate.await()
            Result.success(null)
        }
    }

    private fun dependenciesFor(testScope: TestScope): UpdateProgressDependencies =
        stubDependencies(testScope).also { dependencies ->
            every {
                dependencies.recordBookProgress
            } returns recordBookProgress
            every {
                dependencies.markBookAsRead
            } returns markBookAsRead
        }

    @Nested
    inner class Record {
        @Test
        fun `passes the entry fields to the record use case`() = runTest {
            // ----- Arrange -----
            stubRecord(Result.success(null))
            val dependencies = dependenciesFor(this)
            val book = stubPagedBook(pages = 100)

            // ----- Act -----
            UpdateProgressSheetHost.record(
                book = book,
                entry = ProgressEntry(
                    newPage = 42,
                    newSeconds = 90,
                    actionAt = "2020-01-02",
                ),
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                recordBookProgress(
                    book = book,
                    newPage = 42,
                    newSeconds = 90,
                    actionAt = "2020-01-02",
                )
            }
        }

        @Test
        fun `a new write cancels the previous in-flight write`() = runTest {
            // ----- Arrange -----
            val gate = CompletableDeferred<Unit>()
            stubRecordGated(gate)
            val dependencies = dependenciesFor(this)
            val book = stubPagedBook(pages = 100)
            UpdateProgressSheetHost.record(
                book = book,
                entry = entry,
                dependencies = dependencies,
                scope = scope,
            )
            val firstJob = localVariablesFlow.value.mutationJob.shouldNotBeNull()

            // ----- Act -----
            UpdateProgressSheetHost.record(
                book = book,
                entry = entry,
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            firstJob.isCancelled shouldBe true
            localVariablesFlow.value.mutationJob.shouldNotBeNull().isCancelled shouldBe false
            gate.complete(Unit)
        }

        @Test
        fun `an applied write marks the book as finished`() = runTest {
            // ----- Arrange -----
            stubRecord(Result.success(ShelfMutationOutcome.Applied))
            val dependencies = dependenciesFor(this)

            // ----- Act -----
            UpdateProgressSheetHost.record(
                book = stubPagedBook(
                    pages = 100,
                    id = 99,
                ),
                entry = entry,
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            localVariablesFlow.value.finishedBookId shouldBe 99
            localVariablesFlow.value.writeFailed shouldBe false
        }

        @Test
        fun `a write that changed nothing leaves the book unfinished`() = runTest {
            // ----- Arrange -----
            stubRecord(Result.success(ShelfMutationOutcome.NoChange))
            val dependencies = dependenciesFor(this)

            // ----- Act -----
            UpdateProgressSheetHost.record(
                book = stubPagedBook(pages = 100),
                entry = entry,
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            localVariablesFlow.value.finishedBookId.shouldBeNull()
            localVariablesFlow.value.writeFailed shouldBe false
        }

        @Test
        fun `a write with no outcome leaves the book unfinished`() = runTest {
            // ----- Arrange -----
            stubRecord(Result.success(null))
            val dependencies = dependenciesFor(this)

            // ----- Act -----
            UpdateProgressSheetHost.record(
                book = stubPagedBook(pages = 100),
                entry = entry,
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            localVariablesFlow.value.finishedBookId.shouldBeNull()
        }

        @Test
        fun `a failed write flags the failure and leaves the book unfinished`() = runTest {
            // ----- Arrange -----
            stubRecord(Result.failure(IllegalStateException("boom")))
            val dependencies = dependenciesFor(this)

            // ----- Act -----
            UpdateProgressSheetHost.record(
                book = stubPagedBook(pages = 100),
                entry = entry,
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            localVariablesFlow.value.writeFailed shouldBe true
            localVariablesFlow.value.finishedBookId.shouldBeNull()
        }

        @Test
        fun `starting a new write clears the previous finished book`() = runTest {
            // ----- Arrange -----
            localVariablesFlow.value = UpdateProgressLocalVariables(finishedBookId = 99)
            val gate = CompletableDeferred<Unit>()
            stubRecordGated(gate)
            val dependencies = dependenciesFor(this)

            // ----- Act -----
            UpdateProgressSheetHost.record(
                book = stubPagedBook(pages = 100),
                entry = entry,
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            localVariablesFlow.value.finishedBookId.shouldBeNull()
            gate.complete(Unit)
        }

        @Test
        fun `starting a new write clears the previous failure`() = runTest {
            // ----- Arrange -----
            localVariablesFlow.value = UpdateProgressLocalVariables(writeFailed = true)
            val gate = CompletableDeferred<Unit>()
            stubRecordGated(gate)
            val dependencies = dependenciesFor(this)

            // ----- Act -----
            UpdateProgressSheetHost.record(
                book = stubPagedBook(pages = 100),
                entry = entry,
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            localVariablesFlow.value.writeFailed shouldBe false
            gate.complete(Unit)
        }
    }

    @Nested
    inner class MarkAsRead {
        @Test
        fun `passes the action time to the mark as read use case`() = runTest {
            // ----- Arrange -----
            coEvery {
                markBookAsRead(
                    book = any(),
                    editionId = any(),
                    actionAt = any(),
                )
            } returns Result.success(ShelfMutationOutcome.Applied)
            val dependencies = dependenciesFor(this)
            val book = stubPagedBook(pages = 100)

            // ----- Act -----
            UpdateProgressSheetHost.markAsRead(
                book = book,
                actionAt = "2020-01-02",
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                markBookAsRead(
                    book = book,
                    editionId = null,
                    actionAt = "2020-01-02",
                )
            }
        }

        @Test
        fun `an applied write marks the book as finished`() = runTest {
            // ----- Arrange -----
            coEvery {
                markBookAsRead(
                    book = any(),
                    editionId = any(),
                    actionAt = any(),
                )
            } returns Result.success(ShelfMutationOutcome.Applied)
            val dependencies = dependenciesFor(this)

            // ----- Act -----
            UpdateProgressSheetHost.markAsRead(
                book = stubPagedBook(
                    pages = 100,
                    id = 99,
                ),
                actionAt = null,
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            localVariablesFlow.value.finishedBookId shouldBe 99
        }

        @Test
        fun `a write that changed nothing leaves the book unfinished`() = runTest {
            // ----- Arrange -----
            coEvery {
                markBookAsRead(
                    book = any(),
                    editionId = any(),
                    actionAt = any(),
                )
            } returns Result.success(ShelfMutationOutcome.NoChange)
            val dependencies = dependenciesFor(this)

            // ----- Act -----
            UpdateProgressSheetHost.markAsRead(
                book = stubPagedBook(pages = 100),
                actionAt = null,
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            localVariablesFlow.value.finishedBookId.shouldBeNull()
        }

        @Test
        fun `a failed write flags the failure and leaves the book unfinished`() = runTest {
            // ----- Arrange -----
            coEvery {
                markBookAsRead(
                    book = any(),
                    editionId = any(),
                    actionAt = any(),
                )
            } returns Result.failure(IllegalStateException("boom"))
            val dependencies = dependenciesFor(this)

            // ----- Act -----
            UpdateProgressSheetHost.markAsRead(
                book = stubPagedBook(pages = 100),
                actionAt = null,
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            localVariablesFlow.value.writeFailed shouldBe true
            localVariablesFlow.value.finishedBookId.shouldBeNull()
        }

        @Test
        fun `a new write cancels the previous in-flight write`() = runTest {
            // ----- Arrange -----
            val gate = CompletableDeferred<Unit>()
            coEvery {
                markBookAsRead(
                    book = any(),
                    editionId = any(),
                    actionAt = any(),
                )
            } coAnswers {
                gate.await()
                Result.success(ShelfMutationOutcome.NoChange)
            }
            val dependencies = dependenciesFor(this)
            val book = stubPagedBook(pages = 100)
            UpdateProgressSheetHost.markAsRead(
                book = book,
                actionAt = null,
                dependencies = dependencies,
                scope = scope,
            )
            val firstJob = localVariablesFlow.value.mutationJob.shouldNotBeNull()

            // ----- Act -----
            UpdateProgressSheetHost.markAsRead(
                book = book,
                actionAt = null,
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            firstJob.isCancelled shouldBe true
            gate.complete(Unit)
        }
    }

    @Nested
    inner class AwaitPendingWrite {
        @Test
        fun `returns immediately when no write is in flight`() = runTest {
            // ----- Arrange -----
            val dependencies = dependenciesFor(this)

            // ----- Act -----
            UpdateProgressSheetHost.awaitPendingWrite(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            localVariablesFlow.value.mutationJob.shouldBeNull()
        }

        @Test
        fun `suspends until the in-flight write completes`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val gate = CompletableDeferred<Unit>()
            stubRecordGated(gate)
            val dependencies = dependenciesFor(this)
            UpdateProgressSheetHost.record(
                book = stubPagedBook(pages = 100),
                entry = entry,
                dependencies = dependencies,
                scope = scope,
            )
            var returned = false
            val waiter = launch {
                UpdateProgressSheetHost.awaitPendingWrite(
                    dependencies = dependencies,
                    scope = scope,
                )
                returned = true
            }
            runCurrent()
            val returnedWhileInFlight = returned

            // ----- Act -----
            gate.complete(Unit)
            runCurrent()

            // ----- Assert -----
            returnedWhileInFlight shouldBe false
            returned shouldBe true
            waiter.cancel()
        }
    }

    @Nested
    inner class AfterClose {
        @Test
        fun `opens the verdict for the finished book and sends Finished`() = runTest {
            // ----- Arrange -----
            localVariablesFlow.value = UpdateProgressLocalVariables(finishedBookId = 99)
            val dependencies = dependenciesFor(this)

            // ----- Act -----
            UpdateProgressSheetHost.afterClose(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            verify(exactly = 1) {
                dependencies.openVerdict(99)
            }
            verify(exactly = 0) {
                dependencies.showSaveFailed()
            }
            eventChannel.tryReceive().getOrNull() shouldBe UpdateProgressEvent.Finished
        }

        @Test
        fun `reports a failed write instead of opening a verdict and still sends Finished`() = runTest {
            // ----- Arrange -----
            localVariablesFlow.value = UpdateProgressLocalVariables(
                finishedBookId = 99,
                writeFailed = true,
            )
            val dependencies = dependenciesFor(this)

            // ----- Act -----
            UpdateProgressSheetHost.afterClose(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            verify(exactly = 1) {
                dependencies.showSaveFailed()
            }
            verify(exactly = 0) {
                dependencies.openVerdict(any())
            }
            eventChannel.tryReceive().getOrNull() shouldBe UpdateProgressEvent.Finished
        }

        @Test
        fun `does neither when nothing finished and no write failed`() = runTest {
            // ----- Arrange -----
            val dependencies = dependenciesFor(this)

            // ----- Act -----
            UpdateProgressSheetHost.afterClose(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            verify(exactly = 0) {
                dependencies.openVerdict(any())
            }
            verify(exactly = 0) {
                dependencies.showSaveFailed()
            }
            eventChannel.tryReceive().getOrNull() shouldBe UpdateProgressEvent.Finished
        }
    }
}
