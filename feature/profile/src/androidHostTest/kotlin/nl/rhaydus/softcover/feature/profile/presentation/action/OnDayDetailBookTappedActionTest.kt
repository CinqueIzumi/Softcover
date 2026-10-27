package nl.rhaydus.softcover.feature.profile.presentation.action

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.core.component.sheet.isClosing
import nl.rhaydus.softcover.feature.profile.presentation.event.StatsAtlasEvent
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.StatsAtlasDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.DayDetailBook
import nl.rhaydus.softcover.feature.profile.presentation.state.DayDetailUiModel
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasLocalVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnDayDetailBookTappedActionTest {
    private lateinit var dependencies: StatsAtlasDependencies
    private lateinit var stateFlow: MutableStateFlow<StatsAtlasUiState>
    private lateinit var scope: ActionScope<StatsAtlasUiState, StatsAtlasEvent, StatsAtlasLocalVariables>

    private val model = DayDetailUiModel.previews.getValue("Two books, with update times")

    @BeforeEach
    fun setUp() {
        dependencies = mockk(relaxed = true)
        stateFlow = MutableStateFlow(StatsAtlasUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(StatsAtlasLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    @Nested
    inner class Execute {
        @Test
        fun `requests the sheet to close with the tapped book pending`() = runTest {
            // ----- Arrange -----
            val book = DayDetailBook(
                bookId = 7,
                initialCover = null,
            )
            stateFlow.value = StatsAtlasUiState(dayDetailSheet = SheetSession.Open(model = model))

            // ----- Act -----
            OnDayDetailBookTappedAction(book).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            val sheet = stateFlow.value.dayDetailSheet
            sheet.isClosing shouldBe true
            sheet.shouldBeInstanceOf<SheetSession.Open<DayDetailUiModel, DayDetailBook>>().pending shouldBe book
        }

        @Test
        fun `leaves a closed sheet closed`() = runTest {
            // ----- Arrange -----
            val book = DayDetailBook(
                bookId = 7,
                initialCover = null,
            )

            // ----- Act -----
            OnDayDetailBookTappedAction(book).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.dayDetailSheet shouldBe SheetSession.Closed
        }
    }
}
