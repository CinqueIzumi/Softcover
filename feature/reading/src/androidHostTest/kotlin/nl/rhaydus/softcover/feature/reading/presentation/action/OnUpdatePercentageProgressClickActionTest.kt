package nl.rhaydus.softcover.feature.reading.presentation.action

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
import nl.rhaydus.softcover.core.book.domain.usecase.RecordBookProgressUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.ShelfMutationOutcome
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.toad.ActionScope

class OnUpdatePercentageProgressClickActionTest {
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
        }

    @Nested
    inner class Execute {
        @Test
        fun `sets showProgressSheet to false after execute`() = runTest {
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
        }

        @Test
        fun `sets bookToUpdate to null after execute`() = runTest {
            // ----- Arrange -----
            val book = stubBookWithCurrentEditionPages(pages = 200)
            stateFlow.value = ReadingScreenUiState(bookToUpdate = book)
            val dependencies = stubDependencies(this)
            val action = OnUpdatePercentageProgressClickAction(newPercentage = "25")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.bookToUpdate shouldBe null
        }

        @Test
        fun `computes page from percentage and currentEdition pages`() = runTest {
            // ----- Arrange -----
            val book = stubBookWithCurrentEditionPages(pages = 200)
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
                    newPage = 100,
                    newSeconds = null,
                    actionAt = null,
                )
            }
        }

        @Test
        fun `passes actionAt through to updateBookProgress when provided`() = runTest {
            // ----- Arrange -----
            val book = stubBookWithCurrentEditionPages(pages = 200)
            stateFlow.value = ReadingScreenUiState(bookToUpdate = book)
            val dependencies = stubDependencies(this)

            coEvery {
                updateBookProgress(
                    book = book,
                    newPage = any(),
                    newSeconds = any(),
                    actionAt = "2026-07-21T21:00:00Z",
                )
            } returns Result.success(null)

            val action = OnUpdatePercentageProgressClickAction(
                newPercentage = "50",
                actionAt = "2026-07-21T21:00:00Z",
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
                    actionAt = "2026-07-21T21:00:00Z",
                )
            }
        }

        @Test
        fun `falls back to 0 pages when currentEdition pages is null and no defaultEdition`() = runTest {
            // ----- Arrange -----
            val book = stubBookWithCurrentEditionPages(pages = null)
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
        fun `uses 0 percentage when newPercentage is an empty string`() = runTest {
            // ----- Arrange -----
            val book = stubBookWithCurrentEditionPages(pages = 300)
            stateFlow.value = ReadingScreenUiState(bookToUpdate = book)
            val dependencies = stubDependencies(this)
            val action = OnUpdatePercentageProgressClickAction(newPercentage = "")

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
        fun `does not invoke updateBookProgress when bookToUpdate is null`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(bookToUpdate = null)
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
        }

        @Test
        fun `does not change state when bookToUpdate is null`() = runTest {
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
            stateFlow.value.showProgressSheet shouldBe true
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
        fun `passes newPage as null when edition is audiobook`() = runTest {
            // ----- Arrange -----
            val book = stubAudiobook(audioSeconds = 3600)
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
                    newPage = null,
                    newSeconds = 900,
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
        fun `adds book id to failedMutationBookIds when updateBookProgress fails`() = runTest {
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
        fun `does not add book id to failedMutationBookIds when updateBookProgress succeeds`() = runTest {
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
            } returns Result.success(null)

            val action = OnUpdatePercentageProgressClickAction(newPercentage = "50")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.failedMutationBookIds.contains(3) shouldBe false
        }

        @Test
        fun `sets verdictPromptBook to bookToUpdate when updateBookProgress outcome is Applied`() = runTest {
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
        fun `stores job in bookMutationJobs after execute returns`() = runTest {
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
            } returns Result.success(null)

            val action = OnUpdatePercentageProgressClickAction(newPercentage = "50")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            localVariablesFlow.value.bookMutationJobs.containsKey(3) shouldBe true
        }

        @Test
        fun `cancels prior job for same book id and replaces it with a new one`() = runTest {
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
                    newPage = any(),
                    newSeconds = any(),
                )
            } returns Result.success(null)

            val action = OnUpdatePercentageProgressClickAction(newPercentage = "50")

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
