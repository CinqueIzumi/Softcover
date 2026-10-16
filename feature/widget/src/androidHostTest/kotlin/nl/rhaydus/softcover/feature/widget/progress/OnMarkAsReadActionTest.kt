package nl.rhaydus.softcover.feature.widget.progress

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.book.domain.usecase.MarkBookAsReadUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.ShelfMutationOutcome
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnMarkAsReadActionTest {
    private val eventChannel = Channel<UpdateProgressEvent>(Channel.BUFFERED)
    private lateinit var markBookAsRead: MarkBookAsReadUseCase
    private lateinit var localVariablesFlow: MutableStateFlow<UpdateProgressLocalVariables>
    private lateinit var scope: ActionScope<UpdateProgressUiState, UpdateProgressEvent, UpdateProgressLocalVariables>

    @BeforeEach
    fun setUp() {
        markBookAsRead = mockk(relaxed = true)
        localVariablesFlow = MutableStateFlow(UpdateProgressLocalVariables())
        scope = ActionScope(
            stateFlow = MutableStateFlow(UpdateProgressUiState(bookToUpdate = stubPagedBook(pages = 100))),
            localVariablesFlow = localVariablesFlow,
            eventChannel = eventChannel,
        )
    }

    private fun stubMarkAsRead(outcome: ShelfMutationOutcome) {
        coEvery {
            markBookAsRead(
                book = any(),
                editionId = any(),
                actionAt = any(),
            )
        } returns Result.success(outcome)
    }

    @Nested
    inner class Execute {
        @Test
        fun `an applied write marks the book as finished`() = runTest {
            // ----- Arrange -----
            stubMarkAsRead(ShelfMutationOutcome.Applied)
            val dependencies = stubDependencies(this)
            every {
                dependencies.markBookAsRead
            } returns markBookAsRead

            // ----- Act -----
            OnMarkAsReadAction.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            localVariablesFlow.value.finishedBookId shouldBe 99
        }

        @Test
        fun `a write that changed nothing leaves the book unfinished`() = runTest {
            // ----- Arrange -----
            stubMarkAsRead(ShelfMutationOutcome.NoChange)
            val dependencies = stubDependencies(this)
            every {
                dependencies.markBookAsRead
            } returns markBookAsRead

            // ----- Act -----
            OnMarkAsReadAction.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            localVariablesFlow.value.finishedBookId.shouldBeNull()
        }

        @Test
        fun `a failed write flags the failure and leaves the book unfinished`() = runTest {
            // ----- Arrange -----
            coEvery {
                markBookAsRead(
                    book = any(),
                    editionId = any(),
                    actionAt = any(),
                )
            } returns Result.failure(IllegalStateException("boom"))
            val dependencies = stubDependencies(this)
            every {
                dependencies.markBookAsRead
            } returns markBookAsRead

            // ----- Act -----
            OnMarkAsReadAction.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            localVariablesFlow.value.writeFailed shouldBe true
            localVariablesFlow.value.finishedBookId.shouldBeNull()
        }
    }
}
