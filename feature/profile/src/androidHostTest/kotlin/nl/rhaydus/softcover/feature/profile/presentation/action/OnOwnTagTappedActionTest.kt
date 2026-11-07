package nl.rhaydus.softcover.feature.profile.presentation.action

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.StatsScope
import nl.rhaydus.softcover.core.profile.domain.model.OwnTagCount
import nl.rhaydus.softcover.core.profile.domain.model.WrittenStats
import nl.rhaydus.softcover.feature.profile.presentation.event.StatsAtlasEvent
import nl.rhaydus.softcover.feature.profile.presentation.mapper.inYourWordsUiModel
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.StatsAtlasDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.InYourWordsUiModel
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasLocalVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnOwnTagTappedActionTest {
    private lateinit var dependencies: StatsAtlasDependencies
    private lateinit var stateFlow: MutableStateFlow<StatsAtlasUiState>
    private lateinit var eventChannel: Channel<StatsAtlasEvent>
    private lateinit var scope: ActionScope<StatsAtlasUiState, StatsAtlasEvent, StatsAtlasLocalVariables>

    private val loaded: InYourWordsUiModel = inYourWordsUiModel(
        written = WrittenStats(
            reviewCount = 0,
            reviewWordCount = 0,
            quoteCount = 0,
            quotedBookCount = 0,
            mostQuoted = null,
        ),
        ownTags = listOf(
            OwnTagCount(
                name = "cozy",
                bookCount = 4,
            ),
            OwnTagCount(
                name = "slow burn",
                bookCount = 2,
            ),
        ),
        scope = StatsScope.AllTime,
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
        fun `opens the library for a shown tag`() = runTest {
            // ----- Arrange -----
            stateFlow.value = StatsAtlasUiState(inYourWords = loaded)

            // ----- Act -----
            OnOwnTagTappedAction(tagName = "slow burn").execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            val event = eventChannel.tryReceive().getOrNull()
                .shouldNotBeNull()
                .shouldBeInstanceOf<StatsAtlasEvent.OpenLibraryForTag>()

            event.tagName shouldBe "slow burn"
        }

        @Test
        fun `sends nothing while the rows are loading stand-ins`() = runTest {
            // ----- Arrange -----
            stateFlow.value = StatsAtlasUiState(inYourWords = loaded.copy(isLoading = true))

            // ----- Act -----
            OnOwnTagTappedAction(tagName = "cozy").execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            eventChannel.tryReceive().getOrNull().shouldBeNull()
        }

        @Test
        fun `sends nothing without an own tags block`() = runTest {
            // ----- Arrange -----
            stateFlow.value = StatsAtlasUiState(inYourWords = loaded.copy(ownTags = null))

            // ----- Act -----
            OnOwnTagTappedAction(tagName = "cozy").execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            eventChannel.tryReceive().getOrNull().shouldBeNull()
        }

        @Test
        fun `sends nothing for a tag the ranking does not hold`() = runTest {
            // ----- Arrange -----
            stateFlow.value = StatsAtlasUiState(inYourWords = loaded)

            // ----- Act -----
            OnOwnTagTappedAction(tagName = "unknown").execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            eventChannel.tryReceive().getOrNull().shouldBeNull()
        }
    }
}
