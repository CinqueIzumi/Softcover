package nl.rhaydus.softcover.feature.reading.presentation.collector

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
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

class FeaturedBookCollectorTest {
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
            book.currentRead
        } returns read

        return book
    }

    @Nested
    inner class FeaturedProgress {
        @Test
        fun `falls back to the loading stand-in when there is no featured book`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(books = persistentListOf())
            val collector = FeaturedBookCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.featuredProgress shouldBe FeaturedProgressUiModel.Loading
            job.cancel()
        }

        @Test
        fun `maps the first book as the featured book`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(books = persistentListOf(stubBook(progress = 40f), stubBook(progress = 90f)))
            val collector = FeaturedBookCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.featuredProgress.percent.value shouldBe 40.0
            stateFlow.value.featuredProgress.isLoading shouldBe false
            job.cancel()
        }

        @Test
        fun `rebuilds when the featured book changes`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(books = persistentListOf(stubBook(progress = 40f)))
            val collector = FeaturedBookCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(books = persistentListOf(stubBook(progress = 75f)))

            // ----- Assert -----
            stateFlow.value.featuredProgress.percent.value shouldBe 75.0
            job.cancel()
        }
    }

    @Nested
    inner class FeaturedBookModel {
        private fun namedBook(
            id: Int,
            title: String,
        ): Book {
            val book = mockk<Book>(relaxed = true)
            every {
                book.id
            } returns id
            every {
                book.title
            } returns title

            return book
        }

        @Test
        fun `is null when there is no featured book`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(books = persistentListOf())
            val collector = FeaturedBookCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.featuredBookModel.shouldBeNull()
            job.cancel()
        }

        @Test
        fun `is set for the first book as the featured book`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(
                books = persistentListOf(namedBook(
                    id = 1,
                    title = "First",
                ), namedBook(
                    id = 2,
                    title = "Second",
                ),),
            )
            val collector = FeaturedBookCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            val model = stateFlow.value.featuredBookModel.shouldNotBeNull()
            model.id shouldBe 1
            model.title shouldBe "First"
            job.cancel()
        }

        @Test
        fun `updates when the featured book changes`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(books = persistentListOf(namedBook(
                id = 1,
                title = "Old",
            ),),)
            val collector = FeaturedBookCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(books = persistentListOf(namedBook(
                id = 2,
                title = "New",
            ),),)

            // ----- Assert -----
            val model = stateFlow.value.featuredBookModel.shouldNotBeNull()
            model.id shouldBe 2
            model.title shouldBe "New"
            job.cancel()
        }

        @Test
        fun `clears when the featured book goes away`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(books = persistentListOf(namedBook(
                id = 1,
                title = "Only",
            ),),)
            val collector = FeaturedBookCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(books = persistentListOf())

            // ----- Assert -----
            stateFlow.value.featuredBookModel.shouldBeNull()
            job.cancel()
        }
    }
}
