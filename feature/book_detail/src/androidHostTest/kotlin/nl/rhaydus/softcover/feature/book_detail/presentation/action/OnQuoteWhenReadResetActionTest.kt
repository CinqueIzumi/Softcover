package nl.rhaydus.softcover.feature.book_detail.presentation.action

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import nl.rhaydus.softcover.core.component.quote.QuotePositionTab
import nl.rhaydus.softcover.core.domain.model.PrivacySetting
import nl.rhaydus.softcover.core.progresssheet.presentation.ProgressSheetState
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

class OnQuoteWhenReadResetActionTest {
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
        fun `clears the quote draft's backdate without touching the progress sheet's`() = runTest {
            // ----- Arrange -----
            val progressReadAt = LocalDateTime(
                2019,
                6,
                1,
                8,
                0,
            )
            stateFlow.value = buildBookDetailUiState().copy(
                progressSheet = ProgressSheetState(readAt = progressReadAt),
                quoteDraft = QuoteDraft(
                    tab = QuotePositionTab.PERCENTAGE,
                    privacy = PrivacySetting.PUBLIC,
                    readAt = LocalDateTime(
                        2020,
                        1,
                        2,
                        9,
                        15,
                    ),
                ),
            )
            val action = OnQuoteWhenReadResetAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.quoteDraft.shouldNotBeNull().readAt shouldBe null
            stateFlow.value.progressSheet.readAt shouldBe progressReadAt
        }
    }
}
