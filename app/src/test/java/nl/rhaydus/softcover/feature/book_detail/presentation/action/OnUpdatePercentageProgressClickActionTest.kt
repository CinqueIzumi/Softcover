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
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.reading.presentation.util.UpdateBookProgress
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnUpdatePercentageProgressClickActionTest {

    private lateinit var updateBookProgress: UpdateBookProgress
    private lateinit var dependencies: BookDetailDependencies
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        updateBookProgress = mockk(relaxed = true)
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
                mock.updateBookProgress
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
        every { this@mockk.pages } returns pages
        every { id } returns 1
    }

    private fun stubBook(currentEditionPages: Int?, defaultEditionPages: Int? = null): Book = mockk {
        val edition = stubEdition(currentEditionPages)
        val defaultEdition = defaultEditionPages?.let { stubEdition(it) }

        every { currentEdition } returns edition
        every { this@mockk.defaultEdition } returns defaultEdition
    }

    @Nested
    inner class Execute {

        @Test
        fun `hides the update progress sheet after execution`() = runTest {
            // ----- Arrange -----
            val book = stubBook(currentEditionPages = 300)
            stateFlow.value = BookDetailUiState(book = book, showUpdateProgressSheet = true)
            dependencies = stubDependencies(this)

            val action = OnUpdatePercentageProgressClickAction(newPercentage = "50")

            // ----- Act -----
            action.execute(dependencies = dependencies, scope = scope)

            // ----- Assert -----
            stateFlow.value.showUpdateProgressSheet shouldBe false
        }

        @Test
        fun `does nothing when book in state is null`() = runTest {
            // ----- Arrange -----
            stateFlow.value = BookDetailUiState(book = null, showUpdateProgressSheet = true)
            dependencies = stubDependencies(this)

            val action = OnUpdatePercentageProgressClickAction(newPercentage = "50")

            // ----- Act -----
            action.execute(dependencies = dependencies, scope = scope)

            // ----- Assert -----
            stateFlow.value.showUpdateProgressSheet shouldBe true
            coVerify(exactly = 0) { updateBookProgress(any(), any(), any()) }
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
            action.execute(dependencies = dependencies, scope = scope)

            // ----- Assert -----
            coVerify {
                updateBookProgress(book = book, newPage = 100, setLoading = any())
            }
        }

        @Test
        fun `falls back to defaultEdition pages when currentEdition has no page count`() = runTest {
            // ----- Arrange -----
            val book = stubBook(currentEditionPages = null, defaultEditionPages = 400)
            stateFlow.value = BookDetailUiState(book = book)
            dependencies = stubDependencies(this)

            // 25% of 400 pages = 100
            val action = OnUpdatePercentageProgressClickAction(newPercentage = "25")

            // ----- Act -----
            action.execute(dependencies = dependencies, scope = scope)

            // ----- Assert -----
            coVerify {
                updateBookProgress(book = book, newPage = 100, setLoading = any())
            }
        }

        @Test
        fun `uses zero pages when both currentEdition and defaultEdition have no page count`() = runTest {
            // ----- Arrange -----
            val book = stubBook(currentEditionPages = null, defaultEditionPages = null)
            stateFlow.value = BookDetailUiState(book = book)
            dependencies = stubDependencies(this)

            val action = OnUpdatePercentageProgressClickAction(newPercentage = "75")

            // ----- Act -----
            action.execute(dependencies = dependencies, scope = scope)

            // ----- Assert -----
            coVerify {
                updateBookProgress(book = book, newPage = 0, setLoading = any())
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
            action.execute(dependencies = dependencies, scope = scope)

            // ----- Assert -----
            coVerify {
                updateBookProgress(book = book, newPage = 0, setLoading = any())
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
            action.execute(dependencies = dependencies, scope = scope)

            // ----- Assert -----
            coVerify {
                updateBookProgress(book = book, newPage = 0, setLoading = any())
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
            action.execute(dependencies = dependencies, scope = scope)

            // ----- Assert -----
            coVerify {
                updateBookProgress(book = book, newPage = 33, setLoading = any())
            }
        }

        @Test
        fun `passes a setLoading lambda that toggles loadingBookDetails state`() = runTest {
            // ----- Arrange -----
            val book = stubBook(currentEditionPages = 100)
            stateFlow.value = BookDetailUiState(book = book, loadingBookDetails = false)
            dependencies = stubDependencies(this)

            val action = OnUpdatePercentageProgressClickAction(newPercentage = "50")

            // ----- Act -----
            action.execute(dependencies = dependencies, scope = scope)

            // ----- Assert -----
            // After UpdateBookProgress completes (relaxed mock), loadingBookDetails stays false
            stateFlow.value.loadingBookDetails shouldBe false
        }

        @Test
        fun `propagates negative page count when percentage is negative — no low-end clamping`() = runTest {
            // ----- Arrange -----
            // -10% of 300 pages = -30; no clamping on the low end
            val book = stubBook(currentEditionPages = 300)
            stateFlow.value = BookDetailUiState(book = book)
            dependencies = stubDependencies(this)

            val action = OnUpdatePercentageProgressClickAction(newPercentage = "-10")

            // ----- Act -----
            action.execute(dependencies = dependencies, scope = scope)

            // ----- Assert -----
            coVerify {
                updateBookProgress(book = book, newPage = -30, setLoading = any())
            }
        }

        @Test
        fun `propagates page count greater than total pages when percentage exceeds 100 — no high-end clamping`() = runTest {
            // ----- Arrange -----
            // 150% of 200 pages = 300, which is greater than the book's total page count
            val book = stubBook(currentEditionPages = 200)
            stateFlow.value = BookDetailUiState(book = book)
            dependencies = stubDependencies(this)

            val action = OnUpdatePercentageProgressClickAction(newPercentage = "150")

            // ----- Act -----
            action.execute(dependencies = dependencies, scope = scope)

            // ----- Assert -----
            coVerify {
                updateBookProgress(book = book, newPage = 300, setLoading = any())
            }
        }

        @Test
        fun `setLoading lambda passed to updateBookProgress sets loadingBookDetails true then false`() = runTest {
            // ----- Arrange -----
            val book = stubBook(currentEditionPages = 100)
            stateFlow.value = BookDetailUiState(book = book, loadingBookDetails = false)
            dependencies = stubDependencies(this)

            val loadingStates = mutableListOf<Boolean>()

            coEvery {
                updateBookProgress(book = book, newPage = 50, setLoading = any())
            } coAnswers {
                val setLoading = thirdArg<(Boolean) -> Unit>()
                setLoading(true)
                loadingStates.add(stateFlow.value.loadingBookDetails)
                setLoading(false)
                loadingStates.add(stateFlow.value.loadingBookDetails)
            }

            val action = OnUpdatePercentageProgressClickAction(newPercentage = "50")

            // ----- Act -----
            action.execute(dependencies = dependencies, scope = scope)

            // ----- Assert -----
            loadingStates shouldBe listOf(true, false)
        }
    }
}
