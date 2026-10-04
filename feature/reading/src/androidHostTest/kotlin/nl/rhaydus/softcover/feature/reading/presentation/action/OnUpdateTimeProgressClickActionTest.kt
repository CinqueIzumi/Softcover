package nl.rhaydus.softcover.feature.reading.presentation.action

import kotlinx.datetime.LocalDateTime
import nl.rhaydus.softcover.core.book.domain.usecase.RecordBookProgressUseCase
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
import nl.rhaydus.softcover.core.book.domain.usecase.ShelfMutationOutcome
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.ReviewDocument
import nl.rhaydus.softcover.core.domain.model.ReviewParagraph
import nl.rhaydus.softcover.core.domain.model.ReviewRun
import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.core.uibinding.progress.ProgressTimeDraft
import nl.rhaydus.softcover.core.uibinding.progress.toActionAt
import nl.rhaydus.softcover.core.uibinding.richtext.toRichTextUiModel
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnUpdateTimeProgressClickActionTest {
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

    private fun stubBookWithAudioSeconds(
        audioSeconds: Int?,
        id: Int = 99,
    ): Book =
        mockk<Book>().also { book ->
            val edition = mockk<BookEdition>().also { e ->
                every {
                    e.audioSeconds
                } returns audioSeconds
            }

            every {
                book.id
            } returns id
            every {
                book.currentEdition
            } returns edition
            every {
                book.userBook
            } returns null
        }

    @Nested
    inner class Execute {
        @Test
        fun `discards the time draft once the entered time is submitted`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(
                bookToUpdate = stubBookWithAudioSeconds(audioSeconds = 7200),
                progressTimeDraft = ProgressTimeDraft(
                    forSeconds = 3725,
                    hours = "1",
                    minutes = "2",
                    seconds = "5",
                ),
            )
            val dependencies = stubDependencies(this)

            val action = OnUpdateTimeProgressClickAction(
                hours = "1",
                minutes = "2",
                seconds = "5",
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.progressTimeDraft shouldBe null
        }

        @Test
        fun `sets showProgressSheet to false after execute`() = runTest {
            // ----- Arrange -----
            val book = stubBookWithAudioSeconds(audioSeconds = 7200)
            stateFlow.value = ReadingScreenUiState(
                bookToUpdate = book,
                showProgressSheet = true,
            )
            val dependencies = stubDependencies(this)
            val action = OnUpdateTimeProgressClickAction(
                hours = "1",
                minutes = "0",
                seconds = "0",
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.showProgressSheet shouldBe false
        }

        @Test
        fun `sets bookToUpdate to null after execute`() = runTest {
            // ----- Arrange -----
            val book = stubBookWithAudioSeconds(audioSeconds = 7200)
            stateFlow.value = ReadingScreenUiState(bookToUpdate = book)
            val dependencies = stubDependencies(this)
            val action = OnUpdateTimeProgressClickAction(
                hours = "0",
                minutes = "30",
                seconds = "0",
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.bookToUpdate shouldBe null
        }

        @Test
        fun `does not invoke updateBookProgress when bookToUpdate is null`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(bookToUpdate = null)
            val dependencies = stubDependencies(this)
            val action = OnUpdateTimeProgressClickAction(
                hours = "1",
                minutes = "0",
                seconds = "0",
            )

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
        }

        @Test
        fun `does not change state when bookToUpdate is null`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(
                bookToUpdate = null,
                showProgressSheet = true,
            )
            val dependencies = stubDependencies(this)
            val action = OnUpdateTimeProgressClickAction(
                hours = "1",
                minutes = "0",
                seconds = "0",
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.showProgressSheet shouldBe true
            stateFlow.value.bookToUpdate shouldBe null
        }

        @Test
        fun `computes total seconds as h times 3600 plus m times 60 plus s`() = runTest {
            // ----- Arrange -----
            val book = stubBookWithAudioSeconds(audioSeconds = 7200)
            stateFlow.value = ReadingScreenUiState(bookToUpdate = book)
            val dependencies = stubDependencies(this)
            // 1*3600 + 2*60 + 3 = 3723
            val action = OnUpdateTimeProgressClickAction(
                hours = "1",
                minutes = "2",
                seconds = "3",
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
                    newSeconds = 3723,
                    actionAt = null,
                )
            }
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
            val book = stubBookWithAudioSeconds(audioSeconds = 7200)
            stateFlow.value = ReadingScreenUiState(
                bookToUpdate = book,
                readAt = readAt,
            )
            val dependencies = stubDependencies(this)

            coEvery {
                updateBookProgress(
                    book = book,
                    newSeconds = any(),
                    actionAt = readAt.toActionAt(),
                )
            } returns Result.success(null)

            val action = OnUpdateTimeProgressClickAction(
                hours = "1",
                minutes = "0",
                seconds = "0",
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
                    newSeconds = 3600,
                    actionAt = readAt.toActionAt(),
                )
            }
        }

        @Test
        fun `treats non-numeric hours as 0`() = runTest {
            // ----- Arrange -----
            val book = stubBookWithAudioSeconds(audioSeconds = 3600)
            stateFlow.value = ReadingScreenUiState(bookToUpdate = book)
            val dependencies = stubDependencies(this)
            // 0*3600 + 0*60 + 30 = 30
            val action = OnUpdateTimeProgressClickAction(
                hours = "abc",
                minutes = "0",
                seconds = "30",
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
                    newSeconds = 30,
                )
            }
        }

        @Test
        fun `treats non-numeric minutes as 0`() = runTest {
            // ----- Arrange -----
            val book = stubBookWithAudioSeconds(audioSeconds = 7200)
            stateFlow.value = ReadingScreenUiState(bookToUpdate = book)
            val dependencies = stubDependencies(this)
            // 1*3600 + 0*60 + 0 = 3600
            val action = OnUpdateTimeProgressClickAction(
                hours = "1",
                minutes = "abc",
                seconds = "0",
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
                    newSeconds = 3600,
                )
            }
        }

        @Test
        fun `treats non-numeric seconds as 0`() = runTest {
            // ----- Arrange -----
            val book = stubBookWithAudioSeconds(audioSeconds = 7200)
            stateFlow.value = ReadingScreenUiState(bookToUpdate = book)
            val dependencies = stubDependencies(this)
            // 1*3600 + 0*60 + 0 = 3600
            val action = OnUpdateTimeProgressClickAction(
                hours = "1",
                minutes = "0",
                seconds = "abc",
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
                    newSeconds = 3600,
                )
            }
        }

        @Test
        fun `treats empty string fields as 0`() = runTest {
            // ----- Arrange -----
            val book = stubBookWithAudioSeconds(audioSeconds = 3600)
            stateFlow.value = ReadingScreenUiState(bookToUpdate = book)
            val dependencies = stubDependencies(this)
            val action = OnUpdateTimeProgressClickAction(
                hours = "",
                minutes = "",
                seconds = "",
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
                    newSeconds = 0,
                )
            }
        }

        @Test
        fun `clamps a negative entered time to zero`() = runTest {
            // ----- Arrange -----
            val book = stubBookWithAudioSeconds(audioSeconds = 3600)
            stateFlow.value = ReadingScreenUiState(bookToUpdate = book)
            val dependencies = stubDependencies(this)
            val action = OnUpdateTimeProgressClickAction(
                hours = "-2",
                minutes = "30",
                seconds = "0",
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
                    newSeconds = 0,
                )
            }
        }

        @Test
        fun `clamps total seconds to audioSeconds of current edition`() = runTest {
            // ----- Arrange -----
            val audioSeconds = 3600
            val book = stubBookWithAudioSeconds(audioSeconds = audioSeconds)
            stateFlow.value = ReadingScreenUiState(bookToUpdate = book)
            val dependencies = stubDependencies(this)
            // 2*3600 + 0 + 0 = 7200, clamped to 3600
            val action = OnUpdateTimeProgressClickAction(
                hours = "2",
                minutes = "0",
                seconds = "0",
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
                    newSeconds = 3600,
                )
            }
        }

        @Test
        fun `records entered seconds unclamped when audioSeconds is null`() = runTest {
            // ----- Arrange -----
            val book = stubBookWithAudioSeconds(audioSeconds = null)
            stateFlow.value = ReadingScreenUiState(bookToUpdate = book)
            val dependencies = stubDependencies(this)
            // total = 0 (null), so entered.coerceAtLeast(0) = 3600
            val action = OnUpdateTimeProgressClickAction(
                hours = "1",
                minutes = "0",
                seconds = "0",
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
                    newSeconds = 3600,
                )
            }
        }

        @Test
        fun `records entered seconds unclamped when audioSeconds is null with 2 hours`() = runTest {
            // ----- Arrange -----
            val book = stubBookWithAudioSeconds(audioSeconds = null)
            stateFlow.value = ReadingScreenUiState(bookToUpdate = book)
            val dependencies = stubDependencies(this)
            // total = 0 (null), so entered.coerceAtLeast(0) = 7200
            val action = OnUpdateTimeProgressClickAction(
                hours = "2",
                minutes = "0",
                seconds = "0",
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
                    newSeconds = 7200,
                )
            }
        }

        @Test
        fun `adds book id to failedMutationBookIds when updateBookProgress fails`() = runTest {
            // ----- Arrange -----
            val book = stubBookWithAudioSeconds(
                audioSeconds = 7200,
                id = 5,
            )
            stateFlow.value = ReadingScreenUiState(bookToUpdate = book)
            val dependencies = stubDependencies(this)

            coEvery {
                updateBookProgress(
                    book = book,
                    newSeconds = any(),
                )
            } returns Result.failure(RuntimeException("network error"))

            val action = OnUpdateTimeProgressClickAction(
                hours = "1",
                minutes = "0",
                seconds = "0",
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.failedMutationBookIds.contains(5) shouldBe true
        }

        @Test
        fun `does not add book id to failedMutationBookIds when updateBookProgress succeeds`() = runTest {
            // ----- Arrange -----
            val book = stubBookWithAudioSeconds(
                audioSeconds = 7200,
                id = 5,
            )
            stateFlow.value = ReadingScreenUiState(bookToUpdate = book)
            val dependencies = stubDependencies(this)

            coEvery {
                updateBookProgress(
                    book = book,
                    newSeconds = any(),
                )
            } returns Result.success(null)

            val action = OnUpdateTimeProgressClickAction(
                hours = "1",
                minutes = "0",
                seconds = "0",
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.failedMutationBookIds.contains(5) shouldBe false
        }

        @Test
        fun `sets verdictPromptBook to bookToUpdate when updateBookProgress outcome is Applied`() = runTest {
            // ----- Arrange -----
            val book = stubBookWithAudioSeconds(
                audioSeconds = 7200,
                id = 5,
            )
            stateFlow.value = ReadingScreenUiState(bookToUpdate = book)
            val dependencies = stubDependencies(this)

            coEvery {
                updateBookProgress(
                    book = book,
                    newSeconds = any(),
                )
            } returns Result.success(ShelfMutationOutcome.Applied)

            val action = OnUpdateTimeProgressClickAction(
                hours = "1",
                minutes = "0",
                seconds = "0",
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.verdictPromptBook shouldBe book
        }

        @Test
        fun `stores job in bookMutationJobs after execute returns`() = runTest {
            // ----- Arrange -----
            val book = stubBookWithAudioSeconds(
                audioSeconds = 7200,
                id = 5,
            )
            stateFlow.value = ReadingScreenUiState(bookToUpdate = book)
            val dependencies = stubDependencies(this)

            coEvery {
                updateBookProgress(
                    book = book,
                    newSeconds = any(),
                )
            } returns Result.success(null)

            val action = OnUpdateTimeProgressClickAction(
                hours = "1",
                minutes = "0",
                seconds = "0",
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            localVariablesFlow.value.bookMutationJobs.containsKey(5) shouldBe true
        }

        @Test
        fun `cancels prior job for same book id and replaces it with a new one`() = runTest {
            // ----- Arrange -----
            val bookId = 5
            val book = stubBookWithAudioSeconds(
                audioSeconds = 7200,
                id = bookId,
            )
            val priorJob = Job()

            localVariablesFlow.value = ReadingLocalVariables(
                bookMutationJobs = mapOf(bookId to priorJob),
            )

            scope = ActionScope(
                stateFlow = stateFlow,
                localVariablesFlow = localVariablesFlow,
                eventChannel = Channel(Channel.BUFFERED),
            )

            stateFlow.value = ReadingScreenUiState(bookToUpdate = book)

            val dependencies = stubDependencies(this)

            coEvery {
                updateBookProgress(
                    book = book,
                    newSeconds = any(),
                )
            } returns Result.success(null)

            val action = OnUpdateTimeProgressClickAction(
                hours = "1",
                minutes = "0",
                seconds = "0",
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            priorJob.isCancelled shouldBe true

            localVariablesFlow.value.bookMutationJobs.containsKey(bookId) shouldBe true
        }
    }
}
