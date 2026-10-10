package nl.rhaydus.softcover.feature.book_detail.presentation.action

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.state.EditionSheetUiModel
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnEditionSearchQueryChangeActionTest {
    private lateinit var dependencies: BookDetailDependencies
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        stateFlow = MutableStateFlow(BookDetailUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(BookDetailLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
        dependencies = mockk<BookDetailDependencies>(relaxed = true)
    }

    private fun openSheet(query: String = ""): EditionSheetUiModel = EditionSheetUiModel(
        scaffold = mockk<SheetScaffoldUiModel>(),
        cards = persistentListOf(),
        countHeader = SectionHeaderUiModel.Label(eyebrow = "0 EDITIONS"),
        searchQuery = query,
        searchPlaceholder = "Search",
        emptyMessage = "Nothing",
    )

    @Nested
    inner class Execute {
        @Test
        fun `writes the query to the open sheet`() = runTest {
            // ----- Arrange -----
            stateFlow.value = BookDetailUiState(editionSheet = openSheet(query = "old"))

            // ----- Act -----
            OnEditionSearchQueryChangeAction(query = "978").execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.editionSheet.shouldNotBeNull().searchQuery shouldBe "978"
        }

        @Test
        fun `leaves the sheet null when none is open`() = runTest {
            // ----- Arrange -----
            stateFlow.value = BookDetailUiState(editionSheet = null)

            // ----- Act -----
            OnEditionSearchQueryChangeAction(query = "penguin").execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.editionSheet.shouldBeNull()
        }
    }
}
