package nl.rhaydus.softcover.feature.book_detail.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.celebration.MarkAsReadBurstScale
import nl.rhaydus.softcover.feature.book_detail.presentation.buildBookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnCelebrateMarkedAsReadActionTest {
    private lateinit var dependencies: BookDetailDependencies
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        dependencies = mockk(relaxed = true)
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
        fun `bumps the burst trigger key from 0 to 1`() = runTest {
            // ----- Arrange -----
            val action = OnCelebrateMarkedAsReadAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.markAsReadBurst.triggerKey shouldBe 1
        }

        @Test
        fun `bumps the key again on a second execute and leaves the scale unchanged`() = runTest {
            // ----- Arrange -----
            val action = OnCelebrateMarkedAsReadAction()
            val scaleBefore = stateFlow.value.markAsReadBurst.scale

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.markAsReadBurst.triggerKey shouldBe 2
            stateFlow.value.markAsReadBurst.scale shouldBe scaleBefore
            stateFlow.value.markAsReadBurst.scale shouldBe MarkAsReadBurstScale.Card
        }
    }
}
