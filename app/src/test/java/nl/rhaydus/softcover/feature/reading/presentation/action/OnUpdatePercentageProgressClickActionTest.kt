package nl.rhaydus.softcover.feature.reading.presentation.action

import io.kotest.matchers.shouldBe
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
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.softcover.feature.reading.presentation.util.UpdateBookProgress
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnUpdatePercentageProgressClickActionTest {

    private lateinit var updateBookProgress: UpdateBookProgress
    private lateinit var stateFlow: MutableStateFlow<ReadingScreenUiState>
    private lateinit var scope: ActionScope<ReadingScreenUiState, ReadingScreenEvent, ReadingLocalVariables>

    @BeforeEach
    fun setUp() {
        updateBookProgress = mockk(relaxed = true)
        stateFlow = MutableStateFlow(ReadingScreenUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(ReadingLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    private fun stubDependencies(testScope: kotlinx.coroutines.test.TestScope): ReadingScreenDependencies {
        val dispatcher = UnconfinedTestDispatcher(testScope.testScheduler)
        return mockk<ReadingScreenDependencies>(relaxed = true).also { mock ->
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

    private fun stubEditionWithPages(pages: Int?): BookEdition = mockk<BookEdition>().also { edition ->
        every { edition.pages } returns pages
    }

    private fun stubBookWithCurrentEditionPages(pages: Int?): Book = mockk<Book>().also { book ->
        val edition = stubEditionWithPages(pages = pages)
        every { book.currentEdition } returns edition
        every { book.defaultEdition } returns null
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
                    setLoading = any(),
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
                    setLoading = any(),
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
                    setLoading = any(),
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
                    setLoading = any(),
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
                updateBookProgress(any(), any(), any())
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
                every { b.currentEdition } returns currentEdition
                every { b.defaultEdition } returns defaultEdition
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
                    setLoading = any(),
                )
            }
        }

        @Test
        fun `falls back to 0 pages when both currentEdition and defaultEdition pages are null`() = runTest {
            // ----- Arrange -----
            val currentEdition = stubEditionWithPages(pages = null)
            val defaultEdition = stubEditionWithPages(pages = null)
            val book = mockk<Book>().also { b ->
                every { b.currentEdition } returns currentEdition
                every { b.defaultEdition } returns defaultEdition
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
                    setLoading = any(),
                )
            }
        }
    }
}
