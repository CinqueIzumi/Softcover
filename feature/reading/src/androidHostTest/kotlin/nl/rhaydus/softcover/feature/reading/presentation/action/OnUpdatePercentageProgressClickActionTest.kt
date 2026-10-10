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
import kotlinx.datetime.LocalDateTime
import nl.rhaydus.softcover.core.book.domain.usecase.RecordBookProgressUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.ShelfMutationOutcome
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.uibinding.progress.toActionAt
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnUpdatePercentageProgressClickActionTest {
    private lateinit var updateBookProgress: RecordBookProgressUseCase
    private val eventChannel = Channel<ReadingScreenEvent>(Channel.BUFFERED)
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
            eventChannel = eventChannel,
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

    private fun stubEditionWithPages(pages: Int?): BookEdition = mockk<BookEdition>().also { edition ->
        every {
            edition.pages
        } returns pages
        every {
            edition.isAudiobook
        } returns false
        every {
            edition.audioSeconds
        } returns null
    }

    private fun stubAudiobookEdition(audioSeconds: Int?): BookEdition =
        mockk<BookEdition>().also { edition ->
            every {
                edition.isAudiobook
            } returns true
            every {
                edition.audioSeconds
            } returns audioSeconds
        }

    private fun stubBookWithCurrentEditionPages(
        pages: Int?,
        id: Int = 99,
    ): Book =
        mockk<Book>().also { book ->
            val edition = stubEditionWithPages(pages = pages)

            every {
                book.id
            } returns id
            every {
                book.currentEdition
            } returns edition
            every {
                book.defaultEdition
            } returns null
            every {
                book.userBook
            } returns null
        }

    private fun stubAudiobook(
        audioSeconds: Int?,
        id: Int = 99,
    ): Book =
        mockk<Book>().also { book ->
            val edition = stubAudiobookEdition(audioSeconds = audioSeconds)

            every {
                book.id
            } returns id
            every {
                book.currentEdition
            } returns edition
            every {
                book.defaultEdition
            } returns null
            every {
                book.userBook
            } returns null
        }

    @Nested
    inner class Execute {
        @Test
        fun `closes the sheet and clears bookToUpdate after recording`() = runTest {
            // ----- Arrange -----
            val book = stubBookWithCurrentEditionPages(pages = 300)
            stateFlow.value = ReadingScreenUiState(
                bookToUpdate = book,
                showProgressSheet = true,
            )
            val dependencies = stubDependencies(this)
            val action = OnUpdatePercentageProgressClickAction(newPercentage = "50")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.showProgressSheet shouldBe false
            stateFlow.value.bookToUpdate shouldBe null
        }

        @Test
        fun `passes actionAt through to updateBookProgress when provided`() = runTest {
            // ----- Arrange -----
            val readAt = LocalDateTime(
                2026,
                7,
                21,
                21,
                0,
            )
            val book = stubBookWithCurrentEditionPages(pages = 200)
            stateFlow.value = ReadingScreenUiState(
                bookToUpdate = book,
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

            val action = OnUpdatePercentageProgressClickAction(
                newPercentage = "50",
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                updateBookProgress(
                    book = book,
                    newPage = 100,
                    newSeconds = null,
                    actionAt = readAt.toActionAt(),
                )
            }
        }

        @Test
        fun `uses 0 percentage when newPercentage is not a valid double`() = runTest {
            // ----- Arrange -----
            val book = stubBookWithCurrentEditionPages(pages = 300)
            stateFlow.value = ReadingScreenUiState(bookToUpdate = book)
            val dependencies = stubDependencies(this)
            val action = OnUpdatePercentageProgressClickAction(newPercentage = "not-a-number")

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
        fun `records nothing and leaves the state alone when there is no bookToUpdate`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(
                bookToUpdate = null,
                showProgressSheet = true,
            )
            val dependencies = stubDependencies(this)
            val action = OnUpdatePercentageProgressClickAction(newPercentage = "50")

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
        fun `closes the sheet without recording when the book has no current edition`() = runTest {
            // ----- Arrange -----
            val book = mockk<Book>().also { b ->
                every {
                    b.id
                } returns 99
                every {
                    b.currentEdition
                } returns null
            }
            stateFlow.value = ReadingScreenUiState(
                bookToUpdate = book,
                showProgressSheet = true,
            )
            val dependencies = stubDependencies(this)
            val action = OnUpdatePercentageProgressClickAction(newPercentage = "50")

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
            stateFlow.value.showProgressSheet shouldBe false
            stateFlow.value.bookToUpdate shouldBe null
        }

        @Test
        fun `uses defaultEdition pages when currentEdition pages is null`() = runTest {
            // ----- Arrange -----
            val currentEdition = stubEditionWithPages(pages = null)
            val defaultEdition = stubEditionWithPages(pages = 400)
            val book = mockk<Book>().also { b ->
                every {
                    b.id
                } returns 99
                every {
                    b.currentEdition
                } returns currentEdition
                every {
                    b.defaultEdition
                } returns defaultEdition
            }
            stateFlow.value = ReadingScreenUiState(bookToUpdate = book)
            val dependencies = stubDependencies(this)
            val action = OnUpdatePercentageProgressClickAction(newPercentage = "25")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                updateBookProgress(
                    book = book,
                    newPage = 100,
                    newSeconds = null,
                )
            }
        }

        @Test
        fun `falls back to 0 pages when both currentEdition and defaultEdition pages are null`() = runTest {
            // ----- Arrange -----
            val currentEdition = stubEditionWithPages(pages = null)
            val defaultEdition = stubEditionWithPages(pages = null)
            val book = mockk<Book>().also { b ->
                every {
                    b.id
                } returns 99
                every {
                    b.currentEdition
                } returns currentEdition
                every {
                    b.defaultEdition
                } returns defaultEdition
            }
            stateFlow.value = ReadingScreenUiState(bookToUpdate = book)
            val dependencies = stubDependencies(this)
            val action = OnUpdatePercentageProgressClickAction(newPercentage = "50")

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
        fun `computes newSeconds from percentage and audioSeconds for an audiobook edition`() = runTest {
            // ----- Arrange -----
            val book = stubAudiobook(audioSeconds = 3600)
            stateFlow.value = ReadingScreenUiState(bookToUpdate = book)
            val dependencies = stubDependencies(this)
            val action = OnUpdatePercentageProgressClickAction(newPercentage = "50")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                updateBookProgress(
                    book = book,
                    newPage = null,
                    newSeconds = 1800,
                )
            }
        }

        @Test
        fun `clamps percentage above 100 to 100 when edition is audiobook`() = runTest {
            // ----- Arrange -----
            val book = stubAudiobook(audioSeconds = 3600)
            stateFlow.value = ReadingScreenUiState(bookToUpdate = book)
            val dependencies = stubDependencies(this)
            val action = OnUpdatePercentageProgressClickAction(newPercentage = "150")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                updateBookProgress(
                    book = book,
                    newPage = null,
                    newSeconds = 3600,
                )
            }
        }

        @Test
        fun `clamps percentage below 0 to 0 when edition is audiobook`() = runTest {
            // ----- Arrange -----
            val book = stubAudiobook(audioSeconds = 3600)
            stateFlow.value = ReadingScreenUiState(bookToUpdate = book)
            val dependencies = stubDependencies(this)
            val action = OnUpdatePercentageProgressClickAction(newPercentage = "-50")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                updateBookProgress(
                    book = book,
                    newPage = null,
                    newSeconds = 0,
                )
            }
        }

        @Test
        fun `uses 0 seconds when audioSeconds is null and edition is audiobook`() = runTest {
            // ----- Arrange -----
            val book = stubAudiobook(audioSeconds = null)
            stateFlow.value = ReadingScreenUiState(bookToUpdate = book)
            val dependencies = stubDependencies(this)
            val action = OnUpdatePercentageProgressClickAction(newPercentage = "50")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                updateBookProgress(
                    book = book,
                    newPage = null,
                    newSeconds = 0,
                )
            }
        }

        @Test
        fun `flags the book as failed when updateBookProgress fails`() = runTest {
            // ----- Arrange -----
            val book = stubBookWithCurrentEditionPages(
                pages = 200,
                id = 3,
            )
            stateFlow.value = ReadingScreenUiState(bookToUpdate = book)
            val dependencies = stubDependencies(this)

            coEvery {
                updateBookProgress(
                    book = book,
                    newPage = any(),
                    newSeconds = any(),
                )
            } returns Result.failure(RuntimeException("network error"))

            val action = OnUpdatePercentageProgressClickAction(newPercentage = "50")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.failedMutationBookIds.contains(3) shouldBe true
        }

        @Test
        fun `prompts for a verdict when the outcome is Applied`() = runTest {
            // ----- Arrange -----
            val book = stubBookWithCurrentEditionPages(
                pages = 200,
                id = 3,
            )
            stateFlow.value = ReadingScreenUiState(bookToUpdate = book)
            val dependencies = stubDependencies(this)

            coEvery {
                updateBookProgress(
                    book = book,
                    newPage = any(),
                    newSeconds = any(),
                )
            } returns Result.success(ShelfMutationOutcome.Applied)

            val action = OnUpdatePercentageProgressClickAction(newPercentage = "50")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.verdictPromptBook shouldBe book
        }

        @Test
        fun `sends ProgressUpdatedEvent when the update succeeds without finishing the book`() = runTest {
            // ----- Arrange -----
            val book = stubBookWithCurrentEditionPages(
                pages = 200,
                id = 3,
            )
            stateFlow.value = ReadingScreenUiState(bookToUpdate = book)
            val dependencies = stubDependencies(this)

            coEvery {
                updateBookProgress(
                    book = book,
                    newPage = any(),
                    newSeconds = any(),
                )
            } returns Result.success(ShelfMutationOutcome.NoChange)

            val action = OnUpdatePercentageProgressClickAction(newPercentage = "50")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            eventChannel.tryReceive().getOrNull().shouldBeInstanceOf<ReadingScreenEvent.ProgressUpdatedEvent>()
        }

        @Test
        fun `sends no ProgressUpdatedEvent when the update finishes the book`() = runTest {
            // ----- Arrange -----
            val book = stubBookWithCurrentEditionPages(
                pages = 200,
                id = 3,
            )
            stateFlow.value = ReadingScreenUiState(bookToUpdate = book)
            val dependencies = stubDependencies(this)

            coEvery {
                updateBookProgress(
                    book = book,
                    newPage = any(),
                    newSeconds = any(),
                )
            } returns Result.success(ShelfMutationOutcome.Applied)

            val action = OnUpdatePercentageProgressClickAction(newPercentage = "50")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            eventChannel.tryReceive().getOrNull() shouldBe null
        }

        @Test
        fun `sends ProgressUpdateFailedEvent when updateBookProgress fails`() = runTest {
            // ----- Arrange -----
            val book = stubBookWithCurrentEditionPages(
                pages = 200,
                id = 3,
            )
            stateFlow.value = ReadingScreenUiState(bookToUpdate = book)
            val dependencies = stubDependencies(this)

            coEvery {
                updateBookProgress(
                    book = book,
                    newPage = any(),
                    newSeconds = any(),
                )
            } returns Result.failure(RuntimeException("network error"))

            val action = OnUpdatePercentageProgressClickAction(newPercentage = "50")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            eventChannel.tryReceive().getOrNull().shouldBeInstanceOf<ReadingScreenEvent.ProgressUpdateFailedEvent>()
        }

        @Test
        fun `cancels the prior job for the same book id and stores a new one`() = runTest {
            // ----- Arrange -----
            val bookId = 3
            val book = stubBookWithCurrentEditionPages(
                pages = 200,
                id = bookId,
            )
            val priorJob = Job()

            localVariablesFlow.value = ReadingLocalVariables(
                bookMutationJobs = mapOf(bookId to priorJob),
            )

            stateFlow.value = ReadingScreenUiState(bookToUpdate = book)

            val dependencies = stubDependencies(this)

            val action = OnUpdatePercentageProgressClickAction(newPercentage = "50")

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
