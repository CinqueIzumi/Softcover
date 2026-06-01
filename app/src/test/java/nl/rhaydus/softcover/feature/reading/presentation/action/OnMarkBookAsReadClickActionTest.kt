package nl.rhaydus.softcover.feature.reading.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.book.domain.usecase.MarkBookAsReadUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.ShelfMutationOutcome
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnMarkBookAsReadClickActionTest {

    private lateinit var markBookAsReadUseCase: MarkBookAsReadUseCase
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
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    private fun stubDependencies(testScope: kotlinx.coroutines.test.TestScope): ReadingScreenDependencies {
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
        every { mock.id } returns id
    }

    @Nested
    inner class Execute {

        @Test
        fun `invokes markBookAsReadUseCase with the provided book`() = runTest {
            // ----- Arrange -----
            val book = stubBook()
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
            coVerify {
                markBookAsReadUseCase(book = book)
            }
        }

        @Test
        fun `does not add book id to failedMutationBookIds when use case succeeds`() = runTest {
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
            stateFlow.value.failedMutationBookIds.contains(42) shouldBe false
        }

        @Test
        fun `adds book id to failedMutationBookIds when use case fails`() = runTest {
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
        }

        @Test
        fun `stores job in bookMutationJobs after execute returns`() = runTest {
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
            localVariablesFlow.value.bookMutationJobs.containsKey(42) shouldBe true
        }

        @Test
        fun `cancels prior job for same book id and replaces it with a new one`() = runTest {
            // ----- Arrange -----
            val bookId = 42
            val book = stubBook(id = bookId)
            val priorJob = Job()

            localVariablesFlow.value = ReadingLocalVariables(
                bookMutationJobs = mapOf(bookId to priorJob),
            )

            scope = ActionScope(
                stateFlow = stateFlow,
                localVariablesFlow = localVariablesFlow,
                eventChannel = Channel(Channel.BUFFERED),
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

            localVariablesFlow.value.bookMutationJobs.containsKey(bookId) shouldBe true
        }

        @Test
        fun `completes without throwing when use case fails`() = runTest {
            // ----- Arrange -----
            val book = stubBook()
            val dependencies = stubDependencies(this)

            coEvery {
                markBookAsReadUseCase(book = book)
            } returns Result.failure(RuntimeException("network failure"))

            val action = OnMarkBookAsReadClickAction(book = book)

            // ----- Act & Assert -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )
        }
    }
}
