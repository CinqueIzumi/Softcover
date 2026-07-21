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

class OnUpdatePercentageProgressClickActionTest {
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

    private fun stubEdition(pages: Int?): BookEdition = mockk {
        every {
            this@mockk.pages
        } returns pages
        every {
            id
        } returns 1
        every {
            isAudiobook
        } returns false
        every {
            audioSeconds
        } returns null
    }

    private fun stubAudiobookEdition(audioSeconds: Int?): BookEdition = mockk {
        every {
            this@mockk.audioSeconds
        } returns audioSeconds
        every {
            isAudiobook
        } returns true
    }

    private fun stubBook(
        currentEditionPages: Int?,
        defaultEditionPages: Int? = null,
        id: Int = 42,
    ): Book = mockk {
        val edition = stubEdition(currentEditionPages)
        val defaultEdition = defaultEditionPages?.let { stubEdition(it) }

        every {
            currentEdition
        } returns edition
        every {
            this@mockk.defaultEdition
        } returns defaultEdition
        every {
            this@mockk.id
        } returns id
    }

    private fun stubAudiobook(
        audioSeconds: Int?,
        id: Int = 42,
    ): Book = mockk {
        val edition = stubAudiobookEdition(audioSeconds = audioSeconds)
        every {
            currentEdition
        } returns edition
        every {
            defaultEdition
        } returns null
        every {
            this@mockk.id
        } returns id
    }

    @Nested
    inner class Execute {
        @Test
        fun `hides the update progress sheet after execution`() = runTest {
            // ----- Arrange -----
            val book = stubBook(currentEditionPages = 300)
            stateFlow.value = BookDetailUiState(
                book = book,
                showUpdateProgressSheet = true,
            )
            dependencies = stubDependencies(this)

            val action = OnUpdatePercentageProgressClickAction(newPercentage = "50")

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

            val action = OnUpdatePercentageProgressClickAction(newPercentage = "50")

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
        fun `invokes updateBookProgress with page derived from currentEdition page count`() = runTest {
            // ----- Arrange -----
            val book = stubBook(currentEditionPages = 200)
            stateFlow.value = BookDetailUiState(book = book)
            dependencies = stubDependencies(this)

            // 50% of 200 pages = 100
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
                )
            }
        }

