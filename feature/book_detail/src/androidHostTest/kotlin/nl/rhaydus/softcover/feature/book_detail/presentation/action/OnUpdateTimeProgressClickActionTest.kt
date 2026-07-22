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
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookMarkedAsReadEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

class OnUpdateTimeProgressClickActionTest {
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

    private fun stubBookWithAudioSeconds(
        audioSeconds: Int?,
        id: Int = 42,
    ): Book = mockk<Book>().also { book ->
        val edition = mockk<BookEdition>().also { e ->
            every {
                e.audioSeconds
            } returns audioSeconds
        }
        every {
            book.currentEdition
        } returns edition
        every {
            book.id
        } returns id
    }

    @Nested
    inner class Execute {
        @Test
        fun `hides the update progress sheet after execution`() = runTest {
            // ----- Arrange -----
            val book = stubBookWithAudioSeconds(audioSeconds = 7200)
            stateFlow.value = BookDetailUiState(
                book = book,
                showUpdateProgressSheet = true,
            )
            dependencies = stubDependencies(this)

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
            stateFlow.value.showUpdateProgressSheet shouldBe false
        }

        @Test
        fun `does nothing when book in state is null`() = runTest {
            // ----- Arrange -----
            stateFlow.value = BookDetailUiState(
                book = null,
                showUpdateProgressSheet = true,
            )
            dependencies = stubDependencies(this)

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
            stateFlow.value.showUpdateProgressSheet shouldBe true
            coVerify(exactly = 0) { updateBookProgress(
                any(),
                any(),
                any(),
            ) }
        }

        @Test
        fun `computes total seconds as h times 3600 plus m times 60 plus s`() = runTest {
            // ----- Arrange -----
            val book = stubBookWithAudioSeconds(audioSeconds = 7200)
            stateFlow.value = BookDetailUiState(book = book)
            dependencies = stubDependencies(this)

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
                )
            }
        }

        @Test
        fun `invokes updateBookProgress with the provided actionAt`() = runTest {
            // ----- Arrange -----
            val book = stubBookWithAudioSeconds(audioSeconds = 7200)
            stateFlow.value = BookDetailUiState(book = book)
            dependencies = stubDependencies(this)

            coEvery {
                updateBookProgress(
                    book = any(),
                    newSeconds = any(),
                    actionAt = any(),
                )
            } returns Result.success(null)

            val action = OnUpdateTimeProgressClickAction(
                hours = "1",
                minutes = "0",
                seconds = "0",
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
                    newSeconds = 3600,
                    actionAt = "2026-07-21T21:00:00Z",
                )
            }
        }

        @Test
        fun `treats non-numeric hours as 0`() = runTest {
            // ----- Arrange -----
            val book = stubBookWithAudioSeconds(audioSeconds = 3600)
            stateFlow.value = BookDetailUiState(book = book)
            dependencies = stubDependencies(this)

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
            stateFlow.value = BookDetailUiState(book = book)
            dependencies = stubDependencies(this)

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
            stateFlow.value = BookDetailUiState(book = book)
            dependencies = stubDependencies(this)

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
            stateFlow.value = BookDetailUiState(book = book)
            dependencies = stubDependencies(this)

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
        fun `coerces minutes greater than 59 to 59`() = runTest {
            // ----- Arrange -----
            val book = stubBookWithAudioSeconds(audioSeconds = 7200)
            stateFlow.value = BookDetailUiState(book = book)
            dependencies = stubDependencies(this)

            // 0*3600 + 59*60 + 0 = 3540
            val action = OnUpdateTimeProgressClickAction(
                hours = "0",
                minutes = "90",
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
                    newSeconds = 3540,
                )
            }
        }

        @Test
        fun `coerces seconds greater than 59 to 59`() = runTest {
            // ----- Arrange -----
            val book = stubBookWithAudioSeconds(audioSeconds = 7200)
            stateFlow.value = BookDetailUiState(book = book)
            dependencies = stubDependencies(this)

            // 0*3600 + 0*60 + 59 = 59
            val action = OnUpdateTimeProgressClickAction(
                hours = "0",
                minutes = "0",
                seconds = "90",
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
                    newSeconds = 59,
                )
            }
        }

        @Test
        fun `treats negative hours as 0 via coerceAtLeast`() = runTest {
            // ----- Arrange -----
            val book = stubBookWithAudioSeconds(audioSeconds = 3600)
            stateFlow.value = BookDetailUiState(book = book)
            dependencies = stubDependencies(this)

            // 0*3600 + 30*60 + 0 = 1800
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
                    newSeconds = 1800,
                )
            }
        }

        @Test
        fun `clamps total seconds to audioSeconds of current edition`() = runTest {
            // ----- Arrange -----
            val book = stubBookWithAudioSeconds(audioSeconds = 3600)
            stateFlow.value = BookDetailUiState(book = book)
            dependencies = stubDependencies(this)

            // 2*3600 = 7200, clamped to 3600
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
        fun `passes entered seconds through unchanged when audioSeconds is null — 1h maps to 3600`() = runTest {
            // ----- Arrange -----
            val book = stubBookWithAudioSeconds(audioSeconds = null)
            stateFlow.value = BookDetailUiState(book = book)
            dependencies = stubDependencies(this)

            // total is unknown; free-entry path — coerceAtLeast(0) keeps 3600 as-is
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
        fun `passes entered seconds through unchanged when audioSeconds is null — 2h maps to 7200`() = runTest {
            // ----- Arrange -----
            val book = stubBookWithAudioSeconds(audioSeconds = null)
            stateFlow.value = BookDetailUiState(book = book)
            dependencies = stubDependencies(this)

            // total is unknown; free-entry path — coerceAtLeast(0) keeps 7200 as-is
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
        fun `coerces minutes below 0 to 0 via coerceIn`() = runTest {
            // ----- Arrange -----
            val book = stubBookWithAudioSeconds(audioSeconds = 3600)
            stateFlow.value = BookDetailUiState(book = book)
            dependencies = stubDependencies(this)

            // 1*3600 + 0*60 + 0 = 3600
            val action = OnUpdateTimeProgressClickAction(
                hours = "1",
                minutes = "-10",
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
        fun `coerces seconds below 0 to 0 via coerceIn`() = runTest {
            // ----- Arrange -----
            val book = stubBookWithAudioSeconds(audioSeconds = 3600)
            stateFlow.value = BookDetailUiState(book = book)
            dependencies = stubDependencies(this)

            // 1*3600 + 0 + 0 = 3600
            val action = OnUpdateTimeProgressClickAction(
                hours = "1",
                minutes = "0",
                seconds = "-30",
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
        fun `sends BookMarkedAsReadEvent when use case returns Applied`() = runTest {
            // ----- Arrange -----
            val book = stubBookWithAudioSeconds(audioSeconds = 3600)
            stateFlow.value = BookDetailUiState(book = book)
            dependencies = stubDependencies(this)

            coEvery {
                updateBookProgress(
                    book = any(),
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
            val event = eventChannel.tryReceive().getOrNull()
            event.shouldBeInstanceOf<BookMarkedAsReadEvent>()
        }

        @Test
        fun `adds book id to failedMutationBookIds when use case fails`() = runTest {
            // ----- Arrange -----
            val book = stubBookWithAudioSeconds(
                audioSeconds = 3600,
                id = 42,
            )
            stateFlow.value = BookDetailUiState(book = book)
            dependencies = stubDependencies(this)

            coEvery {
                updateBookProgress(
                    book = any(),
                    newSeconds = any(),
                )
            } returns Result.failure(RuntimeException("api error"))

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
            stateFlow.value.failedMutationBookIds.contains(42) shouldBe true
        }
    }
}
