package nl.rhaydus.softcover.feature.book_detail.presentation.action

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
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

class OnLookUpEditionByIsbnClickActionTest {
    private lateinit var dependencies: BookDetailDependencies
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>
    private lateinit var model: EditionSheetUiModel

    @BeforeEach
    fun setUp() {
        dependencies = mockk(relaxed = true)
        model = mockk<EditionSheetUiModel>().also {
            every {
                it.requestingClose()
            } returns it
        }
        stateFlow = MutableStateFlow(buildBookDetailUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(BookDetailLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    @Nested
    inner class Execute {
        @Test
        fun `clears editionSheetRequested and records the cleaned query as the pending prefill`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(
                editionSheetRequested = true,
                editionSheet = SheetSession.Open(model = model),
                editionSearchQuery = "978-0-374-71078-1",
            )

            // ----- Act -----
            OnLookUpEditionByIsbnClickAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.editionSheetRequested shouldBe false
            stateFlow.value.editionSheet.shouldNotBeNull().let { it as SheetSession.Open }.pending shouldBe
                IsbnLookup(prefilledIsbn = "9780374710781")
        }

        @Test
        fun `records a null prefill when the query is not an ISBN`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(
                editionSheetRequested = true,
                editionSheet = SheetSession.Open(model = model),
                editionSearchQuery = "Penguin",
            )

            // ----- Act -----
            OnLookUpEditionByIsbnClickAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.editionSheet.shouldNotBeNull().let { it as SheetSession.Open }.pending shouldBe
                IsbnLookup(prefilledIsbn = null)
        }
    }
}
