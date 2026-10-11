package nl.rhaydus.softcover.feature.explore.presentation.action

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.core.component.sheet.dismissed
import nl.rhaydus.softcover.core.component.sheet.isClosing
import nl.rhaydus.softcover.core.component.sheet.modelOrNull
import nl.rhaydus.softcover.core.domain.preview.PreviewData
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.mapper.continueSeriesMenuSheetFor
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

    private val menu = continueSeriesMenuSheetFor(book = PreviewData.baseBook.copy(id = 7))

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
        fun `asks the open menu to close and records the chosen book and kind as pending`() = runTest {
            // ----- Arrange -----
            stateFlow.value = stateFlow.value.copy(
                continueSeriesMenu = SheetSession.Open(model = menu),
                continueSeriesMenuBookId = 7,
            )
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
            stateFlow.value.continueSeriesMenu.isClosing shouldBe true
            stateFlow.value.continueSeriesMenu.dismissed() shouldBe PendingContinueSeriesDismissal(
                bookId = 7,
                kind = ContinueSeriesDismissalKind.WHOLE_SERIES,
            )
            stateFlow.value.continueSeriesMenu.modelOrNull shouldBe menu.requestingClose()
            stateFlow.value.continueSeriesMenuBookId.shouldBeNull()
        }

        @Test
        fun `replaces an earlier pending dismissal`() = runTest {
            // ----- Arrange -----
            stateFlow.value = stateFlow.value.copy(
                continueSeriesMenu = SheetSession.Open(
                    model = menu.requestingClose(),
                    pending = PendingContinueSeriesDismissal(
                        bookId = 1,
                        kind = ContinueSeriesDismissalKind.THIS_BOOK,
                    ),
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
            stateFlow.value.continueSeriesMenu.dismissed()?.bookId shouldBe 2
        }

        @Test
        fun `stays closed and records nothing when no menu is open`() = runTest {
            // ----- Arrange -----
            val action = OnContinueSeriesDismissalChosenAction(
                bookId = 7,
                kind = ContinueSeriesDismissalKind.THIS_BOOK,
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.continueSeriesMenu shouldBe SheetSession.Closed
            stateFlow.value.continueSeriesMenu.dismissed().shouldBeNull()
        }
    }
}
