package nl.rhaydus.softcover.feature.reading.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.UserBookRead
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import nl.rhaydus.softcover.feature.reading.presentation.state.FeaturedProgressUiModel
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class FeaturedProgressCollectorTest {
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

    private fun stubBook(progress: Float): Book {
        val read = mockk<UserBookRead>(relaxed = true)
        every {
            read.progress
        } returns progress

        val book = mockk<Book>(relaxed = true)
        every {
            book.userBookRead
        } returns read

        return book
    }

    @Nested
    inner class FeaturedProgress {
        @Test
        fun `falls back to the loading stand-in when there is no featured book`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(books = emptyList())
            val collector = FeaturedProgressCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act & Assert -----
            stateFlow.value.featuredProgress shouldBe FeaturedProgressUiModel.Loading
            job.cancel()
        }

        @Test
        fun `maps the first book as the featured book`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(books = listOf(stubBook(progress = 40f), stubBook(progress = 90f)))
            val collector = FeaturedProgressCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act & Assert -----
            stateFlow.value.featuredProgress.percent shouldBe "40%"
            stateFlow.value.featuredProgress.isLoading shouldBe false
            job.cancel()
        }

        @Test
        fun `rebuilds when the featured book changes`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(books = listOf(stubBook(progress = 40f)))
            val collector = FeaturedProgressCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(books = listOf(stubBook(progress = 75f)))

            // ----- Assert -----
            stateFlow.value.featuredProgress.percent shouldBe "75%"
            job.cancel()
        }

        @Test
        fun `returns to the loading stand-in when the books are cleared`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(books = listOf(stubBook(progress = 40f)))
            val collector = FeaturedProgressCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(books = emptyList())

            // ----- Assert -----
            stateFlow.value.featuredProgress shouldBe FeaturedProgressUiModel.Loading
            job.cancel()
        }
    }
}
