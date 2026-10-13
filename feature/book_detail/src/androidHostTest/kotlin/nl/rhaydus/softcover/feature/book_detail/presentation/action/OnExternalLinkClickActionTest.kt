package nl.rhaydus.softcover.feature.book_detail.presentation.action

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.feature.book_detail.presentation.buildBookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.buildEdition
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.AMAZON_KEY
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.BOOKSHOP_KEY
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.OPEN_LIBRARY_KEY
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnExternalLinkClickActionTest {
    private lateinit var dependencies: BookDetailDependencies
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var localVariablesFlow: MutableStateFlow<BookDetailLocalVariables>
    private lateinit var eventChannel: Channel<BookDetailEvent>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        dependencies = mockk(relaxed = true)
        stateFlow = MutableStateFlow(buildBookDetailUiState())
        localVariablesFlow = MutableStateFlow(BookDetailLocalVariables())
        eventChannel = Channel(Channel.BUFFERED)
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = localVariablesFlow,
            eventChannel = eventChannel,
        )
    }

    @Nested
    inner class Execute {
        @Test
        fun `sends OpenExternalLinkEvent with the url of the known key`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(
                previewEdition = buildEdition(id = 1).copy(isbn13 = "9780306406157"),
            )
            val action = OnExternalLinkClickAction(key = AMAZON_KEY)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            val event = eventChannel.tryReceive().getOrNull()
            event.shouldNotBeNull()
            event.shouldBeInstanceOf<BookDetailEvent.OpenExternalLinkEvent>()
            event.url shouldBe "https://www.amazon.com/s?k=9780306406157"
        }

        @Test
        fun `sends no event when the key is unknown`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(
                previewEdition = buildEdition(id = 1).copy(isbn13 = "9780306406157"),
            )
            val action = OnExternalLinkClickAction(key = "missing")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            val event = eventChannel.tryReceive().getOrNull()
            event.shouldBeNull()
        }

        @Test
        fun `sends no event when the edition has no ISBN`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(
                previewEdition = buildEdition(id = 1).copy(
                    isbn10 = null,
                    isbn13 = null,
                ),
            )
            val action = OnExternalLinkClickAction(key = BOOKSHOP_KEY)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            val event = eventChannel.tryReceive().getOrNull()
            event.shouldBeNull()
        }

        @Test
        fun `sends the bookshop search url for the bookshop key`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(
                previewEdition = buildEdition(id = 1).copy(isbn13 = "9780306406157"),
            )
            val action = OnExternalLinkClickAction(key = BOOKSHOP_KEY)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            val event = eventChannel.tryReceive().getOrNull()
                .shouldBeInstanceOf<BookDetailEvent.OpenExternalLinkEvent>()
            event.url shouldBe "https://bookshop.org/search?keywords=9780306406157"
        }

        @Test
        fun `sends the open library search url for the open library key`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(
                previewEdition = buildEdition(id = 1).copy(isbn13 = "9780306406157"),
            )
            val action = OnExternalLinkClickAction(key = OPEN_LIBRARY_KEY)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            val event = eventChannel.tryReceive().getOrNull()
                .shouldBeInstanceOf<BookDetailEvent.OpenExternalLinkEvent>()
            event.url shouldBe "https://openlibrary.org/search?isbn=9780306406157"
        }

        @Test
        fun `searches by the ISBN10 when the edition has no ISBN13`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(
                previewEdition = buildEdition(id = 1).copy(
                    isbn10 = "0306406152",
                    isbn13 = null,
                ),
            )
            val action = OnExternalLinkClickAction(key = AMAZON_KEY)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            val event = eventChannel.tryReceive().getOrNull()
                .shouldBeInstanceOf<BookDetailEvent.OpenExternalLinkEvent>()
            event.url shouldBe "https://www.amazon.com/s?k=0306406152"
        }

        @Test
        fun `sends no event when no edition is displayed`() = runTest {
            // ----- Arrange -----
            val action = OnExternalLinkClickAction(key = AMAZON_KEY)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            eventChannel.tryReceive().getOrNull().shouldBeNull()
        }
    }
}
