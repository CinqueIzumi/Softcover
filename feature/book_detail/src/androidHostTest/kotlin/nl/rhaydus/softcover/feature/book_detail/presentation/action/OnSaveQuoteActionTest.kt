package nl.rhaydus.softcover.feature.book_detail.presentation.action

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import nl.rhaydus.designsystem.util.SnackBarManager
import nl.rhaydus.softcover.core.book.domain.usecase.RecordBookProgressUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.ShelfMutationOutcome
import nl.rhaydus.softcover.core.component.quote.QuotePositionTab
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.PrivacySetting
import nl.rhaydus.softcover.core.domain.model.QuotePosition
import nl.rhaydus.softcover.core.personal.domain.usecase.AddQuoteUseCase
import nl.rhaydus.softcover.core.uibinding.progress.toActionAt
import nl.rhaydus.softcover.feature.book_detail.presentation.buildBookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.buildQuoteBook
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.state.QuoteDraft
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnSaveQuoteActionTest {
    private lateinit var addQuoteUseCase: AddQuoteUseCase
    private lateinit var recordBookProgressUseCase: RecordBookProgressUseCase
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var eventChannel: Channel<BookDetailEvent>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        addQuoteUseCase = mockk()
        recordBookProgressUseCase = mockk()
        stateFlow = MutableStateFlow(buildBookDetailUiState())
        eventChannel = Channel(Channel.BUFFERED)
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(BookDetailLocalVariables()),
            eventChannel = eventChannel,
        )

        mockkObject(SnackBarManager)
        every {
            SnackBarManager.showSnackbar(title = any())
        } returns Unit

        coEvery {
            addQuoteUseCase(
                bookId = any(),
                editionId = any(),
                text = any(),
                privacy = any(),
                actionAt = any(),
                position = any(),
                bookTitle = any(),
                editionTitle = any(),
                editionCoverUrl = any(),
            )
        } returns Result.success(1L)
        coEvery {
            recordBookProgressUseCase(
                book = any(),
                newPage = any(),
                newSeconds = any(),
                actionAt = any(),
            )
        } returns Result.success(null)
    }

    @AfterEach
    fun tearDown() {
        unmockkObject(SnackBarManager)
    }

    private fun stubDependencies(testScope: TestScope): BookDetailDependencies {
        val dispatcher = UnconfinedTestDispatcher(testScope.testScheduler)

        return mockk<BookDetailDependencies>(relaxed = true).also { mock ->
            every {
                mock.addQuoteUseCase
            } returns addQuoteUseCase
            every {
                mock.recordBookProgressUseCase
            } returns recordBookProgressUseCase
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

    private fun pagedBook(readPage: Int = 180): Book = buildQuoteBook(
        currentPages = 437,
        readPage = readPage,
    )

    private fun draft(
        text: String = "A line worth keeping",
        positionText: String = "214",
        progressAdvanceEnabled: Boolean = true,
        readAt: LocalDateTime? = null,
    ): QuoteDraft = QuoteDraft(
        tab = QuotePositionTab.PAGE,
        privacy = PrivacySetting.FOLLOWERS,
        text = text,
        positionText = positionText,
        progressAdvanceEnabled = progressAdvanceEnabled,
        readAt = readAt,
    )

    private suspend fun execute(testScope: TestScope) {
        OnSaveQuoteAction().execute(
            dependencies = stubDependencies(testScope),
            scope = scope,
        )
    }

    @Nested
    inner class Execute {
        @Test
        fun `records progress and adds the quote when the toggle is on and the position is ahead`() = runTest {
            // ----- Arrange -----
            val book = pagedBook()
            stateFlow.value = buildBookDetailUiState().copy(
                book = book,
                quoteDraft = draft(),
            )

            // ----- Act -----
            execute(this)

            // ----- Assert -----
            coVerify {
                recordBookProgressUseCase(
                    book = book,
                    newPage = 214,
                    newSeconds = null,
                    actionAt = null,
                )
            }
            coVerify {
                addQuoteUseCase(
                    bookId = 7,
                    editionId = 70,
                    text = "A line worth keeping",
                    privacy = PrivacySetting.FOLLOWERS,
                    actionAt = null,
                    position = QuotePosition.pages(
                        page = 214,
                        totalPages = 437,
                    ),
                    bookTitle = "Piranesi",
                    editionTitle = any(),
                    editionCoverUrl = any(),
                )
            }
        }

        @Test
        fun `stamps both writes with the backdate from the draft`() = runTest {
            // ----- Arrange -----
            val readAt = LocalDateTime(
                2024,
                5,
                6,
                7,
                8,
            )
            val book = pagedBook()
            stateFlow.value = buildBookDetailUiState().copy(
                book = book,
                quoteDraft = draft(readAt = readAt),
            )

            // ----- Act -----
            execute(this)

            // ----- Assert -----
            coVerify {
                recordBookProgressUseCase(
                    book = book,
                    newPage = 214,
                    newSeconds = null,
                    actionAt = readAt.toActionAt(),
                )
            }
            coVerify {
                addQuoteUseCase(
                    bookId = 7,
                    editionId = 70,
                    text = "A line worth keeping",
                    privacy = PrivacySetting.FOLLOWERS,
                    actionAt = readAt.toInstant(TimeZone.currentSystemDefault()),
                    position = QuotePosition.pages(
                        page = 214,
                        totalPages = 437,
                    ),
                    bookTitle = "Piranesi",
                    editionTitle = any(),
                    editionCoverUrl = any(),
                )
            }
        }

        @Test
        fun `adds the quote without recording progress when the toggle is off`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(
                book = pagedBook(),
                quoteDraft = draft(progressAdvanceEnabled = false),
            )

            // ----- Act -----
            execute(this)

            // ----- Assert -----
            coVerify(exactly = 0) {
                recordBookProgressUseCase(
                    book = any(),
                    newPage = any(),
                    newSeconds = any(),
                    actionAt = any(),
                )
            }
            coVerify(exactly = 1) {
                addQuoteUseCase(
                    bookId = any(),
                    editionId = any(),
                    text = any(),
                    privacy = any(),
                    actionAt = any(),
                    position = any(),
                    bookTitle = any(),
                    editionTitle = any(),
                    editionCoverUrl = any(),
                )
            }
        }

        @Test
        fun `stores the book title on the quote`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(
                book = buildQuoteBook(
                    title = "The Ministry of Time",
                    currentPages = 437,
                    readPage = 180,
                ),
                quoteDraft = draft(),
            )

            // ----- Act -----
            execute(this)

            // ----- Assert -----
            coVerify(exactly = 1) {
                addQuoteUseCase(
                    bookId = any(),
                    editionId = any(),
                    text = any(),
                    privacy = any(),
                    actionAt = any(),
                    position = any(),
                    bookTitle = "The Ministry of Time",
                    editionTitle = any(),
                    editionCoverUrl = any(),
                )
            }
        }

        @Test
        fun `passes the current edition's title and cover url to the use case`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(
                book = buildQuoteBook(
                    currentPages = 437,
                    readPage = 180,
                    editionTitle = "Piranesi (Illustrated)",
                    editionUrl = "https://example.com/cover.jpg",
                ),
                quoteDraft = draft(),
            )

            // ----- Act -----
            execute(this)

            // ----- Assert -----
            coVerify(exactly = 1) {
                addQuoteUseCase(
                    bookId = any(),
                    editionId = any(),
                    text = any(),
                    privacy = any(),
                    actionAt = any(),
                    position = any(),
                    bookTitle = any(),
                    editionTitle = "Piranesi (Illustrated)",
                    editionCoverUrl = "https://example.com/cover.jpg",
                )
            }
        }

        @Test
        fun `trims the quote text before saving it`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(
                book = pagedBook(),
                quoteDraft = draft(text = "  A line worth keeping \n"),
            )

            // ----- Act -----
            execute(this)

            // ----- Assert -----
            coVerify(exactly = 1) {
                addQuoteUseCase(
                    bookId = any(),
                    editionId = any(),
                    text = "A line worth keeping",
                    privacy = any(),
                    actionAt = any(),
                    position = any(),
                    bookTitle = any(),
                    editionTitle = any(),
                    editionCoverUrl = any(),
                )
            }
        }

        @Test
        fun `saves nothing and keeps the draft when the text is blank`() = runTest {
            // ----- Arrange -----
            val blankDraft = draft(text = "   ")
            stateFlow.value = buildBookDetailUiState().copy(
                book = pagedBook(),
                quoteDraft = blankDraft,
            )

            // ----- Act -----
            execute(this)

            // ----- Assert -----
            coVerify(exactly = 0) {
                addQuoteUseCase(
                    bookId = any(),
                    editionId = any(),
                    text = any(),
                    privacy = any(),
                    actionAt = any(),
                    position = any(),
                    bookTitle = any(),
                    editionTitle = any(),
                    editionCoverUrl = any(),
                )
            }
            coVerify(exactly = 0) {
                recordBookProgressUseCase(
                    book = any(),
                    newPage = any(),
                    newSeconds = any(),
                    actionAt = any(),
                )
            }
            stateFlow.value.quoteDraft shouldBe blankDraft
        }

        @Test
        fun `clears the draft once the quote is saved`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(
                book = pagedBook(),
                quoteDraft = draft(),
            )

            // ----- Act -----
            execute(this)

            // ----- Assert -----
            stateFlow.value.quoteDraft.shouldBeNull()
        }

        @Test
        fun `still saves the quote when recording progress fails`() = runTest {
            // ----- Arrange -----
            coEvery {
                recordBookProgressUseCase(
                    book = any(),
                    newPage = any(),
                    newSeconds = any(),
                    actionAt = any(),
                )
            } returns Result.failure(RuntimeException("progress failed"))
            stateFlow.value = buildBookDetailUiState().copy(
                book = pagedBook(),
                quoteDraft = draft(),
            )

            // ----- Act -----
            execute(this)

            // ----- Assert -----
            coVerify(exactly = 1) {
                addQuoteUseCase(
                    bookId = any(),
                    editionId = any(),
                    text = any(),
                    privacy = any(),
                    actionAt = any(),
                    position = any(),
                    bookTitle = any(),
                    editionTitle = any(),
                    editionCoverUrl = any(),
                )
            }
            stateFlow.value.failedMutationBookIds shouldBe setOf(7)
        }

        @Test
        fun `celebrates the finish when the quote's position ends the book`() = runTest {
            // ----- Arrange -----
            coEvery {
                recordBookProgressUseCase(
                    book = any(),
                    newPage = any(),
                    newSeconds = any(),
                    actionAt = any(),
                )
            } returns Result.success(ShelfMutationOutcome.Applied)
            stateFlow.value = buildBookDetailUiState().copy(
                book = pagedBook(),
                quoteDraft = draft(positionText = "437"),
            )

            // ----- Act -----
            execute(this)

            // ----- Assert -----
            eventChannel.tryReceive().getOrNull().shouldBeInstanceOf<BookDetailEvent.BookMarkedAsReadEvent>()
        }

        @Test
        fun `reports updated progress when the position only moves the reader forward`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(
                book = pagedBook(),
                quoteDraft = draft(),
            )

            // ----- Act -----
            execute(this)

            // ----- Assert -----
            eventChannel.tryReceive().getOrNull().shouldBeInstanceOf<BookDetailEvent.ProgressUpdatedEvent>()
        }

        @Test
        fun `shows a failure message and sends MutationFailedEvent when adding the quote fails`() = runTest {
            // ----- Arrange -----
            coEvery {
                addQuoteUseCase(
                    bookId = any(),
                    editionId = any(),
                    text = any(),
                    privacy = any(),
                    actionAt = any(),
                    position = any(),
                    bookTitle = any(),
                    editionTitle = any(),
                    editionCoverUrl = any(),
                )
            } returns Result.failure(RuntimeException("add failed"))
            stateFlow.value = buildBookDetailUiState().copy(
                book = pagedBook(),
                quoteDraft = draft(progressAdvanceEnabled = false),
            )

            // ----- Act -----
            execute(this)

            // ----- Assert -----
            verify(exactly = 1) {
                SnackBarManager.showSnackbar(title = "Couldn't save your quote. Try again.")
            }
            eventChannel.tryReceive().getOrNull().shouldBeInstanceOf<BookDetailEvent.MutationFailedEvent>()
        }

        @Test
        fun `records an audiobook's progress in seconds`() = runTest {
            // ----- Arrange -----
            val book = buildQuoteBook(
                isAudiobook = true,
                audioSeconds = 36_000,
                readSeconds = 2700,
            )
            stateFlow.value = buildBookDetailUiState().copy(
                book = book,
                quoteDraft = QuoteDraft(
                    tab = QuotePositionTab.TIME,
                    privacy = PrivacySetting.PUBLIC,
                    text = "A line worth keeping",
                    hours = "1",
                    minutes = "2",
                    seconds = "3",
                ),
            )

            // ----- Act -----
            execute(this)

            // ----- Assert -----
            coVerify {
                recordBookProgressUseCase(
                    book = book,
                    newPage = null,
                    newSeconds = 3723,
                    actionAt = null,
                )
            }
        }
    }
}
