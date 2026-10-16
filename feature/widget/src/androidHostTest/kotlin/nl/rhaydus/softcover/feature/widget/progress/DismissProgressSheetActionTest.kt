package nl.rhaydus.softcover.feature.widget.progress

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class DismissProgressSheetActionTest {
    private val eventChannel = Channel<UpdateProgressEvent>(Channel.BUFFERED)
    private lateinit var dependencies: UpdateProgressDependencies
    private lateinit var localVariablesFlow: MutableStateFlow<UpdateProgressLocalVariables>
    private lateinit var stateFlow: MutableStateFlow<UpdateProgressUiState>
    private lateinit var scope: ActionScope<UpdateProgressUiState, UpdateProgressEvent, UpdateProgressLocalVariables>

    @BeforeEach
    fun setUp() {
        dependencies = mockk(relaxed = true)
        stateFlow = MutableStateFlow(UpdateProgressUiState())
        localVariablesFlow = MutableStateFlow(UpdateProgressLocalVariables())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = localVariablesFlow,
            eventChannel = eventChannel,
        )
    }

    @Nested
    inner class Execute {
        @Test
        fun `sends Finished and clears the book when no write is in flight`() = runTest {
            // ----- Arrange -----
            stateFlow.value = UpdateProgressUiState(bookToUpdate = stubPagedBook(pages = 10))

            // ----- Act -----
            DismissProgressSheetAction.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            eventChannel.tryReceive().getOrNull() shouldBe UpdateProgressEvent.Finished
            stateFlow.value.bookToUpdate.shouldBeNull()
            stateFlow.value.progressSheet shouldBe SheetSession.Closed
        }

        @Test
        fun `opens the verdict for the book a write finished and sends Finished`() = runTest {
            // ----- Arrange -----
            localVariablesFlow.value = UpdateProgressLocalVariables(finishedBookId = 7)

            // ----- Act -----
            DismissProgressSheetAction.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            verify(exactly = 1) {
                dependencies.openVerdict(7)
            }
            eventChannel.tryReceive().getOrNull() shouldBe UpdateProgressEvent.Finished
        }

        @Test
        fun `does not open a verdict when no write finished a book`() = runTest {
            // ----- Arrange -----
            localVariablesFlow.value = UpdateProgressLocalVariables(finishedBookId = null)

            // ----- Act -----
            DismissProgressSheetAction.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            verify(exactly = 0) {
                dependencies.openVerdict(any())
            }
        }

        @Test
        fun `waits for the in-flight write before sending Finished`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val gate = CompletableDeferred<Unit>()
            localVariablesFlow.value = UpdateProgressLocalVariables(
                mutationJob = launch { gate.await() },
            )
            val dismiss = launch {
                DismissProgressSheetAction.execute(
                    dependencies = dependencies,
                    scope = scope,
                )
            }
            val beforeWriteEnds = eventChannel.tryReceive().getOrNull()

            // ----- Act -----
            gate.complete(Unit)
            dismiss.join()

            // ----- Assert -----
            beforeWriteEnds.shouldBeNull()
            eventChannel.tryReceive().getOrNull() shouldBe UpdateProgressEvent.Finished
        }
    }
}
