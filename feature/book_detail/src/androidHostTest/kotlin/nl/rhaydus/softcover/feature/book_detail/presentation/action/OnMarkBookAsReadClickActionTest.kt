package nl.rhaydus.softcover.feature.book_detail.presentation.action

import io.kotest.matchers.shouldBe
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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.book.domain.usecase.MarkBookAsReadUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.ShelfMutationOutcome
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

class OnMarkBookAsReadClickActionTest {
    private lateinit var markBookAsReadUseCase: MarkBookAsReadUseCase
    private lateinit var dependencies: BookDetailDependencies
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var localVariablesFlow: MutableStateFlow<BookDetailLocalVariables>
    private lateinit var eventChannel: Channel<BookDetailEvent>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        markBookAsReadUseCase = mockk()
        stateFlow = MutableStateFlow(BookDetailUiState())
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
    }

    private fun stubEdition(id: Int = 55): BookEdition = mockk<BookEdition>().also { mock ->
        every {
            mock.id
        } returns id
    }

    @Nested
    inner class Execute {
        @Test
        fun `invokes use case with the book provided to the action constructor`() = runTest {
            // ----- Arrange -----
            val book = stubBook()
            dependencies = stubDependencies(this)

            coEvery {
                markBookAsReadUseCase(
                    book = book,
                    editionId = any(),
                )
            } returns Result.success(ShelfMutationOutcome.Applied)

            val action = OnMarkBookAsReadClickAction(book = book)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                markBookAsReadUseCase(
                    book = book,
                    editionId = any(),
                )
            }
        }

        @Test
        fun `does not add book id to failedMutationBookIds when use case succeeds`() = runTest {
            // ----- Arrange -----
            val book = stubBook(id = 42)
            dependencies = stubDependencies(this)

            coEvery {
                markBookAsReadUseCase(
                    book = book,
                    editionId = any(),
                )
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
            dependencies = stubDependencies(this)

            coEvery {
                markBookAsReadUseCase(
                    book = book,
                    editionId = any(),
                )
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
            dependencies = stubDependencies(this)

            coEvery {
                markBookAsReadUseCase(
                    book = book,
                    editionId = any(),
                )
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
            localVariablesFlow.value = BookDetailLocalVariables(
                bookMutationJobs = mapOf(bookId to priorJob),
            )

            scope = ActionScope(
                stateFlow = stateFlow,
                localVariablesFlow = localVariablesFlow,
                eventChannel = Channel(Channel.BUFFERED),
            )

            dependencies = stubDependencies(this)

            coEvery {
                markBookAsReadUseCase(
                    book = book,
                    editionId = any(),
                )
            } returns Result.success(ShelfMutationOutcome.Applied)

            val action = OnMarkBookAsReadClickAction(book = book)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            priorJob.isCancelled shouldBe true
            localVariablesFlow.value.bookMutationJobs[bookId] shouldBe localVariablesFlow.value.bookMutationJobs[bookId]
            localVariablesFlow.value.bookMutationJobs.containsKey(bookId) shouldBe true
        }

        @Test
        fun `completes without throwing when use case fails`() = runTest {
            // ----- Arrange -----
            val book = stubBook()
            dependencies = stubDependencies(this)

            coEvery {
                markBookAsReadUseCase(
                    book = book,
                    editionId = any(),
                )
            } returns Result.failure(RuntimeException("network failure"))

            val action = OnMarkBookAsReadClickAction(book = book)

            // ----- Act & Assert -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )
        }

        @Test
        fun `does not send BookMarkedAsReadEvent when use case returns NoChange`() = runTest {
            // ----- Arrange -----
            val book = stubBook()
            dependencies = stubDependencies(this)

            coEvery {
                markBookAsReadUseCase(
                    book = book,
                    editionId = any(),
                )
            } returns Result.success(ShelfMutationOutcome.NoChange)

            val action = OnMarkBookAsReadClickAction(book = book)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            val event = eventChannel.tryReceive().getOrNull()
            event shouldBe null
        }

        @Test
        fun `forwards previewEdition id as editionId when preview is set`() = runTest {
            // ----- Arrange -----
            val book = stubBook(id = 42)
            val preview = stubEdition(id = 55)
            stateFlow.value = BookDetailUiState(previewEdition = preview)
            dependencies = stubDependencies(this)

            coEvery {
                markBookAsReadUseCase(
                    book = book,
                    editionId = 55,
                )
            } returns Result.success(ShelfMutationOutcome.Applied)

            val action = OnMarkBookAsReadClickAction(book = book)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                markBookAsReadUseCase(
                    book = book,
                    editionId = 55,
                )
            }
        }

        @Test
        fun `uses scannedEditionId as editionId when previewEdition is null`() = runTest {
            // ----- Arrange -----
            val book = stubBook(id = 42)
            stateFlow.value = BookDetailUiState(
                previewEdition = null,
                scannedEditionId = 555,
            )
            dependencies = stubDependencies(this)

            coEvery {
                markBookAsReadUseCase(
                    book = book,
                    editionId = 555,
                )
            } returns Result.success(ShelfMutationOutcome.Applied)

            val action = OnMarkBookAsReadClickAction(book = book)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                markBookAsReadUseCase(
                    book = book,
                    editionId = 555,
                )
            }
        }

        @Test
        fun `forwards null as editionId when no preview is set`() = runTest {
            // ----- Arrange -----
            val book = stubBook(id = 42)
            stateFlow.value = BookDetailUiState(previewEdition = null)
            dependencies = stubDependencies(this)

            coEvery {
                markBookAsReadUseCase(
                    book = book,
                    editionId = null,
                )
            } returns Result.success(ShelfMutationOutcome.Applied)

            val action = OnMarkBookAsReadClickAction(book = book)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                markBookAsReadUseCase(
                    book = book,
                    editionId = null,
                )
            }
        }

        @Test
        fun `forwards actionAt to use case when constructed with a value`() = runTest {
            // ----- Arrange -----
            val book = stubBook()
            dependencies = stubDependencies(this)

            coEvery {
                markBookAsReadUseCase(
                    book = book,
                    editionId = any(),
                    actionAt = "2026-07-21T21:00:00Z",
                )
            } returns Result.success(ShelfMutationOutcome.Applied)

            val action = OnMarkBookAsReadClickAction(
                book = book,
                actionAt = "2026-07-21T21:00:00Z",
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
                    editionId = any(),
                    actionAt = "2026-07-21T21:00:00Z",
                )
            }
        }

        @Test
        fun `forwards null as actionAt when action is constructed without one`() = runTest {
            // ----- Arrange -----
            val book = stubBook()
            dependencies = stubDependencies(this)

            coEvery {
                markBookAsReadUseCase(
                    book = book,
                    editionId = any(),
                    actionAt = null,
                )
            } returns Result.success(ShelfMutationOutcome.Applied)

            val action = OnMarkBookAsReadClickAction(book = book)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                markBookAsReadUseCase(
                    book = book,
                    editionId = any(),
                    actionAt = null,
                )
            }
        }
    }
}
