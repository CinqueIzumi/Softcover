package nl.rhaydus.softcover.feature.session.presentation.action

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.presentation.session.ActiveSession
import nl.rhaydus.softcover.core.presentation.session.ActiveSessionController
import nl.rhaydus.softcover.feature.session.presentation.event.FocusModeEvent
import nl.rhaydus.softcover.feature.session.presentation.screenmodel.FocusModeDependencies
import nl.rhaydus.softcover.feature.session.presentation.state.FocusModeUiState
import nl.rhaydus.softcover.feature.session.presentation.state.LocalFocusModeVariables
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnPageDraftChangedActionTest {
    private lateinit var activeSessionFlow: MutableStateFlow<ActiveSession?>
    private lateinit var dependencies: FocusModeDependencies
    private lateinit var stateFlow: MutableStateFlow<FocusModeUiState>
    private lateinit var localVariablesFlow: MutableStateFlow<LocalFocusModeVariables>
    private lateinit var scope: ActionScope<FocusModeUiState, FocusModeEvent, LocalFocusModeVariables>

    @BeforeEach
    fun setUp() {
        activeSessionFlow = MutableStateFlow(null)
        val controller = mockk<ActiveSessionController>(relaxed = true) {
            every {
                activeSession
            } returns activeSessionFlow
        }
        dependencies = mockk(relaxed = true) {
            every {
                activeSessionController
            } returns controller
        }
        stateFlow = MutableStateFlow(FocusModeUiState())
        localVariablesFlow = MutableStateFlow(LocalFocusModeVariables(isFirstFocusEvent = false))
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = localVariablesFlow,
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    private fun startSession(totalPages: Int) {
        activeSessionFlow.value = mockk(relaxed = true) {
            every {
                book
            } returns mockk(relaxed = true) {
                every {
                    currentEdition
                } returns mockk(relaxed = true) {
                    every {
                        pages
                    } returns totalPages
                }
            }
        }
    }

    private fun draft(
        text: String,
        selection: TextRange = TextRange.Zero,
    ) = TextFieldValue(
        text = text,
        selection = selection,
    )

    private suspend fun change(newValue: TextFieldValue) {
        OnPageDraftChangedAction(newValue = newValue).execute(
            dependencies = dependencies,
            scope = scope,
        )
    }

    @Nested
    inner class Execute {
        @Test
        fun `accepts a number within the page count`() = runTest {
            // ----- Arrange -----
            startSession(totalPages = 300)
            stateFlow.value = FocusModeUiState(pageDraft = draft("10"))

            // ----- Act -----
            change(draft("120"))

            // ----- Assert -----
            stateFlow.value.pageDraft.text shouldBe "120"
        }

        @Test
        fun `clamps a number above the page count to the page count`() = runTest {
            // ----- Arrange -----
            startSession(totalPages = 300)
            stateFlow.value = FocusModeUiState(pageDraft = draft("10"))

            // ----- Act -----
            change(draft("9999"))

            // ----- Assert -----
            stateFlow.value.pageDraft.text shouldBe "300"
        }

        @Test
        fun `clears the text for a non-numeric edit`() = runTest {
            // ----- Arrange -----
            startSession(totalPages = 300)
            stateFlow.value = FocusModeUiState(pageDraft = draft("10"))

            // ----- Act -----
            change(draft("1a"))

            // ----- Assert -----
            stateFlow.value.pageDraft.text shouldBe ""
        }

        @Test
        fun `accepts an emptied field as given`() = runTest {
            // ----- Arrange -----
            startSession(totalPages = 300)
            stateFlow.value = FocusModeUiState(pageDraft = draft("10"))

            // ----- Act -----
            change(draft(""))

            // ----- Assert -----
            stateFlow.value.pageDraft.text shouldBe ""
        }

        @Test
        fun `takes the new selection when the text is unchanged`() = runTest {
            // ----- Arrange -----
            startSession(totalPages = 300)
            stateFlow.value = FocusModeUiState(pageDraft = draft("10"))
            val selection = TextRange(
                start = 1,
                end = 2,
            )

            // ----- Act -----
            change(
                draft(
                    text = "10",
                    selection = selection,
                ),
            )

            // ----- Assert -----
            stateFlow.value.pageDraft.text shouldBe "10"
            stateFlow.value.pageDraft.selection shouldBe selection
        }

        @Test
        fun `the first callback after focus keeps the existing selection and clears the flag`() = runTest {
            // ----- Arrange -----
            startSession(totalPages = 300)
            val selectAll = TextRange(
                start = 0,
                end = 2,
            )
            stateFlow.value = FocusModeUiState(
                pageDraft = draft(
                    text = "10",
                    selection = selectAll,
                ),
            )
            localVariablesFlow.value = LocalFocusModeVariables(isFirstFocusEvent = true)

            // ----- Act -----
            change(
                draft(
                    text = "10",
                    selection = TextRange(2),
                ),
            )

            // ----- Assert -----
            stateFlow.value.pageDraft.selection shouldBe selectAll
            localVariablesFlow.value.isFirstFocusEvent shouldBe false
        }

        @Test
        fun `leaves the draft untouched when there is no active session`() = runTest {
            // ----- Arrange -----
            stateFlow.value = FocusModeUiState(pageDraft = draft("10"))

            // ----- Act -----
            change(draft("55"))

            // ----- Assert -----
            stateFlow.value.pageDraft.text shouldBe "10"
        }
    }
}
