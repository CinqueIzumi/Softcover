package nl.rhaydus.softcover.feature.explore.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.model.ContinueSeriesDismissalKind
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.softcover.feature.explore.presentation.state.PendingContinueSeriesDismissal
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnContinueSeriesDismissalChosenActionTest {
    private lateinit var dependencies: ExploreDependencies
    private lateinit var stateFlow: MutableStateFlow<ExploreScreenUiState>
    private lateinit var scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>

    @BeforeEach
    fun setUp() {
        dependencies = mockk(relaxed = true)
        stateFlow = MutableStateFlow(ExploreScreenUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(ExploreLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    @Nested
    inner class Execute {
        @Test
        fun `records the chosen book and kind as the pending dismissal`() = runTest {
            // ----- Arrange -----
            val action = OnContinueSeriesDismissalChosenAction(
                bookId = 7,
                kind = ContinueSeriesDismissalKind.WHOLE_SERIES,
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.pendingContinueSeriesDismissal shouldBe PendingContinueSeriesDismissal(
                bookId = 7,
                kind = ContinueSeriesDismissalKind.WHOLE_SERIES,
            )
        }

        @Test
        fun `replaces an earlier pending dismissal`() = runTest {
            // ----- Arrange -----
            stateFlow.value = stateFlow.value.copy(
                pendingContinueSeriesDismissal = PendingContinueSeriesDismissal(
                    bookId = 1,
                    kind = ContinueSeriesDismissalKind.THIS_BOOK,
                ),
            )
            val action = OnContinueSeriesDismissalChosenAction(
                bookId = 2,
                kind = ContinueSeriesDismissalKind.THIS_BOOK,
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.pendingContinueSeriesDismissal?.bookId shouldBe 2
        }
    }
}
