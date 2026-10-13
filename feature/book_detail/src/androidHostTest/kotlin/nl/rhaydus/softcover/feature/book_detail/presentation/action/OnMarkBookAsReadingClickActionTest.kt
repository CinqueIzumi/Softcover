package nl.rhaydus.softcover.feature.book_detail.presentation.action

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
import nl.rhaydus.softcover.core.book.domain.usecase.MarkBookAsReadingUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.ShelfMutationOutcome
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.feature.book_detail.presentation.buildBookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnMarkBookAsReadingClickActionTest {
    private lateinit var markBookAsReadingUseCase: MarkBookAsReadingUseCase
    private lateinit var dependencies: BookDetailDependencies
    private lateinit var eventChannel: Channel<BookDetailEvent>
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var localVariablesFlow: MutableStateFlow<BookDetailLocalVariables>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        markBookAsReadingUseCase = mockk()
        eventChannel = Channel(Channel.BUFFERED)
        stateFlow = MutableStateFlow(buildBookDetailUiState())
        localVariablesFlow = MutableStateFlow(BookDetailLocalVariables())
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
                mock.markBookAsReadingUseCase
            } returns markBookAsReadingUseCase

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

    private fun stubBook(
        id: Int = 42,
        userBook: UserBook? = null,
    ): Book = mockk<Book>().also { mock ->
        every {
            mock.id
        } returns id

        every {
            mock.userBook
        } returns userBook
    }

    private fun stubEdition(id: Int = 55): BookEdition = mockk<BookEdition>().also { mock ->
        every {
            mock.id
        } returns id
    }

    @Nested
    inner class Execute {
        @Test
        fun `sets pendingShelfStatus to Reading when the book is not on a shelf`() = runTest {
            // ----- Arrange -----
            val book = stubBook(
                id = 42,
                userBook = null,
            )
            dependencies = stubDependencies(this)

            coEvery {
                markBookAsReadingUseCase(
                    book = book,
                    editionId = any(),
                )
            } returns Result.success(ShelfMutationOutcome.Applied)

            val action = OnMarkBookAsReadingClickAction(book = book)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.pendingShelfStatus shouldBe BookStatus.Reading
        }

        @Test
        fun `leaves pendingShelfStatus null when the book already has a user book`() = runTest {
            // ----- Arrange -----
            val book = stubBook(
                id = 42,
                userBook = mockk<UserBook>(),
            )
            dependencies = stubDependencies(this)

            coEvery {
                markBookAsReadingUseCase(
                    book = book,
                    editionId = any(),
                )
            } returns Result.success(ShelfMutationOutcome.Applied)

            val action = OnMarkBookAsReadingClickAction(book = book)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.pendingShelfStatus.shouldBeNull()
        }

        @Test
        fun `clears pendingShelfStatus when the use case fails for an unshelved book`() = runTest {
            // ----- Arrange -----
            val book = stubBook(
                id = 42,
                userBook = null,
            )
            dependencies = stubDependencies(this)

            coEvery {
                markBookAsReadingUseCase(
                    book = book,
                    editionId = any(),
                )
            } returns Result.failure(RuntimeException("api error"))

            val action = OnMarkBookAsReadingClickAction(book = book)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.pendingShelfStatus.shouldBeNull()
        }

        @Test
        fun `leaves a newer tap's pendingShelfStatus untouched when the use case fails`() = runTest {
            // ----- Arrange -----
            val book = stubBook(
                id = 42,
                userBook = null,
            )
            dependencies = stubDependencies(this)

            coEvery {
                markBookAsReadingUseCase(
                    book = book,
                    editionId = any(),
                )
            } coAnswers {
                stateFlow.value = stateFlow.value.copy(pendingShelfStatus = BookStatus.Read)
                Result.failure(RuntimeException("api error"))
            }

            val action = OnMarkBookAsReadingClickAction(book = book)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.pendingShelfStatus shouldBe BookStatus.Read
        }

        @Test
        fun `adds book id to failedMutationBookIds when use case fails`() = runTest {
            // ----- Arrange -----
            val book = stubBook(id = 42)
            dependencies = stubDependencies(this)

            coEvery {
                markBookAsReadingUseCase(
                    book = book,
                    editionId = any(),
                )
            } returns Result.failure(RuntimeException("api error"))

            val action = OnMarkBookAsReadingClickAction(book = book)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.failedMutationBookIds.contains(42) shouldBe true
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
                markBookAsReadingUseCase(
                    book = book,
                    editionId = any(),
                )
            } returns Result.success(ShelfMutationOutcome.Applied)

            val action = OnMarkBookAsReadingClickAction(book = book)

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
        fun `forwards previewEdition id as editionId when preview is set`() = runTest {
            // ----- Arrange -----
            val book = stubBook(id = 42)
            val preview = stubEdition(id = 55)
            stateFlow.value = buildBookDetailUiState().copy(previewEdition = preview)
            dependencies = stubDependencies(this)

            coEvery {
                markBookAsReadingUseCase(
                    book = book,
                    editionId = 55,
                )
            } returns Result.success(ShelfMutationOutcome.Applied)

            val action = OnMarkBookAsReadingClickAction(book = book)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                markBookAsReadingUseCase(
                    book = book,
                    editionId = 55,
                )
            }
        }

        @Test
        fun `uses scannedEditionId as editionId when previewEdition is null`() = runTest {
            // ----- Arrange -----
            val book = stubBook(id = 42)
            stateFlow.value = buildBookDetailUiState().copy(
                previewEdition = null,
                scannedEditionId = 555,
            )
            dependencies = stubDependencies(this)

            coEvery {
                markBookAsReadingUseCase(
                    book = book,
                    editionId = 555,
                )
            } returns Result.success(ShelfMutationOutcome.Applied)

            val action = OnMarkBookAsReadingClickAction(book = book)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                markBookAsReadingUseCase(
                    book = book,
                    editionId = 555,
                )
            }
        }

        @Test
        fun `forwards null as editionId when no preview is set`() = runTest {
            // ----- Arrange -----
            val book = stubBook(id = 42)
            stateFlow.value = buildBookDetailUiState().copy(previewEdition = null)
            dependencies = stubDependencies(this)

            coEvery {
                markBookAsReadingUseCase(
                    book = book,
                    editionId = null,
                )
            } returns Result.success(ShelfMutationOutcome.Applied)

            val action = OnMarkBookAsReadingClickAction(book = book)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                markBookAsReadingUseCase(
                    book = book,
                    editionId = null,
                )
            }
        }

        @Test
        fun `sends ShelfChangedEvent when use case succeeds`() = runTest {
            // ----- Arrange -----
            val book = stubBook(id = 42)
            dependencies = stubDependencies(this)

            coEvery {
                markBookAsReadingUseCase(
                    book = book,
                    editionId = any(),
                )
            } returns Result.success(ShelfMutationOutcome.Applied)

            val action = OnMarkBookAsReadingClickAction(book = book)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            val event = eventChannel.tryReceive().getOrNull()
            event.shouldBeInstanceOf<BookDetailEvent.ShelfChangedEvent>()
        }

        @Test
        fun `sends no event when use case fails`() = runTest {
            // ----- Arrange -----
            val book = stubBook(id = 42)
            dependencies = stubDependencies(this)

            coEvery {
                markBookAsReadingUseCase(
                    book = book,
                    editionId = any(),
                )
            } returns Result.failure(RuntimeException("api error"))

            val action = OnMarkBookAsReadingClickAction(book = book)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            eventChannel.tryReceive().getOrNull().shouldBeNull()
        }
    }
}
