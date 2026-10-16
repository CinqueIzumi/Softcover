package nl.rhaydus.softcover.feature.book_detail.presentation.action

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.quote.QuotePositionTab
import nl.rhaydus.softcover.core.domain.model.PrivacySetting
import nl.rhaydus.softcover.feature.book_detail.presentation.buildBookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.state.QuoteDraft
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnQuotePositionTabSelectedActionTest {
    private lateinit var dependencies: BookDetailDependencies
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        dependencies = mockk(relaxed = true)
        stateFlow = MutableStateFlow(buildBookDetailUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(BookDetailLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    @Nested
    inner class Execute {
        @Test
        fun `switches the tab and clears every position field but keeps the quote text`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(
                quoteDraft = QuoteDraft(
                    tab = QuotePositionTab.PAGE,
                    privacy = PrivacySetting.PUBLIC,
                    text = "A line",
                    positionText = "214",
                    hours = "1",
                    minutes = "2",
                    seconds = "3",
                ),
            )
            val action = OnQuotePositionTabSelectedAction(tab = QuotePositionTab.PERCENTAGE)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            val draft = stateFlow.value.quoteDraft.shouldNotBeNull()
            draft.tab shouldBe QuotePositionTab.PERCENTAGE
            draft.positionText shouldBe ""
            draft.hours shouldBe ""
            draft.minutes shouldBe ""
            draft.seconds shouldBe ""
            draft.text shouldBe "A line"
        }
    }
}
