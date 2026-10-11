package nl.rhaydus.softcover.feature.book_detail.presentation.action

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.verdict.VerdictDraftEvent
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.uibinding.verdict.VerdictSheetContext
import nl.rhaydus.softcover.core.uibinding.verdict.toVerdictSheetUiModel
import nl.rhaydus.softcover.feature.book_detail.presentation.buildBookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnEditVerdictDraftActionTest {
    private lateinit var dependencies: BookDetailDependencies
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        stateFlow = MutableStateFlow(buildBookDetailUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(BookDetailLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
        dependencies = mockk<BookDetailDependencies>(relaxed = true)
    }

    private fun stubBook(): Book = mockk<Book>().also { mock ->
        every {
            mock.title
        } returns "Piranesi"

        every {
            mock.userBook
        } returns null
    }

    @Nested
    inner class Execute {
        @Test
        fun `folds a rating change into the open sheet`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(
                verdictSheet = stubBook().toVerdictSheetUiModel(
                    context = VerdictSheetContext.EDIT,
                    review = null,
                    cover = null,
                ),
            )

            // ----- Act -----
            OnEditVerdictDraftAction(event = VerdictDraftEvent.RatingChanged(rating = 4.0)).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.verdictSheet.shouldNotBeNull().rating shouldBe 4.0
        }

        @Test
        fun `folds a spoiler toggle into the open sheet`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(
                verdictSheet = stubBook().toVerdictSheetUiModel(
                    context = VerdictSheetContext.EDIT,
                    review = null,
                    cover = null,
                ),
            )

            // ----- Act -----
            OnEditVerdictDraftAction(event = VerdictDraftEvent.SpoilersToggled(checked = true)).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.verdictSheet.shouldNotBeNull().spoilers.checked shouldBe true
        }

        @Test
        fun `leaves the sheet null when no verdict sheet is open`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(verdictSheet = null)

            // ----- Act -----
            OnEditVerdictDraftAction(event = VerdictDraftEvent.RatingChanged(rating = 4.0)).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.verdictSheet.shouldBeNull()
        }
    }
}
