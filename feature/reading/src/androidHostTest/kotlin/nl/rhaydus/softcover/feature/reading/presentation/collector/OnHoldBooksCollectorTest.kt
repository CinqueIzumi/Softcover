package nl.rhaydus.softcover.feature.reading.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.book.domain.usecase.GetPausedUserBooksUseCase
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnHoldBooksCollectorTest {
    private lateinit var dependencies: ReadingScreenDependencies
    private lateinit var stateFlow: MutableStateFlow<ReadingScreenUiState>
    private lateinit var scope: ActionScope<ReadingScreenUiState, ReadingScreenEvent, ReadingLocalVariables>
    private lateinit var booksFlow: MutableSharedFlow<List<Book>>

    @BeforeEach
    fun setUp() {
        booksFlow = MutableSharedFlow()
        stateFlow = MutableStateFlow(ReadingScreenUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(ReadingLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        val getPausedUserBooksUseCase: GetPausedUserBooksUseCase = mockk()

        every {
            getPausedUserBooksUseCase()
        } returns booksFlow

        dependencies = mockk<ReadingScreenDependencies>(relaxed = true).also { mock ->
            every {
                mock.getPausedUserBooksUseCase
            } returns getPausedUserBooksUseCase
        }
    }

    private fun pausedBook(
        id: Int,
        progress: Float,
        isAudiobook: Boolean = false,
    ): Book = mockk(relaxed = true) {
        every {
            this@mockk.id
        } returns id
        every {
            currentEdition?.isAudiobook
        } returns isAudiobook
        every {
            currentRead?.progress
        } returns progress
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `maps paused books to on hold rows keyed by id with book payload`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val first = pausedBook(
                id = 1,
                progress = 25f,
            )
            val second = pausedBook(
                id = 2,
                progress = 60f,
            )
            val collector = OnHoldBooksCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            booksFlow.emit(listOf(first, second))

            // ----- Assert -----
            stateFlow.value.onHold.cards.map { it.key } shouldBe listOf(
                BookCardKey.Book(bookId = 1),
                BookCardKey.Book(bookId = 2),
            )
            stateFlow.value.onHold[BookCardKey.Book(bookId = 2)] shouldBe second
            job.cancel()
        }

        @Test
        fun `an empty emission clears the rows`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = OnHoldBooksCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }
            booksFlow.emit(
                listOf(
                    pausedBook(
                        id = 1,
                        progress = 10f,
                    ),
                ),
            )

            // ----- Act -----
            booksFlow.emit(emptyList())

            // ----- Assert -----
            stateFlow.value.onHold.isEmpty shouldBe true
            job.cancel()
        }

        @Test
        fun `does not gate the first load`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = OnHoldBooksCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            booksFlow.emit(emptyList())

            // ----- Assert -----
            stateFlow.value.isLoading shouldBe ReadingScreenUiState().isLoading
            stateFlow.value.hasLoadedBooks shouldBe false
            job.cancel()
        }
    }
}
