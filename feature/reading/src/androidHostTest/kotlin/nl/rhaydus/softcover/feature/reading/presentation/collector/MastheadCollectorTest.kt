package nl.rhaydus.softcover.feature.reading.presentation.collector

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.header.PageMastheadSubtitle
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
            stateFlow.value = ReadingScreenUiState(
                books = persistentListOf(),
                hasLoadedBooks = true,
            )

            // ----- Act -----
            val job = launchCollector(this)

            // ----- Assert -----
            stateFlow.value.masthead.shouldNotBeNull().subtitle shouldBe PageMastheadSubtitle.Text("0 titles in motion")
            job.cancel()
        }

        @Test
        fun `subtitle uses the singular for exactly one book without a read`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(
                books = persistentListOf(stubBook(userBookRead = null)),
                hasLoadedBooks = true,
            )

            // ----- Act -----
            val job = launchCollector(this)

            // ----- Assert -----
            stateFlow.value.masthead.shouldNotBeNull().subtitle shouldBe PageMastheadSubtitle.Text("One title in motion")
            job.cancel()
        }

        @Test
        fun `subtitle counts several books`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(
                books = persistentListOf(stubBook(), stubBook(), stubBook()),
                hasLoadedBooks = true,
            )

            // ----- Act -----
            val job = launchCollector(this)

            // ----- Assert -----
            stateFlow.value.masthead.shouldNotBeNull().subtitle shouldBe PageMastheadSubtitle.Text("3 titles in motion")
            job.cancel()
        }

        @Test
        fun `subtitle appends the rounded average progress across books with a read`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val books = listOf(
                stubBook(userBookRead = stubUserBookRead(progress = 40f)),
                stubBook(userBookRead = stubUserBookRead(progress = 80f)),
            )
            stateFlow.value = ReadingScreenUiState(
                books = books.toImmutableList(),
                hasLoadedBooks = true,
            )

            // ----- Act -----
            val job = launchCollector(this)

            // ----- Assert -----
            stateFlow.value.masthead.shouldNotBeNull().subtitle shouldBe PageMastheadSubtitle.Text(
                "2 titles in motion · 60% along, on average",
            )
            job.cancel()
        }

        @Test
        fun `subtitle averages progress only across books that have a read`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val books = listOf(
                stubBook(userBookRead = null),
                stubBook(userBookRead = stubUserBookRead(progress = 45f)),
            )
            stateFlow.value = ReadingScreenUiState(
                books = books.toImmutableList(),
                hasLoadedBooks = true,
            )

            // ----- Act -----
            val job = launchCollector(this)

            // ----- Assert -----
            stateFlow.value.masthead.shouldNotBeNull().subtitle shouldBe PageMastheadSubtitle.Text(
                "2 titles in motion · 45% along, on average",
            )
            job.cancel()
        }

        @Test
        fun `recomputes the subtitle when the books change`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(
                books = persistentListOf(),
                hasLoadedBooks = true,
            )
            val job = launchCollector(this)

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(books = persistentListOf(stubBook()))

            // ----- Assert -----
            stateFlow.value.masthead.shouldNotBeNull().subtitle shouldBe PageMastheadSubtitle.Text("One title in motion")
            job.cancel()
        }

        @Test
        fun `is null before the books have loaded`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(
                books = persistentListOf(stubBook()),
                hasLoadedBooks = false,
            )

            // ----- Act -----
            val job = launchCollector(this)

            // ----- Assert -----
            stateFlow.value.masthead.shouldBeNull()
            job.cancel()
        }

        @Test
        fun `is built once the books have loaded`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(hasLoadedBooks = false)
            val job = launchCollector(this)

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(hasLoadedBooks = true)

            // ----- Assert -----
            stateFlow.value.masthead.shouldNotBeNull()
            job.cancel()
        }

        @Test
        fun `eyebrow follows isRefreshing`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(
                hasLoadedBooks = true,
                isRefreshing = false,
            )
            val job = launchCollector(this)
            val idleEyebrow = stateFlow.value.masthead.shouldNotBeNull().eyebrow

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(isRefreshing = true)
            val refreshingEyebrow = stateFlow.value.masthead.shouldNotBeNull().eyebrow
            stateFlow.value = stateFlow.value.copy(isRefreshing = false)

            // ----- Assert -----
            idleEyebrow shouldBe mastheadEyebrowFor(isRefreshing = false)
            refreshingEyebrow shouldBe mastheadEyebrowFor(isRefreshing = true)
            stateFlow.value.masthead.shouldNotBeNull().eyebrow shouldBe idleEyebrow
            job.cancel()
        }
    }

    @Nested
    inner class CompactMasthead {
        @Test
        fun `shares the same title and subtitle as the regular masthead`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(
                books = persistentListOf(stubBook(), stubBook()),
                hasLoadedBooks = true,
            )

            // ----- Act -----
            val job = launchCollector(this)

            // ----- Assert -----
            val compact = stateFlow.value.compactMasthead.shouldNotBeNull()
            val regular = stateFlow.value.masthead.shouldNotBeNull()
            compact.title shouldBe regular.title
            compact.subtitle shouldBe regular.subtitle
            job.cancel()
        }

        @Test
        fun `is null before the books have loaded`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(hasLoadedBooks = false)

            // ----- Act -----
            val job = launchCollector(this)

            // ----- Assert -----
            stateFlow.value.compactMasthead.shouldBeNull()
            job.cancel()
        }
    }

    @Nested
    inner class MastheadRefresh {
        @Test
        fun `is disabled while refreshing and enabled once refreshing ends`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(isRefreshing = true)
            val job = launchCollector(this)
            val whileRefreshing = stateFlow.value.mastheadRefresh

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(isRefreshing = false)

            // ----- Assert -----
            whileRefreshing.enabled shouldBe false
            stateFlow.value.mastheadRefresh.enabled shouldBe true
            job.cancel()
        }

        @Test
        fun `ignores first-load isLoading`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(
                isLoading = true,
                isRefreshing = false,
            )

            // ----- Act -----
            val job = launchCollector(this)

            // ----- Assert -----
            stateFlow.value.mastheadRefresh.enabled shouldBe true
            job.cancel()
        }
    }
}
