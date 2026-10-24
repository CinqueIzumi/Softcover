package nl.rhaydus.softcover.feature.reading.presentation.collector

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.book.domain.usecase.GetCurrentlyReadingUserBooksUseCase
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.progress.ProgressSheetUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.progresssheet.presentation.ProgressSheetState
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CurrentlyReadingBooksCollectorTest {
    private val openProgressSheet = ProgressSheetState(
        requested = true,
        session = SheetSession.Open(model = ProgressSheetUiModel.previews.values.first()),
    )

    private lateinit var getCurrentlyReadingBooksUseCase: GetCurrentlyReadingUserBooksUseCase
    private lateinit var dependencies: ReadingScreenDependencies
    private lateinit var stateFlow: MutableStateFlow<ReadingScreenUiState>
    private lateinit var scope: ActionScope<ReadingScreenUiState, ReadingScreenEvent, ReadingLocalVariables>
    private lateinit var booksFlow: MutableSharedFlow<List<Book>>

    @BeforeEach
    fun setUp() {
        booksFlow = MutableSharedFlow()
        getCurrentlyReadingBooksUseCase = mockk()
        stateFlow = MutableStateFlow(ReadingScreenUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(ReadingLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        every {
            getCurrentlyReadingBooksUseCase()
        } returns booksFlow

        dependencies = mockk<ReadingScreenDependencies>(relaxed = true).also { mock ->
            every {
                mock.getCurrentlyReadingBooksUseCase
            } returns getCurrentlyReadingBooksUseCase
        }
    }

    private fun stubBook(): Book = mockk()

    @Nested
    inner class OnLaunch {
        @Test
        fun `stores the emitted books, marks them loaded and leaves other state alone`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(
                isLoading = true,
                progressSheet = openProgressSheet,
            )
            val books = listOf(stubBook(), stubBook())
            val collector = CurrentlyReadingBooksCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            booksFlow.emit(books)

            // ----- Assert -----
            stateFlow.value.books shouldBe books
            stateFlow.value.hasLoadedBooks shouldBe true
            stateFlow.value.progressSheet shouldBe openProgressSheet
            job.cancel()
        }

        @Test
        fun `marks an empty emission as loaded`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = CurrentlyReadingBooksCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            booksFlow.emit(emptyList())

            // ----- Assert -----
            stateFlow.value.hasLoadedBooks shouldBe true
            job.cancel()
        }

        @Test
        fun `has not loaded before the first emission`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = CurrentlyReadingBooksCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.hasLoadedBooks shouldBe false
            job.cancel()
        }

        @Test
        fun `never writes isLoading`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(isLoading = true)
            val collector = CurrentlyReadingBooksCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            booksFlow.emit(listOf(stubBook()))

            // ----- Assert -----
            stateFlow.value.isLoading shouldBe true
            job.cancel()
        }

        @Test
        fun `updates books to the latest emission when flow emits multiple times`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val firstBooks = listOf(stubBook())
            val secondBooks = listOf(stubBook(), stubBook(), stubBook())
            val collector = CurrentlyReadingBooksCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            booksFlow.emit(firstBooks)
            booksFlow.emit(secondBooks)

            // ----- Assert -----
            stateFlow.value.books shouldBe secondBooks
            job.cancel()
        }
    }

    @Nested
    inner class AlsoReadingHeader {
        @Test
        fun `eyebrow reads 0 more in motion when the flow emits an empty list`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = CurrentlyReadingBooksCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            booksFlow.emit(emptyList())

            // ----- Assert -----
            stateFlow.value.alsoReadingHeader.shouldBeInstanceOf<SectionHeaderUiModel.Section>().eyebrow shouldBe "0 more books"
            job.cancel()
        }

        @Test
        fun `eyebrow reads 0 more in motion when the flow emits a single book`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = CurrentlyReadingBooksCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            booksFlow.emit(listOf(stubBook()))

            // ----- Assert -----
            stateFlow.value.alsoReadingHeader.shouldBeInstanceOf<SectionHeaderUiModel.Section>().eyebrow shouldBe "0 more books"
            job.cancel()
        }

        @Test
        fun `eyebrow reads the singular One more in motion when the flow emits exactly two books`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val collector = CurrentlyReadingBooksCollector()
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Act -----
                booksFlow.emit(listOf(stubBook(), stubBook()))

                // ----- Assert -----
                stateFlow.value.alsoReadingHeader.shouldBeInstanceOf<SectionHeaderUiModel.Section>().eyebrow shouldBe "One more book"
                job.cancel()
            }

        @Test
        fun `eyebrow reads the count when the flow emits more than two books`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = CurrentlyReadingBooksCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            booksFlow.emit(listOf(stubBook(), stubBook(), stubBook(), stubBook()))

            // ----- Assert -----
            stateFlow.value.alsoReadingHeader.shouldBeInstanceOf<SectionHeaderUiModel.Section>().eyebrow shouldBe "3 more books"
            job.cancel()
        }
    }
}
