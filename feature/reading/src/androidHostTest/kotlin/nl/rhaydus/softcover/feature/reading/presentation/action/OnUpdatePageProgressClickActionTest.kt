package nl.rhaydus.softcover.feature.reading.presentation.action

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
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
import kotlinx.datetime.LocalDateTime
import nl.rhaydus.softcover.core.book.domain.usecase.RecordBookProgressUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.ShelfMutationOutcome
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.uibinding.progress.toActionAt
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnUpdatePageProgressClickActionTest {
    private lateinit var updateBookProgress: RecordBookProgressUseCase
    private lateinit var stateFlow: MutableStateFlow<ReadingScreenUiState>
    private lateinit var localVariablesFlow: MutableStateFlow<ReadingLocalVariables>
    private lateinit var scope: ActionScope<ReadingScreenUiState, ReadingScreenEvent, ReadingLocalVariables>

    @BeforeEach
    fun setUp() {
        updateBookProgress = mockk(relaxed = true)
        coEvery {
            updateBookProgress(
                book = any(),
                newPage = any(),
                newSeconds = any(),
            )
        } returns Result.success(null)
        stateFlow = MutableStateFlow(ReadingScreenUiState())
        localVariablesFlow = MutableStateFlow(ReadingLocalVariables())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = localVariablesFlow,
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    private fun stubDependencies(testScope: TestScope): ReadingScreenDependencies {
        val dispatcher = UnconfinedTestDispatcher(testScope.testScheduler)

        return mockk<ReadingScreenDependencies>(relaxed = true).also { mock ->
            every {
                mock.recordBookProgressUseCase
            } returns updateBookProgress

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

    private fun stubBook(id: Int = 99): Book = mockk<Book>().also { mock ->
        every {
            mock.id
        } returns id
        every {
            mock.userBook
        } returns null
    }

    @Nested
    inner class Execute {
        @Test
        fun `records the parsed page with the backdated actionAt and closes the sheet`() = runTest {
            // ----- Arrange -----
            val readAt = LocalDateTime(
                2026,
                7,
                21,
                21,
                0,
            )
            val book = stubBook()
            stateFlow.value = ReadingScreenUiState(
                bookToUpdate = book,
                showProgressSheet = true,
                readAt = readAt,
            )
            val dependencies = stubDependencies(this)

            coEvery {
                updateBookProgress(
                    book = book,
                    newPage = any(),
                    newSeconds = any(),
                    actionAt = readAt.toActionAt(),
                )
            } returns Result.success(null)

            val action = OnUpdatePageProgressClickAction(newPage = "150")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                updateBookProgress(
                    book = book,
                    newPage = 150,
                    newSeconds = null,
                    actionAt = readAt.toActionAt(),
                )
            }
            stateFlow.value.showProgressSheet shouldBe false
            stateFlow.value.bookToUpdate shouldBe null
        }

        @Test
        fun `uses page 0 when newPage is not a valid integer`() = runTest {
            // ----- Arrange -----
            val book = stubBook()
            stateFlow.value = ReadingScreenUiState(bookToUpdate = book)
            val dependencies = stubDependencies(this)
            val action = OnUpdatePageProgressClickAction(newPage = "not-a-number")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                updateBookProgress(
                    book = book,
                    newPage = 0,
                    newSeconds = null,
                )
            }
        }

        @Test
        fun `passes a negative page through unchanged`() = runTest {
            // ----- Arrange -----
            val book = stubBook()
            stateFlow.value = ReadingScreenUiState(bookToUpdate = book)
            val dependencies = stubDependencies(this)
            val action = OnUpdatePageProgressClickAction(newPage = "-5")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                updateBookProgress(
                    book = book,
                    newPage = -5,
                    newSeconds = null,
                )
            }
        }

        @Test
        fun `records nothing and leaves the state alone when there is no bookToUpdate`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(
                bookToUpdate = null,
                showProgressSheet = true,
            )
            val dependencies = stubDependencies(this)
            val action = OnUpdatePageProgressClickAction(newPage = "42")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 0) {
                updateBookProgress(
                    any(),
                    any(),
                    any(),
                )
            }
            stateFlow.value.showProgressSheet shouldBe true
        }

        @Test
        fun `flags the book as failed when updateBookProgress fails`() = runTest {
            // ----- Arrange -----
            val book = stubBook(id = 7)
            stateFlow.value = ReadingScreenUiState(bookToUpdate = book)
            val dependencies = stubDependencies(this)

            coEvery {
                updateBookProgress(
                    book = book,
                    newPage = any(),
                    newSeconds = any(),
                )
            } returns Result.failure(RuntimeException("network error"))

            val action = OnUpdatePageProgressClickAction(newPage = "50")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.failedMutationBookIds.contains(7) shouldBe true
        }

        @Test
        fun `flags no failure and no verdict prompt when the update succeeds without a finish`() = runTest {
            // ----- Arrange -----
            val book = stubBook(id = 7)
            stateFlow.value = ReadingScreenUiState(bookToUpdate = book)
            val dependencies = stubDependencies(this)
            val action = OnUpdatePageProgressClickAction(newPage = "50")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.failedMutationBookIds.contains(7) shouldBe false
            stateFlow.value.verdictPromptBook shouldBe null
        }

        @Test
        fun `prompts for a verdict when the outcome is Applied`() = runTest {
            // ----- Arrange -----
            val book = stubBook(id = 7)
            stateFlow.value = ReadingScreenUiState(bookToUpdate = book)
            val dependencies = stubDependencies(this)

            coEvery {
                updateBookProgress(
                    book = book,
                    newPage = any(),
                    newSeconds = any(),
                )
            } returns Result.success(ShelfMutationOutcome.Applied)

            val action = OnUpdatePageProgressClickAction(newPage = "50")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.verdictPromptBook shouldBe book
        }

        @Test
        fun `cancels the prior job for the same book id and stores a new one`() = runTest {
            // ----- Arrange -----
            val bookId = 7
            val book = stubBook(id = bookId)
            val priorJob = Job()

            localVariablesFlow.value = ReadingLocalVariables(
                bookMutationJobs = mapOf(bookId to priorJob),
            )
            stateFlow.value = ReadingScreenUiState(bookToUpdate = book)

            val dependencies = stubDependencies(this)
            val action = OnUpdatePageProgressClickAction(newPage = "50")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            priorJob.isCancelled shouldBe true
            localVariablesFlow.value.bookMutationJobs[bookId] shouldNotBe priorJob
        }
    }
}
