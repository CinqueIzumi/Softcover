package nl.rhaydus.softcover.feature.reading.presentation.action

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.verdict.VerdictDraftEvent
import nl.rhaydus.softcover.core.domain.preview.PreviewData
import nl.rhaydus.softcover.core.uibinding.verdict.VerdictSheetContext
import nl.rhaydus.softcover.core.uibinding.verdict.reduce
import nl.rhaydus.softcover.core.uibinding.verdict.toVerdictSheetUiModel
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnEditVerdictDraftActionTest {
    private lateinit var dependencies: ReadingScreenDependencies
    private lateinit var stateFlow: MutableStateFlow<ReadingScreenUiState>
    private lateinit var scope: ActionScope<ReadingScreenUiState, ReadingScreenEvent, ReadingLocalVariables>

    @BeforeEach
    fun setUp() {
        dependencies = mockk(relaxed = true)
        stateFlow = MutableStateFlow(ReadingScreenUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(ReadingLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    private fun sheet() = PreviewData.baseBook.toVerdictSheetUiModel(
        context = VerdictSheetContext.FINISHED,
        review = null,
        cover = null,
    )

    @Nested
    inner class Execute {
        @Test
        fun `folds a rating event into the draft held in state`() = runTest {
            // ----- Arrange -----
            val initial = sheet()
            stateFlow.value = ReadingScreenUiState(verdictSheet = initial)

            // ----- Act -----
            OnEditVerdictDraftAction(event = VerdictDraftEvent.RatingChanged(rating = 4.5)).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.verdictSheet shouldBe initial.copy(rating = 4.5)
        }

        @Test
        fun `folds a review edit into the draft the same way the reducer does`() = runTest {
            // ----- Arrange -----
            val initial = sheet()
            val event = VerdictDraftEvent.ReviewEdited(
                value = TextFieldValue(
                    text = "Loved it.",
                    selection = TextRange(9),
                ),
            )
            stateFlow.value = ReadingScreenUiState(verdictSheet = initial)

            // ----- Act -----
            OnEditVerdictDraftAction(event = event).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.verdictSheet shouldBe initial.reduce(event = event)
            stateFlow.value.verdictSheet.shouldNotBeNull().review.text shouldBe "Loved it."
        }

        @Test
        fun `folds a spoilers toggle into the draft`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(verdictSheet = sheet())

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
            stateFlow.value = ReadingScreenUiState(verdictSheet = null)

            // ----- Act -----
            OnEditVerdictDraftAction(event = VerdictDraftEvent.RatingChanged(rating = 2.0)).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.verdictSheet.shouldBeNull()
        }
    }
}
