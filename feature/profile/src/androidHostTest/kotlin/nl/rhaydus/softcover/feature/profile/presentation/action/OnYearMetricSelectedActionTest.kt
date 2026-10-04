package nl.rhaydus.softcover.feature.profile.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.feature.profile.presentation.event.ProfileEvent
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.ProfileDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.LocalProfileVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.ProfileUiState
import nl.rhaydus.softcover.feature.profile.presentation.state.YearMetric
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnYearMetricSelectedActionTest {
    private lateinit var dependencies: ProfileDependencies
    private lateinit var stateFlow: MutableStateFlow<ProfileUiState>
    private lateinit var scope: ActionScope<ProfileUiState, ProfileEvent, LocalProfileVariables>

    @BeforeEach
    fun setUp() {
        dependencies = mockk(relaxed = true)
        stateFlow = MutableStateFlow(ProfileUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LocalProfileVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    @Nested
    inner class Execute {
        @Test
        fun `sets the selected year metric`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ProfileUiState(selectedYearMetric = YearMetric.BOOKS)
            val action = OnYearMetricSelectedAction(metric = YearMetric.PAGES)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.selectedYearMetric shouldBe YearMetric.PAGES
        }

        @Test
        fun `selecting the current metric keeps it`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ProfileUiState(selectedYearMetric = YearMetric.PAGES)
            val action = OnYearMetricSelectedAction(metric = YearMetric.PAGES)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.selectedYearMetric shouldBe YearMetric.PAGES
        }

        @Test
        fun `leaves the rest of the state untouched`() = runTest {
            // ----- Arrange -----
            val initialState = ProfileUiState(selectedYearMetric = YearMetric.BOOKS)
            stateFlow.value = initialState
            val action = OnYearMetricSelectedAction(metric = YearMetric.PAGES)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value shouldBe initialState.copy(selectedYearMetric = YearMetric.PAGES)
        }
    }
}
