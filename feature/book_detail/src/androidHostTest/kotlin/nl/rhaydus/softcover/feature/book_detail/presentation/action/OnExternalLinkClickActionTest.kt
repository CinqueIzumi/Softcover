package nl.rhaydus.softcover.feature.book_detail.presentation.action

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.button.PillButtonSet
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.state.ExternalLinksUiModel
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
        stateFlow = MutableStateFlow(BookDetailUiState())
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
            stateFlow.value = BookDetailUiState(
                externalLinks = ExternalLinksUiModel(
                    links = PillButtonSet(
                        payloadByKey = persistentMapOf(
                            "bookshop" to "https://bookshop.org/a",
                            "amazon" to "https://amazon.com/b",
                        ),
                    ),
                ),
            )
            val action = OnExternalLinkClickAction(key = "amazon")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            val event = eventChannel.tryReceive().getOrNull()
            event.shouldNotBeNull()
            event.shouldBeInstanceOf<BookDetailEvent.OpenExternalLinkEvent>()
            event.url shouldBe "https://amazon.com/b"
        }

        @Test
        fun `sends no event when the key is unknown`() = runTest {
            // ----- Arrange -----
            stateFlow.value = BookDetailUiState(
                externalLinks = ExternalLinksUiModel(
                    links = PillButtonSet(
                        payloadByKey = persistentMapOf("bookshop" to "https://bookshop.org/a"),
                    ),
                ),
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
        fun `sends no event when externalLinks is null`() = runTest {
            // ----- Arrange -----
            stateFlow.value = BookDetailUiState(externalLinks = null)
            val action = OnExternalLinkClickAction(key = "bookshop")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            val event = eventChannel.tryReceive().getOrNull()
            event.shouldBeNull()
        }
    }
}
