package nl.rhaydus.softcover.feature.reading.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.header.MastheadRefreshUiModel
import nl.rhaydus.softcover.core.component.header.PageMastheadSize
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

    @Nested
    inner class Masthead {
        @Test
        fun `eyebrow reads "Now reading" when not loading`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(
                books = emptyList(),
                isLoading = false,
            )
            val collector = MastheadCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act & Assert -----
            stateFlow.value.masthead.eyebrow shouldBe "Now reading"
            job.cancel()
        }

        @Test
        fun `eyebrow reads "Catching up on your reading…" while loading`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(
                books = emptyList(),
                isLoading = true,
            )
            val collector = MastheadCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act & Assert -----
            stateFlow.value.masthead.eyebrow shouldBe "Catching up on your reading…"
            job.cancel()
        }

        @Test
        fun `title is the current time-of-day greeting`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = MastheadCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act & Assert -----
            stateFlow.value.masthead.title shouldBe greetingForNow()
            job.cancel()
        }

        @Test
        fun `subtitle reads "0 titles in motion" for an empty book list`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(books = emptyList())
            val collector = MastheadCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act & Assert -----
            stateFlow.value.masthead.subtitle shouldBe "0 titles in motion"
            job.cancel()
        }

        @Test
        fun `subtitle reads the singular "One title in motion" for exactly one book`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(books = listOf(stubBook()))
            val collector = MastheadCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act & Assert -----
            stateFlow.value.masthead.subtitle shouldBe "One title in motion"
            job.cancel()
        }

        @Test
        fun `subtitle counts several books`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(books = listOf(stubBook(), stubBook(), stubBook()))
            val collector = MastheadCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act & Assert -----
            stateFlow.value.masthead.subtitle shouldBe "3 titles in motion"
            job.cancel()
        }

        @Test
        fun `subtitle omits the average progress when no book has a read in progress`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(books = listOf(stubBook(userBookRead = null)))
            val collector = MastheadCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act & Assert -----
            stateFlow.value.masthead.subtitle shouldBe "One title in motion"
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
            val collector = MastheadCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act & Assert -----
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
            val collector = MastheadCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act & Assert -----
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
            val collector = MastheadCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(
                books = listOf(stubBook()),
                isLoading = false,
            )

            // ----- Assert -----
            stateFlow.value.masthead.eyebrow shouldBe "Now reading"
            stateFlow.value.masthead.subtitle shouldBe "One title in motion"
            job.cancel()
        }
    }

    @Nested
    inner class CompactMasthead {
        @Test
        fun `has no eyebrow`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = MastheadCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act & Assert -----
            stateFlow.value.compactMasthead.eyebrow shouldBe null
            job.cancel()
        }

        @Test
        fun `is sized Compact`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = MastheadCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act & Assert -----
            stateFlow.value.compactMasthead.size shouldBe PageMastheadSize.Compact
            job.cancel()
        }

        @Test
        fun `shares the same title and subtitle as the regular masthead`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(
                books = listOf(stubBook(), stubBook()),
                isLoading = false,
            )
            val collector = MastheadCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act & Assert -----
            stateFlow.value.compactMasthead.title shouldBe stateFlow.value.masthead.title
            stateFlow.value.compactMasthead.subtitle shouldBe stateFlow.value.masthead.subtitle
            job.cancel()
        }
    }

    @Nested
    inner class MastheadRefresh {
        @Test
        fun `reads "Refresh" and is enabled when not loading`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(isLoading = false)
            val collector = MastheadCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act & Assert -----
            stateFlow.value.mastheadRefresh shouldBe MastheadRefreshUiModel(
                label = "Refresh",
                enabled = true,
            )
            job.cancel()
        }

        @Test
        fun `reads "Refreshing…" and is disabled while loading`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(isLoading = true)
            val collector = MastheadCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act & Assert -----
            stateFlow.value.mastheadRefresh shouldBe MastheadRefreshUiModel(
                label = "Refreshing…",
                enabled = false,
            )
            job.cancel()
        }
    }

    @Nested
    inner class Defaults {
        @Test
        fun `ReadingScreenUiState defaults match the collector's output for the untouched default state`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val collector = MastheadCollector()
                val defaults = ReadingScreenUiState()

                // ----- Act -----
                val job = launch { collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                ) }

                // ----- Assert -----
                stateFlow.value.masthead.eyebrow shouldBe "Catching up on your reading…"
                stateFlow.value.masthead.eyebrow shouldBe defaults.masthead.eyebrow
                stateFlow.value.masthead.title shouldBe greetingForNow()
                stateFlow.value.masthead.subtitle shouldBe defaults.masthead.subtitle
                stateFlow.value.compactMasthead.subtitle shouldBe defaults.compactMasthead.subtitle
                stateFlow.value.mastheadRefresh shouldBe MastheadRefreshUiModel(
                    label = "Refreshing…",
                    enabled = false,
                )
                stateFlow.value.mastheadRefresh shouldBe defaults.mastheadRefresh
                job.cancel()
            }
    }
}
