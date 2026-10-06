package nl.rhaydus.softcover.feature.reading.presentation.collector

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.UserBookRead
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class MastheadCollectorTest {
    private lateinit var dependencies: ReadingScreenDependencies
    private lateinit var stateFlow: MutableStateFlow<ReadingScreenUiState>
    private lateinit var scope: ActionScope<ReadingScreenUiState, ReadingScreenEvent, ReadingLocalVariables>

    @BeforeEach
    fun setUp() {
        stateFlow = MutableStateFlow(ReadingScreenUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(ReadingLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
        dependencies = mockk<ReadingScreenDependencies>(relaxed = true)
    }

    private fun stubUserBookRead(progress: Float): UserBookRead = mockk<UserBookRead>(relaxed = true).also { read ->
        every {
            read.progress
        } returns progress
    }

    private fun stubBook(userBookRead: UserBookRead? = null): Book = mockk<Book>(relaxed = true).also { book ->
        every {
            book.userBookRead
        } returns userBookRead
    }

    private fun launchCollector(testScope: TestScope): Job = testScope.launch {
        MastheadCollector().onLaunch(
            scope = scope,
            dependencies = dependencies,
        )
    }

    @Nested
    inner class Masthead {
        @Test
        fun `subtitle counts zero books`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(books = emptyList())

            // ----- Act -----
            val job = launchCollector(this)

            // ----- Assert -----
            stateFlow.value.masthead.subtitle shouldBe "0 titles in motion"
            job.cancel()
        }

        @Test
        fun `subtitle uses the singular for exactly one book without a read`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(books = listOf(stubBook(userBookRead = null)))

            // ----- Act -----
            val job = launchCollector(this)

            // ----- Assert -----
            stateFlow.value.masthead.subtitle shouldBe "One title in motion"
            job.cancel()
        }

        @Test
        fun `subtitle counts several books`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(books = listOf(stubBook(), stubBook(), stubBook()))

            // ----- Act -----
            val job = launchCollector(this)

            // ----- Assert -----
            stateFlow.value.masthead.subtitle shouldBe "3 titles in motion"
            job.cancel()
        }

        @Test
        fun `subtitle appends the rounded average progress across books with a read`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val books = listOf(
                stubBook(userBookRead = stubUserBookRead(progress = 40f)),
                stubBook(userBookRead = stubUserBookRead(progress = 80f)),
            )
            stateFlow.value = ReadingScreenUiState(books = books)

            // ----- Act -----
            val job = launchCollector(this)

            // ----- Assert -----
            stateFlow.value.masthead.subtitle shouldBe "2 titles in motion · 60% along, on average"
            job.cancel()
        }

        @Test
        fun `subtitle averages progress only across books that have a read`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val books = listOf(
                stubBook(userBookRead = null),
                stubBook(userBookRead = stubUserBookRead(progress = 45f)),
            )
            stateFlow.value = ReadingScreenUiState(books = books)

            // ----- Act -----
            val job = launchCollector(this)

            // ----- Assert -----
            stateFlow.value.masthead.subtitle shouldBe "2 titles in motion · 45% along, on average"
            job.cancel()
        }

        @Test
        fun `recomputes off the latest state when books and loading change`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(
                books = emptyList(),
                isLoading = true,
            )
            val job = launchCollector(this)
            val loadingEyebrow = stateFlow.value.masthead.eyebrow

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(
                books = listOf(stubBook()),
                isLoading = false,
            )

            // ----- Assert -----
            stateFlow.value.masthead.eyebrow shouldNotBe loadingEyebrow
            stateFlow.value.masthead.subtitle shouldBe "One title in motion"
            job.cancel()
        }
    }

    @Nested
    inner class CompactMasthead {
        @Test
        fun `shares the same title and subtitle as the regular masthead`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(
                books = listOf(stubBook(), stubBook()),
                isLoading = false,
            )

            // ----- Act -----
            val job = launchCollector(this)

            // ----- Assert -----
            stateFlow.value.compactMasthead.title shouldBe stateFlow.value.masthead.title
            stateFlow.value.compactMasthead.subtitle shouldBe stateFlow.value.masthead.subtitle
            job.cancel()
        }
    }

    @Nested
    inner class MastheadRefresh {
        @Test
        fun `is disabled while loading and enabled once loading ends`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(isLoading = true)
            val job = launchCollector(this)
            val whileLoading = stateFlow.value.mastheadRefresh

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(isLoading = false)

            // ----- Assert -----
            whileLoading.enabled shouldBe false
            stateFlow.value.mastheadRefresh.enabled shouldBe true
            job.cancel()
        }
    }
}