        @Test
        fun `falls back to defaultEdition pages when currentEdition has no page count`() = runTest {
            // ----- Arrange -----
            val book = stubBook(
                currentEditionPages = null,
                defaultEditionPages = 400,
            )
            stateFlow.value = BookDetailUiState(book = book)
            dependencies = stubDependencies(this)

            // 25% of 400 pages = 100
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
                )
            }
        }

        @Test
        fun `uses zero pages when both currentEdition and defaultEdition have no page count`() = runTest {
            // ----- Arrange -----
            val book = stubBook(
                currentEditionPages = null,
                defaultEditionPages = null,
            )
            stateFlow.value = BookDetailUiState(book = book)
            dependencies = stubDependencies(this)

            val action = OnUpdatePercentageProgressClickAction(newPercentage = "75")

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
        fun `treats a non-numeric percentage string as zero`() = runTest {
            // ----- Arrange -----
            val book = stubBook(currentEditionPages = 300)
            stateFlow.value = BookDetailUiState(book = book)
            dependencies = stubDependencies(this)

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
                )
            }
        }

        @Test
        fun `treats an empty percentage string as zero`() = runTest {
            // ----- Arrange -----
            val book = stubBook(currentEditionPages = 300)
            stateFlow.value = BookDetailUiState(book = book)
            dependencies = stubDependencies(this)

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
                )
            }
        }

        @Test
        fun `rounds down fractional page results to an integer`() = runTest {
            // ----- Arrange -----
            // 10% of 333 = 33.3 -> truncated to 33
            val book = stubBook(currentEditionPages = 333)
            stateFlow.value = BookDetailUiState(book = book)
            dependencies = stubDependencies(this)

            val action = OnUpdatePercentageProgressClickAction(newPercentage = "10")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                updateBookProgress(
                    book = book,
                    newPage = 33,
                )
            }
        }

        @Test
        fun `clamps negative percentage to zero pages`() = runTest {
            // ----- Arrange -----
            val book = stubBook(currentEditionPages = 300)
            stateFlow.value = BookDetailUiState(book = book)
            dependencies = stubDependencies(this)

            val action = OnUpdatePercentageProgressClickAction(newPercentage = "-10")

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
        fun `clamps percentage greater than 100 to total pages`() = runTest {
            // ----- Arrange -----
            val book = stubBook(currentEditionPages = 200)
            stateFlow.value = BookDetailUiState(book = book)
            dependencies = stubDependencies(this)

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
                    newPage = 200,
                    newSeconds = null,
                )
            }
        }

        @Test
        fun `computes newSeconds from percentage and audioSeconds for an audiobook edition`() = runTest {
            // ----- Arrange -----
            val book = stubAudiobook(audioSeconds = 3600)
            stateFlow.value = BookDetailUiState(book = book)
            dependencies = stubDependencies(this)

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
                    newSeconds = 1800,
                )
            }
        }

        @Test
        fun `passes newPage as null when edition is audiobook`() = runTest {
            // ----- Arrange -----
            val book = stubAudiobook(audioSeconds = 3600)
            stateFlow.value = BookDetailUiState(book = book)
            dependencies = stubDependencies(this)

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
            stateFlow.value = BookDetailUiState(book = book)
            dependencies = stubDependencies(this)

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
                    newSeconds = 3600,
                )
            }
        }

        @Test
        fun `clamps percentage below 0 to 0 when edition is audiobook`() = runTest {
            // ----- Arrange -----
            val book = stubAudiobook(audioSeconds = 3600)
            stateFlow.value = BookDetailUiState(book = book)
            dependencies = stubDependencies(this)

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
                    newSeconds = 0,
                )
            }
        }

        @Test
        fun `uses 0 seconds when audioSeconds is null and edition is audiobook`() = runTest {
            // ----- Arrange -----
            val book = stubAudiobook(audioSeconds = null)
            stateFlow.value = BookDetailUiState(book = book)
            dependencies = stubDependencies(this)

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
                    newSeconds = 0,
                )
            }
        }

        @Test
        fun `hides the update progress sheet for audiobook edition`() = runTest {
            // ----- Arrange -----
            val book = stubAudiobook(audioSeconds = 3600)
            stateFlow.value = BookDetailUiState(
                book = book,
                showUpdateProgressSheet = true,
            )
            dependencies = stubDependencies(this)

            val action = OnUpdatePercentageProgressClickAction(newPercentage = "50")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.showUpdateProgressSheet shouldBe false
        }

        @Test
        fun `sends BookMarkedAsReadEvent when use case returns Applied`() = runTest {
            // ----- Arrange -----
            val book = stubBook(currentEditionPages = 300)
            stateFlow.value = BookDetailUiState(book = book)
            dependencies = stubDependencies(this)

            coEvery {
                updateBookProgress(
                    book = any(),
                    newPage = any(),
                    newSeconds = any(),
                )
            } returns Result.success(ShelfMutationOutcome.Applied)

            val action = OnUpdatePercentageProgressClickAction(newPercentage = "100")

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
            val book = stubBook(
                currentEditionPages = 300,
                id = 42,
            )
            stateFlow.value = BookDetailUiState(book = book)
            dependencies = stubDependencies(this)

            coEvery {
                updateBookProgress(
                    book = any(),
                    newPage = any(),
                    newSeconds = any(),
                )
            } returns Result.failure(RuntimeException("api error"))

            val action = OnUpdatePercentageProgressClickAction(newPercentage = "100")

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
