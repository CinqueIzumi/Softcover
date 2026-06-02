package nl.rhaydus.softcover.feature.book_detail.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.deadlines.domain.usecase.SetBookDeadlineUseCase
import nl.rhaydus.softcover.core.designsystem.presentation.toad.ActionScope
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.DeadlineUnit
import nl.rhaydus.softcover.core.domain.model.UserBookRead
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate

class OnDeadlinePickedActionTest {

    private lateinit var setBookDeadlineUseCase: SetBookDeadlineUseCase
    private lateinit var dependencies: BookDetailDependencies
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        setBookDeadlineUseCase = mockk()
        stateFlow = MutableStateFlow(BookDetailUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(BookDetailLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    private fun stubDependencies(testScope: kotlinx.coroutines.test.TestScope): BookDetailDependencies {
        val dispatcher = UnconfinedTestDispatcher(testScope.testScheduler)
        return mockk<BookDetailDependencies>(relaxed = true).also { mock ->
            every {
                mock.setBookDeadlineUseCase
            } returns setBookDeadlineUseCase

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
        pages: Int? = 300,
        currentPage: Int? = 50,
        audioSeconds: Int? = null,
        currentSeconds: Int? = null,
    ): Book {
        val isAudiobook = (audioSeconds ?: 0) > 0
        val edition = mockk<BookEdition> {
            every {
                this@mockk.pages
            } returns pages

            every {
                this@mockk.audioSeconds
            } returns audioSeconds

            every {
                this@mockk.isAudiobook
            } returns isAudiobook
        }
        val userBookRead = if (currentPage != null || currentSeconds != null) {
            mockk<UserBookRead> {
                every {
                    this@mockk.currentPage
                } returns currentPage

                every {
                    this@mockk.currentSeconds
                } returns currentSeconds
            }
        } else {
            null
        }
        return mockk {
            every {
                this@mockk.id
            } returns id

            every {
                this@mockk.currentEdition
            } returns edition

            every {
                this@mockk.userBookRead
            } returns userBookRead
        }
    }

    @Nested
    inner class Execute {

        @Test
        fun `calls setBookDeadlineUseCase with bookId total current and PAGES unit for regular edition`() = runTest {
            // ----- Arrange -----
            val book = stubBook(id = 7, pages = 250, currentPage = 75)
            stateFlow.value = BookDetailUiState(book = book)
            dependencies = stubDependencies(this)
            val date = LocalDate.of(2026, 6, 1)

            coEvery {
                setBookDeadlineUseCase(
                    bookId = 7,
                    deadlineDate = date,
                    current = 75,
                    total = 250,
                    unit = DeadlineUnit.PAGES,
                )
            } returns Result.success(Unit)

            val action = OnDeadlinePickedAction(date = date)

            // ----- Act -----
            action.execute(dependencies = dependencies, scope = scope)

            // ----- Assert -----
            coVerify {
                setBookDeadlineUseCase(
                    bookId = 7,
                    deadlineDate = date,
                    current = 75,
                    total = 250,
                    unit = DeadlineUnit.PAGES,
                )
            }
        }

        @Test
        fun `uses zero for total when currentEdition pages is null for regular edition`() = runTest {
            // ----- Arrange -----
            val book = stubBook(id = 1, pages = null, currentPage = 10)
            stateFlow.value = BookDetailUiState(book = book)
            dependencies = stubDependencies(this)
            val date = LocalDate.of(2026, 6, 1)

            coEvery {
                setBookDeadlineUseCase(
                    bookId = any(),
                    deadlineDate = any(),
                    current = any(),
                    total = 0,
                    unit = DeadlineUnit.PAGES,
                )
            } returns Result.success(Unit)

            val action = OnDeadlinePickedAction(date = date)

            // ----- Act -----
            action.execute(dependencies = dependencies, scope = scope)

            // ----- Assert -----
            coVerify {
                setBookDeadlineUseCase(
                    bookId = any(),
                    deadlineDate = any(),
                    current = any(),
                    total = 0,
                    unit = DeadlineUnit.PAGES,
                )
            }
        }

        @Test
        fun `uses zero for current when userBookRead is null for regular edition`() = runTest {
            // ----- Arrange -----
            val book = stubBook(id = 3, pages = 200, currentPage = null, currentSeconds = null)
            stateFlow.value = BookDetailUiState(book = book)
            dependencies = stubDependencies(this)
            val date = LocalDate.of(2026, 6, 1)

            coEvery {
                setBookDeadlineUseCase(
                    bookId = any(),
                    deadlineDate = any(),
                    current = 0,
                    total = any(),
                    unit = DeadlineUnit.PAGES,
                )
            } returns Result.success(Unit)

            val action = OnDeadlinePickedAction(date = date)

            // ----- Act -----
            action.execute(dependencies = dependencies, scope = scope)

            // ----- Assert -----
            coVerify {
                setBookDeadlineUseCase(
                    bookId = any(),
                    deadlineDate = any(),
                    current = 0,
                    total = any(),
                    unit = DeadlineUnit.PAGES,
                )
            }
        }

        @Test
        fun `sets showDeadlinePicker to false after calling use case`() = runTest {
            // ----- Arrange -----
            val book = stubBook(id = 5, pages = 100, currentPage = 0)
            stateFlow.value = BookDetailUiState(book = book, showDeadlinePicker = true)
            dependencies = stubDependencies(this)
            val date = LocalDate.of(2026, 7, 1)

            coEvery {
                setBookDeadlineUseCase(
                    bookId = any(),
                    deadlineDate = any(),
                    current = any(),
                    total = any(),
                    unit = any(),
                )
            } returns Result.success(Unit)

            val action = OnDeadlinePickedAction(date = date)

            // ----- Act -----
            action.execute(dependencies = dependencies, scope = scope)

            // ----- Assert -----
            stateFlow.value.showDeadlinePicker shouldBe false
        }

        @Test
        fun `does nothing when state has no book`() = runTest {
            // ----- Arrange -----
            stateFlow.value = BookDetailUiState(book = null, showDeadlinePicker = true)
            dependencies = stubDependencies(this)
            val date = LocalDate.of(2026, 7, 1)
            val action = OnDeadlinePickedAction(date = date)

            // ----- Act -----
            action.execute(dependencies = dependencies, scope = scope)

            // ----- Assert -----
            stateFlow.value.showDeadlinePicker shouldBe true
            coVerify(exactly = 0) {
                setBookDeadlineUseCase(
                    bookId = any(),
                    deadlineDate = any(),
                    current = any(),
                    total = any(),
                    unit = any(),
                )
            }
        }

        @Test
        fun `calls setBookDeadlineUseCase with currentSeconds and audioSeconds and SECONDS unit for audiobook edition`() = runTest {
            // ----- Arrange -----
            val book = stubBook(id = 8, audioSeconds = 18000, currentSeconds = 3600)
            stateFlow.value = BookDetailUiState(book = book)
            dependencies = stubDependencies(this)
            val date = LocalDate.of(2026, 6, 1)

            coEvery {
                setBookDeadlineUseCase(
                    bookId = 8,
                    deadlineDate = date,
                    current = 3600,
                    total = 18000,
                    unit = DeadlineUnit.SECONDS,
                )
            } returns Result.success(Unit)

            val action = OnDeadlinePickedAction(date = date)

            // ----- Act -----
            action.execute(dependencies = dependencies, scope = scope)

            // ----- Assert -----
            coVerify {
                setBookDeadlineUseCase(
                    bookId = 8,
                    deadlineDate = date,
                    current = 3600,
                    total = 18000,
                    unit = DeadlineUnit.SECONDS,
                )
            }
        }

        @Test
        fun `uses zero for total when audioSeconds is null for audiobook edition`() = runTest {
            // ----- Arrange -----
            // audioSeconds=null but isAudiobook would be false in this case; simulate by providing
            // an edition with audioSeconds=null and explicitly forcing isAudiobook true would be
            // contradictory — instead test the null-safety for audioSeconds when audioSeconds is 0
            // (not an audiobook). Here we test the null guard: audioSeconds null → total = 0.
            // We stub isAudiobook=true explicitly to isolate the null-safety path.
            val edition = mockk<BookEdition> {
                every { this@mockk.pages } returns null
                every { this@mockk.audioSeconds } returns null
                every { this@mockk.isAudiobook } returns true
            }
            val userBookRead = mockk<UserBookRead> {
                every { this@mockk.currentPage } returns null
                every { this@mockk.currentSeconds } returns 0
            }
            val book = mockk<Book> {
                every { this@mockk.id } returns 9
                every { this@mockk.currentEdition } returns edition
                every { this@mockk.userBookRead } returns userBookRead
            }
            stateFlow.value = BookDetailUiState(book = book)
            dependencies = stubDependencies(this)
            val date = LocalDate.of(2026, 6, 1)

            coEvery {
                setBookDeadlineUseCase(
                    bookId = any(),
                    deadlineDate = any(),
                    current = any(),
                    total = 0,
                    unit = DeadlineUnit.SECONDS,
                )
            } returns Result.success(Unit)

            val action = OnDeadlinePickedAction(date = date)

            // ----- Act -----
            action.execute(dependencies = dependencies, scope = scope)

            // ----- Assert -----
            coVerify {
                setBookDeadlineUseCase(
                    bookId = any(),
                    deadlineDate = any(),
                    current = any(),
                    total = 0,
                    unit = DeadlineUnit.SECONDS,
                )
            }
        }

        @Test
        fun `uses zero for current when userBookRead is null for audiobook edition`() = runTest {
            // ----- Arrange -----
            val edition = mockk<BookEdition> {
                every { this@mockk.pages } returns null
                every { this@mockk.audioSeconds } returns 18000
                every { this@mockk.isAudiobook } returns true
            }
            val book = mockk<Book> {
                every { this@mockk.id } returns 10
                every { this@mockk.currentEdition } returns edition
                every { this@mockk.userBookRead } returns null
            }
            stateFlow.value = BookDetailUiState(book = book)
            dependencies = stubDependencies(this)
            val date = LocalDate.of(2026, 6, 1)

            coEvery {
                setBookDeadlineUseCase(
                    bookId = any(),
                    deadlineDate = any(),
                    current = 0,
                    total = any(),
                    unit = DeadlineUnit.SECONDS,
                )
            } returns Result.success(Unit)

            val action = OnDeadlinePickedAction(date = date)

            // ----- Act -----
            action.execute(dependencies = dependencies, scope = scope)

            // ----- Assert -----
            coVerify {
                setBookDeadlineUseCase(
                    bookId = any(),
                    deadlineDate = any(),
                    current = 0,
                    total = any(),
                    unit = DeadlineUnit.SECONDS,
                )
            }
        }
    }
}
