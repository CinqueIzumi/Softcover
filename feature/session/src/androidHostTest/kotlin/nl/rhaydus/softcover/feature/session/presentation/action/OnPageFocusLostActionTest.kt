package nl.rhaydus.softcover.feature.session.presentation.action

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.UserBookRead
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

class OnPageFocusLostActionTest {
    private lateinit var controller: ActiveSessionController
    private lateinit var activeSessionFlow: MutableStateFlow<ActiveSession?>
    private lateinit var dependencies: FocusModeDependencies
    private lateinit var stateFlow: MutableStateFlow<FocusModeUiState>
    private lateinit var localVariablesFlow: MutableStateFlow<LocalFocusModeVariables>
    private lateinit var eventChannel: Channel<FocusModeEvent>
    private lateinit var scope: ActionScope<FocusModeUiState, FocusModeEvent, LocalFocusModeVariables>

    @BeforeEach
    fun setUp() {
        activeSessionFlow = MutableStateFlow(null)
        controller = mockk(relaxed = true) {
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
        eventChannel = Channel(Channel.BUFFERED)
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = localVariablesFlow,
            eventChannel = eventChannel,
        )
    }

    private fun storePage(currentPage: Int?) {
        val read = mockk<UserBookRead> {
            every {
                this@mockk.currentPage
            } returns currentPage
        }

        activeSessionFlow.value = mockk(relaxed = true) {
            every {
                book
            } returns mockk(relaxed = true) {
                every {
                    userBookRead
                } returns read
            }
        }
    }

    private fun typeDraft(text: String) {
        stateFlow.value = FocusModeUiState(
            pageDraft = TextFieldValue(
                text = text,
                selection = TextRange(
                    start = 0,
                    end = text.length,
                ),
            ),
        )
    }

    @Nested
    inner class Execute {
        @Test
        fun `commits a changed page and emits PageCommittedEvent`() = runTest {
            // ----- Arrange -----
            storePage(currentPage = 10)
            typeDraft("25")

            // ----- Act -----
            OnPageFocusLostAction.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            verify(exactly = 1) { controller.updatePage(newPage = 25) }
            eventChannel.tryReceive().getOrNull().shouldNotBeNull().shouldBeInstanceOf<FocusModeEvent.PageCommittedEvent>()
        }

        @Test
        fun `does not commit when the draft equals the stored page`() = runTest {
            // ----- Arrange -----
            storePage(currentPage = 10)
            typeDraft("10")

            // ----- Act -----
            OnPageFocusLostAction.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            verify(exactly = 0) { controller.updatePage(any()) }
            eventChannel.tryReceive().getOrNull() shouldBe null
        }

        @Test
        fun `does not commit an empty draft`() = runTest {
            // ----- Arrange -----
            storePage(currentPage = 10)
            typeDraft("")

            // ----- Act -----
            OnPageFocusLostAction.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            verify(exactly = 0) { controller.updatePage(any()) }
            eventChannel.tryReceive().getOrNull() shouldBe null
        }

        @Test
        fun `treats a missing stored page as zero`() = runTest {
            // ----- Arrange -----
            storePage(currentPage = null)
            typeDraft("0")

            // ----- Act -----
            OnPageFocusLostAction.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            verify(exactly = 0) { controller.updatePage(any()) }
        }

        @Test
        fun `commits against a zero baseline when there is no active session`() = runTest {
            // ----- Arrange -----
            typeDraft("7")

            // ----- Act -----
            OnPageFocusLostAction.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            verify(exactly = 1) { controller.updatePage(newPage = 7) }
        }

        @Test
        fun `clears the selection and re-arms the first-focus flag`() = runTest {
            // ----- Arrange -----
            localVariablesFlow.value = LocalFocusModeVariables(isFirstFocusEvent = false)
            typeDraft("10")

            // ----- Act -----
            OnPageFocusLostAction.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            localVariablesFlow.value.isFirstFocusEvent shouldBe true
            stateFlow.value.pageDraft.selection shouldBe TextRange.Zero
        }

        @Test
        fun `clears a previously rejected page draft`() = runTest {
            // ----- Arrange -----
            typeDraft("10")
            stateFlow.value = stateFlow.value.copy(pageDraftRejected = true)

            // ----- Act -----
            OnPageFocusLostAction.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.pageDraftRejected shouldBe false
        }
    }
}
