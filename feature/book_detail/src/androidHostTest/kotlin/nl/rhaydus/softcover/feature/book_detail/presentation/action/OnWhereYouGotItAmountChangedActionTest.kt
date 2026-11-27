package nl.rhaydus.softcover.feature.book_detail.presentation.action

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetCurrenciesUseCase
import nl.rhaydus.softcover.feature.book_detail.presentation.TEST_CURRENCIES
import nl.rhaydus.softcover.feature.book_detail.presentation.YEN
import nl.rhaydus.softcover.feature.book_detail.presentation.buildBookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.buildWhereYouGotItDraft
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnWhereYouGotItAmountChangedActionTest {
    private lateinit var dependencies: BookDetailDependencies
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        stateFlow = MutableStateFlow(buildBookDetailUiState())

        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(BookDetailLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        val getCurrenciesUseCase = mockk<GetCurrenciesUseCase>().also {
            every {
                it()
            } returns TEST_CURRENCIES
        }

        dependencies = mockk<BookDetailDependencies>(relaxed = true).also {
            every {
                it.getCurrenciesUseCase
            } returns getCurrenciesUseCase
        }
    }

    @Nested
    inner class Execute {
        @Test
        fun `stores the typed text without an error when it is valid`() = runTest {
            // ----- Arrange -----
            stateFlow.value = stateFlow.value.copy(whereYouGotItDraft = buildWhereYouGotItDraft())

            // ----- Act -----
            OnWhereYouGotItAmountChangedAction(text = "14,50").execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.whereYouGotItDraft shouldBe buildWhereYouGotItDraft(amountText = "14,50")
        }

        @Test
        fun `flags text the drafted currency cannot hold`() = runTest {
            // ----- Arrange -----
            stateFlow.value = stateFlow.value.copy(whereYouGotItDraft = buildWhereYouGotItDraft(currencyCode = YEN.code))

            // ----- Act -----
            OnWhereYouGotItAmountChangedAction(text = "14.50").execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.whereYouGotItDraft shouldBe buildWhereYouGotItDraft(
                amountText = "14.50",
                currencyCode = YEN.code,
                amountError = "Enter a price like 1450.",
            )
        }

        @Test
        fun `leaves the state without a draft untouched`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState()

            // ----- Act -----
            OnWhereYouGotItAmountChangedAction(text = "14").execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.whereYouGotItDraft.shouldBeNull()
        }
    }
}
