package nl.rhaydus.softcover.feature.widget.progress

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
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.book.domain.usecase.RecordBookProgressUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.ShelfMutationOutcome
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class RecordProgressTest {
    private val eventChannel = Channel<UpdateProgressEvent>(Channel.BUFFERED)
    private lateinit var recordBookProgress: RecordBookProgressUseCase
    private lateinit var localVariablesFlow: MutableStateFlow<UpdateProgressLocalVariables>
    private lateinit var scope: ActionScope<UpdateProgressUiState, UpdateProgressEvent, UpdateProgressLocalVariables>

    @BeforeEach
    fun setUp() {
        recordBookProgress = mockk(relaxed = true)
        localVariablesFlow = MutableStateFlow(UpdateProgressLocalVariables())
        scope = ActionScope(
            stateFlow = MutableStateFlow(UpdateProgressUiState()),
            localVariablesFlow = localVariablesFlow,
            eventChannel = eventChannel,
        )
    }

    private fun stubWrite(result: Result<ShelfMutationOutcome?>) {
        coEvery {
            recordBookProgress(
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
        fun `a new submit cancels the previous in-flight write`() = runTest {
            // ----- Arrange -----
            val gate = CompletableDeferred<Unit>()
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
            val dependencies = stubDependencies(this)
            every {
                dependencies.recordBookProgress
            } returns recordBookProgress
            val book = stubPagedBook(pages = 100)
            recordProgress(
                dependencies = dependencies,
                scope = scope,
                book = book,
                newPage = 10,
                newSeconds = null,
            )
            val firstJob = localVariablesFlow.value.mutationJob.shouldNotBeNull()

            // ----- Act -----
            recordProgress(
                dependencies = dependencies,
                scope = scope,
                book = book,
                newPage = 20,
                newSeconds = null,
            )

            // ----- Assert -----
            firstJob.isCancelled shouldBe true
            localVariablesFlow.value.mutationJob.shouldNotBeNull().isCancelled shouldBe false
            gate.complete(Unit)
        }

        @Test
        fun `a successful write refreshes the widget`() = runTest {
            // ----- Arrange -----
            stubWrite(Result.success(null))
            val dependencies = stubDependencies(this)
            every {
                dependencies.recordBookProgress
            } returns recordBookProgress

            // ----- Act -----
            recordProgress(
                dependencies = dependencies,
                scope = scope,
                book = stubPagedBook(pages = 100),
                newPage = 10,
                newSeconds = null,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                dependencies.refreshWidget()
            }
        }

        @Test
        fun `an applied write marks the book as finished and the dismiss opens its verdict`() = runTest {
            // ----- Arrange -----
            stubWrite(Result.success(ShelfMutationOutcome.Applied))
            val dependencies = stubDependencies(this)
            every {
                dependencies.recordBookProgress
            } returns recordBookProgress
            recordProgress(
                dependencies = dependencies,
                scope = scope,
                book = stubPagedBook(pages = 100),
                newPage = 100,
                newSeconds = null,
            )

            // ----- Act -----
            DismissProgressSheetAction.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            localVariablesFlow.value.finishedBookId shouldBe 99
            verify(exactly = 1) {
                dependencies.openVerdict(99)
            }
            verify(exactly = 0) {
                dependencies.showSaveFailed()
            }
            eventChannel.tryReceive().getOrNull() shouldBe UpdateProgressEvent.Finished
        }

        @Test
        fun `a write that changed nothing leaves the book unfinished and the dismiss plain`() = runTest {
            // ----- Arrange -----
            stubWrite(Result.success(ShelfMutationOutcome.NoChange))
            val dependencies = stubDependencies(this)
            every {
                dependencies.recordBookProgress
            } returns recordBookProgress
            recordProgress(
                dependencies = dependencies,
                scope = scope,
                book = stubPagedBook(pages = 100),
                newPage = 100,
                newSeconds = null,
            )

            // ----- Act -----
            DismissProgressSheetAction.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            localVariablesFlow.value.finishedBookId.shouldBeNull()
            verify(exactly = 0) {
                dependencies.openVerdict(any())
            }
            eventChannel.tryReceive().getOrNull() shouldBe UpdateProgressEvent.Finished
        }

        @Test
        fun `starting a new write clears the previous finished book`() = runTest {
            // ----- Arrange -----
            stubWrite(Result.success(ShelfMutationOutcome.Applied))
            val dependencies = stubDependencies(this)
            every {
                dependencies.recordBookProgress
            } returns recordBookProgress
            val book = stubPagedBook(pages = 100)
            recordProgress(
                dependencies = dependencies,
                scope = scope,
                book = book,
                newPage = 100,
                newSeconds = null,
            )
            val gate = CompletableDeferred<Unit>()
            coEvery {
                recordBookProgress(
                    book = any(),
                    newPage = any(),
                    newSeconds = any(),
                    actionAt = any(),
                )
            } coAnswers {
                gate.await()
                Result.success(ShelfMutationOutcome.NoChange)
            }

            // ----- Act -----
            recordProgress(
                dependencies = dependencies,
                scope = scope,
                book = book,
                newPage = 50,
                newSeconds = null,
            )

            // ----- Assert -----
            localVariablesFlow.value.finishedBookId.shouldBeNull()
            gate.complete(Unit)
        }

        @Test
        fun `a failed write skips the refresh and does not crash`() = runTest {
            // ----- Arrange -----
            stubWrite(Result.failure(IllegalStateException("boom")))
            val dependencies = stubDependencies(this)
            every {
                dependencies.recordBookProgress
            } returns recordBookProgress

            // ----- Act -----
            recordProgress(
                dependencies = dependencies,
                scope = scope,
                book = stubPagedBook(pages = 100),
                newPage = 10,
                newSeconds = null,
            )
            localVariablesFlow.value.mutationJob.shouldNotBeNull().join()

            // ----- Assert -----
            coVerify(exactly = 0) {
                dependencies.refreshWidget()
            }
        }

        @Test
        fun `a failed write reports the failure on dismiss, opens no verdict and still finishes`() = runTest {
            // ----- Arrange -----
            stubWrite(Result.failure(IllegalStateException("boom")))
            val dependencies = stubDependencies(this)
            every {
                dependencies.recordBookProgress
            } returns recordBookProgress
            recordProgress(
                dependencies = dependencies,
                scope = scope,
                book = stubPagedBook(pages = 100),
                newPage = 10,
                newSeconds = null,
            )

            // ----- Act -----
            DismissProgressSheetAction.execute(
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
        fun `starting a new write clears the previous failure`() = runTest {
            // ----- Arrange -----
            stubWrite(Result.failure(IllegalStateException("boom")))
            val dependencies = stubDependencies(this)
            every {
                dependencies.recordBookProgress
            } returns recordBookProgress
            val book = stubPagedBook(pages = 100)
            recordProgress(
                dependencies = dependencies,
                scope = scope,
                book = book,
                newPage = 10,
                newSeconds = null,
            )
            val gate = CompletableDeferred<Unit>()
            coEvery {
                recordBookProgress(
                    book = any(),
                    newPage = any(),
                    newSeconds = any(),
                    actionAt = any(),
                )
            } coAnswers {
                gate.await()
                Result.success(ShelfMutationOutcome.NoChange)
            }

            // ----- Act -----
            recordProgress(
                dependencies = dependencies,
                scope = scope,
                book = book,
                newPage = 20,
                newSeconds = null,
            )

            // ----- Assert -----
            localVariablesFlow.value.writeFailed shouldBe false
            gate.complete(Unit)
        }
    }
}
