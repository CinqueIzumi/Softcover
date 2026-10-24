package nl.rhaydus.softcover.feature.profile.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.StatsScope
import nl.rhaydus.softcover.feature.profile.presentation.event.StatsAtlasEvent
import nl.rhaydus.softcover.feature.profile.presentation.mapper.ALL_TIME_SCOPE_KEY
import nl.rhaydus.softcover.feature.profile.presentation.mapper.atlasScopeChips
import nl.rhaydus.softcover.feature.profile.presentation.mapper.statsScopeKey
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.StatsAtlasDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasLocalVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnScopeSelectedActionTest {
    private lateinit var dependencies: StatsAtlasDependencies
    private lateinit var stateFlow: MutableStateFlow<StatsAtlasUiState>
    private lateinit var localVariablesFlow: MutableStateFlow<StatsAtlasLocalVariables>
    private lateinit var scope: ActionScope<StatsAtlasUiState, StatsAtlasEvent, StatsAtlasLocalVariables>

    @BeforeEach
    fun setUp() {
        dependencies = mockk(relaxed = true)
        stateFlow = MutableStateFlow(StatsAtlasUiState())
        localVariablesFlow = MutableStateFlow(StatsAtlasLocalVariables())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = localVariablesFlow,
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    @Nested
    inner class Execute {
        @Test
        fun `selects the tapped scope`() = runTest {
            // ----- Arrange -----
            val key = statsScopeKey(StatsScope.CalendarYear(2025))
            stateFlow.value = StatsAtlasUiState(
                selectedScopeKey = ALL_TIME_SCOPE_KEY,
                scopeChips = atlasScopeChips(
                    yearsRead = listOf(2025),
                    selectedKey = ALL_TIME_SCOPE_KEY,
                ),
            )
            val action = OnScopeSelectedAction(statsScope = StatsScope.CalendarYear(2025))

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.selectedScopeKey shouldBe key
        }

        @Test
        fun `records that the reader picked a scope`() = runTest {
            // ----- Arrange -----
            val action = OnScopeSelectedAction(statsScope = StatsScope.AllTime)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            localVariablesFlow.value.hasUserPickedScope shouldBe true
        }
    }
}
