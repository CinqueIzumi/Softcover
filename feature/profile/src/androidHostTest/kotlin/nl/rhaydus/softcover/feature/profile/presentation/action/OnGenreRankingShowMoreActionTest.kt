package nl.rhaydus.softcover.feature.profile.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.feature.profile.presentation.event.StatsAtlasEvent
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.StatsAtlasDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasLocalVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnGenreRankingShowMoreActionTest {
    private lateinit var dependencies: StatsAtlasDependencies
    private lateinit var stateFlow: MutableStateFlow<StatsAtlasUiState>
    private lateinit var scope: ActionScope<StatsAtlasUiState, StatsAtlasEvent, StatsAtlasLocalVariables>

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
        fun `reveals ten more genres`() = runTest {
            // ----- Arrange -----
            stateFlow.value = StatsAtlasUiState(genreRankingShownCount = 5)
            val action = OnGenreRankingShowMoreAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.genreRankingShownCount shouldBe 15
        }

        @Test
        fun `repeated reveals stack`() = runTest {
            // ----- Arrange -----
            stateFlow.value = StatsAtlasUiState(genreRankingShownCount = 5)
            val action = OnGenreRankingShowMoreAction()

            // ----- Act -----
            repeat(2) {
                action.execute(
                    dependencies = dependencies,
                    scope = scope,
                )
            }

            // ----- Assert -----
            stateFlow.value.genreRankingShownCount shouldBe 25
        }
    }
}
