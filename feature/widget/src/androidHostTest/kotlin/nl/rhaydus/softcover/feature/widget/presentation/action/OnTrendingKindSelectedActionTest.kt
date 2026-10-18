package nl.rhaydus.softcover.feature.widget.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.TrendingPeriod
import nl.rhaydus.softcover.core.domain.model.UpcomingWindow
import nl.rhaydus.softcover.feature.widget.domain.model.TrendingFeed
import nl.rhaydus.softcover.feature.widget.domain.model.TrendingFeedKind
import nl.rhaydus.softcover.feature.widget.presentation.event.TrendingSettingsEvent
import nl.rhaydus.softcover.feature.widget.presentation.screenmodel.TrendingSettingsDependencies
import nl.rhaydus.softcover.feature.widget.presentation.state.TrendingSettingsLocalVariables
import nl.rhaydus.softcover.feature.widget.presentation.state.TrendingSettingsUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnTrendingKindSelectedActionTest {
    private val eventChannel = Channel<TrendingSettingsEvent>(Channel.BUFFERED)
    private lateinit var dependencies: TrendingSettingsDependencies
    private lateinit var localVariablesFlow: MutableStateFlow<TrendingSettingsLocalVariables>
    private lateinit var stateFlow: MutableStateFlow<TrendingSettingsUiState>
    private lateinit var scope: ActionScope<TrendingSettingsUiState, TrendingSettingsEvent, TrendingSettingsLocalVariables>

    @BeforeEach
    fun setUp() {
        dependencies = mockk(relaxed = true)
        stateFlow = MutableStateFlow(TrendingSettingsUiState())
        localVariablesFlow = MutableStateFlow(TrendingSettingsLocalVariables())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = localVariablesFlow,
            eventChannel = eventChannel,
        )
    }

    private fun selectedFeedChips(): List<TrendingFeed?> = stateFlow.value.periodChips.let { set ->
        set.chips.filter { it.selected }.map { set[it.key] }
    }

    @Nested
    inner class Execute {
        @Test
        fun `switching to upcoming for the first time selects its first window`() = runTest {
            // ----- Arrange -----
            val action = OnTrendingKindSelectedAction(kind = TrendingFeedKind.Upcoming)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            val first = TrendingFeed.Upcoming(window = UpcomingWindow.entries.first())
            localVariablesFlow.value.feed shouldBe first
            selectedFeedChips() shouldBe listOf(first)
        }

        @Test
        fun `switching back to trending restores the period chosen there`() = runTest {
            // ----- Arrange -----
            val chosen = TrendingFeed.Trending(period = TrendingPeriod.Year)
            OnTrendingFeedSelectedAction(feed = chosen).execute(
                dependencies = dependencies,
                scope = scope,
            )
            OnTrendingKindSelectedAction(kind = TrendingFeedKind.Upcoming).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Act -----
            OnTrendingKindSelectedAction(kind = TrendingFeedKind.Trending).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            localVariablesFlow.value.feed shouldBe chosen
            selectedFeedChips() shouldBe listOf(chosen)
        }

        @Test
        fun `switching back to upcoming restores the window chosen there`() = runTest {
            // ----- Arrange -----
            val chosen = TrendingFeed.Upcoming(window = UpcomingWindow.SixMonths)
            OnTrendingKindSelectedAction(kind = TrendingFeedKind.Upcoming).execute(
                dependencies = dependencies,
                scope = scope,
            )
            OnTrendingFeedSelectedAction(feed = chosen).execute(
                dependencies = dependencies,
                scope = scope,
            )
            OnTrendingKindSelectedAction(kind = TrendingFeedKind.Trending).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Act -----
            OnTrendingKindSelectedAction(kind = TrendingFeedKind.Upcoming).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            localVariablesFlow.value.feed shouldBe chosen
            selectedFeedChips() shouldBe listOf(chosen)
        }

        @Test
        fun `switching the kind selects that kind in the Show chips`() = runTest {
            // ----- Arrange -----
            val action = OnTrendingKindSelectedAction(kind = TrendingFeedKind.Upcoming)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            val kinds = stateFlow.value.kindChips
            kinds.chips.filter { it.selected }.map { kinds[it.key] } shouldBe listOf(TrendingFeedKind.Upcoming)
        }
    }
}
