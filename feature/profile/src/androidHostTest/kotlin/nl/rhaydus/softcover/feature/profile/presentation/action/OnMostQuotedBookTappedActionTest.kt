package nl.rhaydus.softcover.feature.profile.presentation.action

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.Quote
import nl.rhaydus.softcover.core.domain.model.StatsScope
import nl.rhaydus.softcover.core.profile.domain.model.MostQuotedBook
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

class OnMostQuotedBookTappedActionTest {
    private lateinit var dependencies: StatsAtlasDependencies
    private lateinit var stateFlow: MutableStateFlow<StatsAtlasUiState>
    private lateinit var eventChannel: Channel<StatsAtlasEvent>
    private lateinit var scope: ActionScope<StatsAtlasUiState, StatsAtlasEvent, StatsAtlasLocalVariables>

    private val loaded: InYourWordsUiModel = inYourWordsUiModel(
        written = WrittenStats(
            reviewCount = 0,
            reviewWordCount = 0,
            quoteCount = 2,
            quotedBookCount = 1,
            mostQuoted = MostQuotedBook(
                book = mockk<Book> {
                    every {
                        id
                    } returns 42

                    every {
                        title
                    } returns "The Dispossessed"

                    every {
                        primaryAuthors
                    } returns emptyList()

                    every {
                        currentEdition
                    } returns null

                    every {
                        defaultEdition
                    } returns null

                    every {
                        coverUrl
                    } returns "https://example.com/cover.jpg"
                },
                quoteCount = 2,
                firstQuote = mockk<Quote> {
                    every {
                        text
                    } returns "A line."
                },
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
        fun `opens the most-quoted book's detail`() = runTest {
            // ----- Arrange -----
            stateFlow.value = StatsAtlasUiState(inYourWords = loaded)

            // ----- Act -----
            OnMostQuotedBookTappedAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            val event = eventChannel.tryReceive().getOrNull()
                .shouldNotBeNull()
                .shouldBeInstanceOf<StatsAtlasEvent.OpenBookDetail>()

            event.bookId shouldBe loaded.mostQuoted.shouldNotBeNull().bookId
        }

        @Test
        fun `sends nothing without a most-quoted book`() = runTest {
            // ----- Arrange -----
            stateFlow.value = StatsAtlasUiState(inYourWords = loaded.copy(mostQuoted = null))

            // ----- Act -----
            OnMostQuotedBookTappedAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            eventChannel.tryReceive().getOrNull().shouldBeNull()
        }

        @Test
        fun `sends nothing while the card is a loading stand-in`() = runTest {
            // ----- Arrange -----
            stateFlow.value = StatsAtlasUiState(inYourWords = loaded.copy(isLoading = true))

            // ----- Act -----
            OnMostQuotedBookTappedAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            eventChannel.tryReceive().getOrNull().shouldBeNull()
        }
    }
}
