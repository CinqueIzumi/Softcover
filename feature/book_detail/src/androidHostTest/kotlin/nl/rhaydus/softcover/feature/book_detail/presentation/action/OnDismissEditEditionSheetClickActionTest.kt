package nl.rhaydus.softcover.feature.book_detail.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.feature.book_detail.presentation.buildBookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.state.EditionSheetUiModel
import nl.rhaydus.softcover.feature.book_detail.presentation.state.IsbnLookup
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnDismissEditEditionSheetClickActionTest {
    private lateinit var dependencies: BookDetailDependencies
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var eventChannel: Channel<BookDetailEvent>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        dependencies = mockk(relaxed = true)
        stateFlow = MutableStateFlow(buildBookDetailUiState())
        eventChannel = Channel(Channel.BUFFERED)
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(BookDetailLocalVariables()),
            eventChannel = eventChannel,
        )
    }

    @Nested
    inner class Execute {
        @Test
        fun `sends the lookup event with the prefill and closes the session when a lookup is pending`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(
                editionSheetRequested = true,
                editionSheet = SheetSession.Open(
                    model = mockk<EditionSheetUiModel>(),
                    pending = IsbnLookup(prefilledIsbn = "9780374710781"),
                ),
                editionSearchQuery = "978",
                selectedEditionId = 5,
            )

            // ----- Act -----
            OnDismissEditEditionSheetClickAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            eventChannel.tryReceive().getOrNull() shouldBe BookDetailEvent.OpenIsbnLookupEvent(
                prefilledIsbn = "9780374710781",
            )
            stateFlow.value.editionSheet shouldBe SheetSession.Closed
            stateFlow.value.editionSheetRequested shouldBe false
            stateFlow.value.editionSearchQuery shouldBe ""
            stateFlow.value.selectedEditionId shouldBe null
        }

        @Test
        fun `sends no event when nothing is pending`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(
                editionSheet = SheetSession.Open(model = mockk<EditionSheetUiModel>()),
            )

            // ----- Act -----
            OnDismissEditEditionSheetClickAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            eventChannel.tryReceive().getOrNull() shouldBe null
            stateFlow.value.editionSheet shouldBe SheetSession.Closed
        }
    }
}
