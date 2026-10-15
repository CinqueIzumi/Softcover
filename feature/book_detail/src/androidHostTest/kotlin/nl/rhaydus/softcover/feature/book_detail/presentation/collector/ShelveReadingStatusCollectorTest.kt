package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.ReadingFormat
import nl.rhaydus.softcover.core.domain.model.UserBookRead
import nl.rhaydus.softcover.feature.book_detail.presentation.buildBookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.buildEdition
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ShelveReadingStatusCollectorTest {
    private lateinit var dependencies: BookDetailDependencies
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        stateFlow = MutableStateFlow(buildBookDetailUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(BookDetailLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
        dependencies = mockk(relaxed = true)
    }

    private fun stubBook(progress: Float): Book = mockk<Book> {
        every {
            currentRead
        } returns UserBookRead(
            id = 1,
            currentPage = 10,
            currentSeconds = null,
            progress = progress,
            startedAt = null,
            finishedAt = null,
        )

        every {
            currentEdition
        } returns buildEdition(id = 1).copy(
            pages = 100,
            readingFormat = ReadingFormat.Physical,
        )
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `sets shelveReadingStatus from the book in state`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(book = stubBook(progress = 10f))
            val collector = ShelveReadingStatusCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            val model = stateFlow.value.shelveReadingStatus.shouldNotBeNull()
            model.percent.value shouldBe 10.0
            model.position.value shouldBe 10.0
            job.cancel()
        }

        @Test
        fun `updates shelveReadingStatus when the book progress changes`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(book = stubBook(progress = 10f))
            val collector = ShelveReadingStatusCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(book = stubBook(progress = 60f))

            // ----- Assert -----
            stateFlow.value.shelveReadingStatus.shouldNotBeNull().percent.value shouldBe 60.0
            job.cancel()
        }

        @Test
        fun `leaves shelveReadingStatus null when there is no book`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = ShelveReadingStatusCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.shelveReadingStatus.shouldBeNull()
            job.cancel()
        }

        @Test
        fun `clears shelveReadingStatus when the book is removed`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(book = stubBook(progress = 10f))
            val collector = ShelveReadingStatusCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(book = null)

            // ----- Assert -----
            stateFlow.value.shelveReadingStatus.shouldBeNull()
            job.cancel()
        }
    }
}
