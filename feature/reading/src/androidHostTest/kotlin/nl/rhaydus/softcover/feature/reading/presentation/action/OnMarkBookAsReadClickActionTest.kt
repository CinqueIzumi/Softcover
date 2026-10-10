package nl.rhaydus.softcover.feature.reading.presentation.action

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
import nl.rhaydus.softcover.core.book.domain.usecase.MarkBookAsReadUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.ShelfMutationOutcome
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnMarkBookAsReadClickActionTest {
    private lateinit var markBookAsReadUseCase: MarkBookAsReadUseCase
    private val eventChannel = Channel<ReadingScreenEvent>(Channel.BUFFERED)
    private lateinit var stateFlow: MutableStateFlow<ReadingScreenUiState>
    private lateinit var localVariablesFlow: MutableStateFlow<ReadingLocalVariables>
    private lateinit var scope: ActionScope<ReadingScreenUiState, ReadingScreenEvent, ReadingLocalVariables>

    @BeforeEach
    fun setUp() {
        markBookAsReadUseCase = mockk()
        stateFlow = MutableStateFlow(ReadingScreenUiState())
        localVariablesFlow = MutableStateFlow(ReadingLocalVariables())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = localVariablesFlow,
            eventChannel = eventChannel,
        )
    }

    private fun stubDependencies(testScope: TestScope): ReadingScreenDependencies {
        val dispatcher = UnconfinedTestDispatcher(testScope.testScheduler)

        return mockk<ReadingScreenDependencies>(relaxed = true).also { mock ->
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

    @Nested
    inner class Execute {
        @Test
        fun `sends the book and backdated actionAt to markBookAsReadUseCase`() = runTest {
            // ----- Arrange -----
            val book = stubBook()
            val dependencies = stubDependencies(this)
            val actionAt = "2026-07-21T21:00:00Z"

            coEvery {
                markBookAsReadUseCase(
                    book = book,
                    actionAt = actionAt,
                )
            } returns Result.success(ShelfMutationOutcome.Applied)

            val action = OnMarkBookAsReadClickAction(
                book = book,
                actionAt = actionAt,
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                markBookAsReadUseCase(
                    book = book,
                    actionAt = actionAt,
                )
            }
        }

        @Test
        fun `prompts for a verdict and flags no failure when the change was applied`() = runTest {
            // ----- Arrange -----
            val book = stubBook(id = 42)
            val dependencies = stubDependencies(this)

            coEvery {
                markBookAsReadUseCase(book = book)
            } returns Result.success(ShelfMutationOutcome.Applied)

            val action = OnMarkBookAsReadClickAction(book = book)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.verdictPromptBook shouldBe book
            stateFlow.value.failedMutationBookIds.contains(42) shouldBe false
        }

        @Test
        fun `leaves verdictPromptBook null when the use case reports no change`() = runTest {
            // ----- Arrange -----
            val book = stubBook(id = 42)
            val dependencies = stubDependencies(this)

            coEvery {
                markBookAsReadUseCase(book = book)
            } returns Result.success(ShelfMutationOutcome.NoChange)

            val action = OnMarkBookAsReadClickAction(book = book)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.verdictPromptBook shouldBe null
        }

        @Test
        fun `flags the book as failed and skips the verdict prompt when the use case fails`() = runTest {
            // ----- Arrange -----
            val book = stubBook(id = 42)
            val dependencies = stubDependencies(this)

            coEvery {
                markBookAsReadUseCase(book = book)
            } returns Result.failure(RuntimeException("api error"))

            val action = OnMarkBookAsReadClickAction(book = book)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.failedMutationBookIds.contains(42) shouldBe true
            stateFlow.value.verdictPromptBook shouldBe null
        }

        @Test
        fun `sends MarkAsReadFailedEvent when the use case fails`() = runTest {
            // ----- Arrange -----
            val book = stubBook(id = 42)
            val dependencies = stubDependencies(this)

            coEvery {
                markBookAsReadUseCase(book = book)
            } returns Result.failure(RuntimeException("api error"))

            val action = OnMarkBookAsReadClickAction(book = book)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            eventChannel.tryReceive().getOrNull().shouldBeInstanceOf<ReadingScreenEvent.MarkAsReadFailedEvent>()
        }

        @Test
        fun `sends no event when the use case succeeds`() = runTest {
            // ----- Arrange -----
            val book = stubBook(id = 42)
            val dependencies = stubDependencies(this)

            coEvery {
                markBookAsReadUseCase(book = book)
            } returns Result.success(ShelfMutationOutcome.Applied)

            val action = OnMarkBookAsReadClickAction(book = book)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            eventChannel.tryReceive().getOrNull() shouldBe null
        }

        @Test
        fun `cancels the prior job for the same book id and stores a new one`() = runTest {
            // ----- Arrange -----
            val bookId = 42
            val book = stubBook(id = bookId)
            val priorJob = Job()

            localVariablesFlow.value = ReadingLocalVariables(
                bookMutationJobs = mapOf(bookId to priorJob),
            )

            val dependencies = stubDependencies(this)

            coEvery {
                markBookAsReadUseCase(book = book)
            } returns Result.success(ShelfMutationOutcome.Applied)

            val action = OnMarkBookAsReadClickAction(book = book)

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
