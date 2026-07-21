package nl.rhaydus.softcover.feature.book_detail.presentation.action

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.book.domain.usecase.RecordBookProgressUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.ShelfMutationOutcome
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookMarkedAsReadEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

class OnUpdatePageProgressClickActionTest {
    private lateinit var updateBookProgress: RecordBookProgressUseCase
    private lateinit var dependencies: BookDetailDependencies
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var eventChannel: Channel<BookDetailEvent>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        updateBookProgress = mockk(relaxed = true)
        stateFlow = MutableStateFlow(BookDetailUiState())
        eventChannel = Channel(Channel.BUFFERED)
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(BookDetailLocalVariables()),
            eventChannel = eventChannel,
        )

        coEvery {
            updateBookProgress(
                any(),
                any(),
                any(),
            )
        } returns Result.success(null)
    }

    private fun stubDependencies(testScope: TestScope): BookDetailDependencies {
        val dispatcher = UnconfinedTestDispatcher(testScope.testScheduler)
        return mockk<BookDetailDependencies>(relaxed = true).also { mock ->
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

    private fun stubBook(id: Int = 42): Book = mockk<Book>().also { mock ->
        every {
            mock.id
        } returns id
    }

    @Nested
    inner class Execute {
        @Test
        fun `hides the update progress sheet after execution`() = runTest {
            // ----- Arrange -----
            val book = stubBook()
            stateFlow.value = BookDetailUiState(
                book = book,
                showUpdateProgressSheet = true,
            )
            dependencies = stubDependencies(this)

            val action = OnUpdatePageProgressClickAction(newPage = "100")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.showUpdateProgressSheet shouldBe false
        }

        @Test
        fun `invokes updateBookProgress with the parsed page number`() = runTest {
            // ----- Arrange -----
            val book = stubBook()
            stateFlow.value = BookDetailUiState(book = book)
            dependencies = stubDependencies(this)

            val action = OnUpdatePageProgressClickAction(newPage = "250")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                updateBookProgress(
                    book = book,
                    newPage = 250,
                )
            }
        }

        @Test
        fun `treats a non-numeric page string as zero`() = runTest {
            // ----- Arrange -----
            val book = stubBook()
            stateFlow.value = BookDetailUiState(book = book)
            dependencies = stubDependencies(this)

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
                )
            }
        }

        @Test
        fun `treats an empty page string as zero`() = runTest {
            // ----- Arrange -----
            val book = stubBook()
            stateFlow.value = BookDetailUiState(book = book)
            dependencies = stubDependencies(this)

            val action = OnUpdatePageProgressClickAction(newPage = "")

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
                )
            }
        }

        @Test
        fun `does nothing when book in state is null`() = runTest {
            // ----- Arrange -----
            stateFlow.value = BookDetailUiState(
                book = null,
                showUpdateProgressSheet = true,
            )
            dependencies = stubDependencies(this)

            val action = OnUpdatePageProgressClickAction(newPage = "50")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.showUpdateProgressSheet shouldBe true
            coVerify(exactly = 0) { updateBookProgress(
                any(),
                any(),
                any(),
            ) }
        }

        @Test
        fun `treats a decimal page string as zero because toIntOrNull returns null for decimals`() = runTest {
            // ----- Arrange -----
            val book = stubBook()
            stateFlow.value = BookDetailUiState(book = book)
            dependencies = stubDependencies(this)

            val action = OnUpdatePageProgressClickAction(newPage = "10.5")

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
                )
            }
        }

        @Test
        fun `parses a negative page string to its negative integer value`() = runTest {
            // ----- Arrange -----
            val book = stubBook()
            stateFlow.value = BookDetailUiState(book = book)
            dependencies = stubDependencies(this)

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
                )
            }
        }

        @Test
        fun `sends BookMarkedAsReadEvent when use case returns Applied`() = runTest {
            // ----- Arrange -----
            val book = stubBook()
            stateFlow.value = BookDetailUiState(book = book)
            dependencies = stubDependencies(this)

            coEvery {
                updateBookProgress(
                    book = any(),
                    newPage = any(),
                )
            } returns Result.success(ShelfMutationOutcome.Applied)

            val action = OnUpdatePageProgressClickAction(newPage = "100")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            val event = eventChannel.tryReceive().getOrNull()
            event.shouldBeInstanceOf<BookMarkedAsReadEvent>()
        }

        @Test
        fun `adds book id to failedMutationBookIds when use case fails`() = runTest {
            // ----- Arrange -----
            val book = stubBook(id = 42)
            stateFlow.value = BookDetailUiState(book = book)
            dependencies = stubDependencies(this)

            coEvery {
                updateBookProgress(
                    book = any(),
                    newPage = any(),
                )
            } returns Result.failure(RuntimeException("api error"))

            val action = OnUpdatePageProgressClickAction(newPage = "100")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.failedMutationBookIds.contains(42) shouldBe true
        }
    }
}
