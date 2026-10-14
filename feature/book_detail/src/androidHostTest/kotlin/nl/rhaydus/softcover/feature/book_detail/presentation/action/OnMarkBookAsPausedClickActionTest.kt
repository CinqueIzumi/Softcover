package nl.rhaydus.softcover.feature.book_detail.presentation.action

import io.kotest.matchers.nulls.shouldBeNull
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
import nl.rhaydus.softcover.core.book.domain.usecase.PauseBookUseCase
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

class OnMarkBookAsPausedClickActionTest {
    private lateinit var pauseBookUseCase: PauseBookUseCase
    private lateinit var dependencies: BookDetailDependencies
    private lateinit var eventChannel: Channel<BookDetailEvent>
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        pauseBookUseCase = mockk()
        eventChannel = Channel(Channel.BUFFERED)
        stateFlow = MutableStateFlow(buildBookDetailUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(BookDetailLocalVariables()),
            eventChannel = eventChannel,
        )
    }

    private fun stubDependencies(testScope: TestScope): BookDetailDependencies {
        val dispatcher = UnconfinedTestDispatcher(testScope.testScheduler)

        return mockk<BookDetailDependencies>(relaxed = true).also { mock ->
            every {
                mock.pauseBookUseCase
            } returns pauseBookUseCase

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

    @Nested
    inner class Execute {
        @Test
        fun `pauses the book when no session is running`() = runTest {
            // ----- Arrange -----
            val book = stubBook()
            dependencies = stubDependencies(this)

            coEvery {
                pauseBookUseCase(
                    book = book,
                    editionId = any(),
                )
            } returns Result.success(ShelfMutationOutcome.Applied)

            // ----- Act -----
            OnMarkBookAsPausedClickAction(book = book).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                pauseBookUseCase(
                    book = book,
                    editionId = null,
                )
            }
        }

        @Test
        fun `pauses the book when the running session belongs to another book`() = runTest {
            // ----- Arrange -----
            val book = stubBook(id = 42)
            stateFlow.value = buildBookDetailUiState().copy(activeSessionBookId = 7)
            dependencies = stubDependencies(this)

            coEvery {
                pauseBookUseCase(
                    book = book,
                    editionId = any(),
                )
            } returns Result.success(ShelfMutationOutcome.Applied)

            // ----- Act -----
            OnMarkBookAsPausedClickAction(book = book).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                pauseBookUseCase(
                    book = book,
                    editionId = any(),
                )
            }
        }

        @Test
        fun `requests the end-session confirm instead of pausing when the session is this book`() = runTest {
            // ----- Arrange -----
            val book = stubBook(id = 42)
            stateFlow.value = buildBookDetailUiState().copy(activeSessionBookId = 42)
            dependencies = stubDependencies(this)

            // ----- Act -----
            OnMarkBookAsPausedClickAction(book = book).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.endSessionPauseRequested shouldBe true
            coVerify(exactly = 0) {
                pauseBookUseCase(
                    book = any(),
                    editionId = any(),
                )
            }
        }

        @Test
        fun `pauses despite a matching session when the pause is already confirmed`() = runTest {
            // ----- Arrange -----
            val book = stubBook(id = 42)
            stateFlow.value = buildBookDetailUiState().copy(activeSessionBookId = 42)
            dependencies = stubDependencies(this)

            coEvery {
                pauseBookUseCase(
                    book = book,
                    editionId = any(),
                )
            } returns Result.success(ShelfMutationOutcome.Applied)

            // ----- Act -----
            OnMarkBookAsPausedClickAction(
                book = book,
                sessionPauseConfirmed = true,
            ).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                pauseBookUseCase(
                    book = book,
                    editionId = any(),
                )
            }
        }

        @Test
        fun `sets pendingShelfStatus to Paused when the book is not on a shelf`() = runTest {
            // ----- Arrange -----
            val book = stubBook(userBook = null)
            dependencies = stubDependencies(this)

            coEvery {
                pauseBookUseCase(
                    book = book,
                    editionId = any(),
                )
            } returns Result.success(ShelfMutationOutcome.Applied)

            // ----- Act -----
            OnMarkBookAsPausedClickAction(book = book).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.pendingShelfStatus shouldBe BookStatus.Paused
        }

        @Test
        fun `leaves pendingShelfStatus null when the book already has a user book`() = runTest {
            // ----- Arrange -----
            val book = stubBook(userBook = mockk<UserBook>())
            dependencies = stubDependencies(this)

            coEvery {
                pauseBookUseCase(
                    book = book,
                    editionId = any(),
                )
            } returns Result.success(ShelfMutationOutcome.Applied)

            // ----- Act -----
            OnMarkBookAsPausedClickAction(book = book).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.pendingShelfStatus.shouldBeNull()
        }

        @Test
        fun `rolls back pendingShelfStatus and flags the failure when the use case fails`() = runTest {
            // ----- Arrange -----
            val book = stubBook(id = 42)
            dependencies = stubDependencies(this)

            coEvery {
                pauseBookUseCase(
                    book = book,
                    editionId = any(),
                )
            } returns Result.failure(RuntimeException("api error"))

            // ----- Act -----
            OnMarkBookAsPausedClickAction(book = book).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.pendingShelfStatus.shouldBeNull()
            stateFlow.value.failedMutationBookIds.contains(42) shouldBe true
        }

        @Test
        fun `leaves a newer tap's pendingShelfStatus untouched when the use case fails`() = runTest {
            // ----- Arrange -----
            val book = stubBook(id = 42)
            dependencies = stubDependencies(this)

            coEvery {
                pauseBookUseCase(
                    book = book,
                    editionId = any(),
                )
            } coAnswers {
                stateFlow.value = stateFlow.value.copy(pendingShelfStatus = BookStatus.Read)
                Result.failure(RuntimeException("api error"))
            }

            // ----- Act -----
            OnMarkBookAsPausedClickAction(book = book).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.pendingShelfStatus shouldBe BookStatus.Read
        }

        @Test
        fun `forwards the preview edition id as editionId`() = runTest {
            // ----- Arrange -----
            val book = stubBook()
            val preview = mockk<BookEdition>().also {
                every {
                    it.id
                } returns 55
            }
            stateFlow.value = buildBookDetailUiState().copy(previewEdition = preview)
            dependencies = stubDependencies(this)

            coEvery {
                pauseBookUseCase(
                    book = book,
                    editionId = 55,
                )
            } returns Result.success(ShelfMutationOutcome.Applied)

            // ----- Act -----
            OnMarkBookAsPausedClickAction(book = book).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                pauseBookUseCase(
                    book = book,
                    editionId = 55,
                )
            }
        }

        @Test
        fun `sends ShelfChangedEvent when the use case succeeds`() = runTest {
            // ----- Arrange -----
            val book = stubBook()
            dependencies = stubDependencies(this)

            coEvery {
                pauseBookUseCase(
                    book = book,
                    editionId = any(),
                )
            } returns Result.success(ShelfMutationOutcome.Applied)

            // ----- Act -----
            OnMarkBookAsPausedClickAction(book = book).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            eventChannel.tryReceive().getOrNull().shouldBeInstanceOf<BookDetailEvent.ShelfChangedEvent>()
        }
    }
}
