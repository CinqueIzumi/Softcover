package nl.rhaydus.softcover.feature.profile.presentation.action

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.component.sheet.SheetSession
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

class OnDayDetailSheetDismissedActionTest {
    private lateinit var dependencies: StatsAtlasDependencies
    private lateinit var stateFlow: MutableStateFlow<StatsAtlasUiState>
    private lateinit var eventChannel: Channel<StatsAtlasEvent>
    private lateinit var scope: ActionScope<StatsAtlasUiState, StatsAtlasEvent, StatsAtlasLocalVariables>

    private val model = DayDetailUiModel.previews.getValue("Two books, with update times")

    private val day = LocalDate(
        year = 2026,
        month = 4,
        day = 14,
    )

    @BeforeEach
    fun setUp() {
        dependencies = mockk(relaxed = true)
        stateFlow = MutableStateFlow(StatsAtlasUiState())
        eventChannel = Channel(Channel.BUFFERED)
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(StatsAtlasLocalVariables()),
            eventChannel = eventChannel,
        )
    }

    @Nested
    inner class Execute {
        @Test
        fun `clears the day and opens the pending book`() = runTest {
            // ----- Arrange -----
            stateFlow.value = StatsAtlasUiState(
                selectedReadingDay = day,
                dayDetailSheet = SheetSession.Open(
                    model = model,
                    pending = DayDetailBook(
                        bookId = 7,
                        initialCover = null,
                    ),
                ),
            )

            // ----- Act -----
            OnDayDetailSheetDismissedAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.dayDetailSheet shouldBe SheetSession.Closed
            stateFlow.value.selectedReadingDay.shouldBeNull()
            val event = eventChannel.tryReceive().getOrNull().shouldNotBeNull()
            event.shouldBeInstanceOf<StatsAtlasEvent.OpenBookDetail>().bookId shouldBe 7
            eventChannel.tryReceive().getOrNull().shouldBeNull()
        }

        @Test
        fun `clears the day without an event when no book was pending`() = runTest {
            // ----- Arrange -----
            stateFlow.value = StatsAtlasUiState(
                selectedReadingDay = day,
                dayDetailSheet = SheetSession.Open(model = model),
            )

            // ----- Act -----
            OnDayDetailSheetDismissedAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.dayDetailSheet shouldBe SheetSession.Closed
            stateFlow.value.selectedReadingDay.shouldBeNull()
            eventChannel.tryReceive().getOrNull().shouldBeNull()
        }
    }
}
