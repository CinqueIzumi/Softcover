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

class OnQuoteWhenReadShiftedActionTest {
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

    private fun draft(readAt: LocalDateTime?): QuoteDraft = QuoteDraft(
        tab = QuotePositionTab.PERCENTAGE,
        privacy = PrivacySetting.PUBLIC,
        readAt = readAt,
    )

    @Nested
    inner class Execute {
        @Test
        fun `moves the quote draft's backdate by the given minutes`() = runTest {
            // ----- Arrange -----
            val readAt = LocalDateTime(
                2020,
                1,
                2,
                9,
                15,
            )
            stateFlow.value = buildBookDetailUiState().copy(quoteDraft = draft(readAt = readAt))
            val action = OnQuoteWhenReadShiftedAction(minutes = 30)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.quoteDraft.shouldNotBeNull().readAt shouldBe LocalDateTime(
                2020,
                1,
                2,
                9,
                45,
            )
        }

        @Test
        fun `never touches the progress sheet's backdate`() = runTest {
            // ----- Arrange -----
            val progressReadAt = LocalDateTime(
                2019,
                6,
                1,
                8,
                0,
            )
            stateFlow.value = buildBookDetailUiState().copy(
                readAt = progressReadAt,
                quoteDraft = draft(
                    readAt = LocalDateTime(
                        2020,
                        1,
                        2,
                        9,
                        15,
                    ),
                ),
            )
            val action = OnQuoteWhenReadShiftedAction(minutes = -30)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.readAt shouldBe progressReadAt
        }
    }
}
