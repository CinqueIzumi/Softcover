package nl.rhaydus.softcover.feature.book_detail.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.reading.presentation.util.UpdateBookProgress
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnUpdatePageProgressClickActionTest {

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

    private fun stubBook(): Book = mockk()

    @Nested
    inner class Execute {

        @Test
        fun `hides the update progress sheet after execution`() = runTest {
            // ----- Arrange -----
            val book = stubBook()
            stateFlow.value = BookDetailUiState(book = book, showUpdateProgressSheet = true)
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
                updateBookProgress(book = book, newPage = 250)
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
                updateBookProgress(book = book, newPage = 0)
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
                updateBookProgress(book = book, newPage = 0)
            }
        }

        @Test
        fun `does nothing when book in state is null`() = runTest {
            // ----- Arrange -----
            stateFlow.value = BookDetailUiState(book = null, showUpdateProgressSheet = true)
            dependencies = stubDependencies(this)

            val action = OnUpdatePageProgressClickAction(newPage = "50")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.showUpdateProgressSheet shouldBe true
            coVerify(exactly = 0) { updateBookProgress(any(), any(), any()) }
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
                updateBookProgress(book = book, newPage = 0)
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
                updateBookProgress(book = book, newPage = -5)
            }
        }
    }
}
