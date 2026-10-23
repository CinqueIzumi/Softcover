package nl.rhaydus.softcover.feature.book_detail.presentation.action

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.presentation.model.BookInitialCover
import nl.rhaydus.softcover.feature.book_detail.presentation.buildBookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.buildSimilarBook
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnSimilarBookClickActionTest {
    private lateinit var dependencies: BookDetailDependencies
    private lateinit var localVariablesFlow: MutableStateFlow<BookDetailLocalVariables>
    private lateinit var eventChannel: Channel<BookDetailEvent>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        dependencies = mockk(relaxed = true)
        eventChannel = Channel(Channel.BUFFERED)
        localVariablesFlow = MutableStateFlow(BookDetailLocalVariables())
        scope = ActionScope(
            stateFlow = MutableStateFlow(buildBookDetailUiState()),
            localVariablesFlow = localVariablesFlow,
            eventChannel = eventChannel,
        )
    }

    @Nested
    inner class Execute {
        @Test
        fun `opens the clicked book with its cover and the source-scoped surface`() = runTest {
            // ----- Arrange -----
            val book = buildSimilarBook(id = 11)
            localVariablesFlow.value = BookDetailLocalVariables(
                similarBooksSourceId = 5,
                similarBooks = listOf(buildSimilarBook(id = 10), book),
            )

            // ----- Act -----
            OnSimilarBookClickAction(bookId = 11).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            val event = eventChannel.tryReceive().getOrNull()
                .shouldNotBeNull()
                .shouldBeInstanceOf<BookDetailEvent.OpenSimilarBookEvent>()
            event.destination.id shouldBe 11
            event.destination.initialCover shouldBe BookInitialCover.fromBook(book)
            event.destination.transitionSurface shouldBe "similar:5"
        }

        @Test
        fun `sends nothing when the book is not among the loaded ones`() = runTest {
            // ----- Arrange -----
            localVariablesFlow.value = BookDetailLocalVariables(
                similarBooksSourceId = 5,
                similarBooks = listOf(buildSimilarBook(id = 10)),
            )

            // ----- Act -----
            OnSimilarBookClickAction(bookId = 99).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            eventChannel.tryReceive().getOrNull().shouldBeNull()
        }

        @Test
        fun `sends nothing before a source book has been recorded`() = runTest {
            // ----- Arrange -----
            localVariablesFlow.value = BookDetailLocalVariables(
                similarBooks = listOf(buildSimilarBook(id = 10)),
            )

            // ----- Act -----
            OnSimilarBookClickAction(bookId = 10).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            eventChannel.tryReceive().getOrNull().shouldBeNull()
        }
    }
}
