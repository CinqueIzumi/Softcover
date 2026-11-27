package nl.rhaydus.softcover.feature.book_detail.presentation.action

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetCurrenciesUseCase
import nl.rhaydus.softcover.feature.book_detail.presentation.EURO
import nl.rhaydus.softcover.feature.book_detail.presentation.TEST_CURRENCIES
import nl.rhaydus.softcover.feature.book_detail.presentation.YEN
import nl.rhaydus.softcover.feature.book_detail.presentation.buildBookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.buildWhereYouGotItDraft
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.state.WhereYouGotItBody
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnWhereYouGotItCurrencyPickedActionTest {
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
        fun `switches the currency and returns to the choices`() = runTest {
            // ----- Arrange -----
            stateFlow.value = stateFlow.value.copy(
                whereYouGotItDraft = buildWhereYouGotItDraft(body = WhereYouGotItBody.Currency(query = "yen")),
            )

            // ----- Act -----
            OnWhereYouGotItCurrencyPickedAction(code = YEN.code).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.whereYouGotItDraft shouldBe buildWhereYouGotItDraft(currencyCode = YEN.code)
        }

        @Test
        fun `re-checks the typed amount against the picked currency`() = runTest {
            // ----- Arrange -----
            stateFlow.value = stateFlow.value.copy(
                whereYouGotItDraft = buildWhereYouGotItDraft(
                    amountText = "14.50",
                    currencyCode = EURO.code,
                    body = WhereYouGotItBody.Currency(query = ""),
                ),
            )

            // ----- Act -----
            OnWhereYouGotItCurrencyPickedAction(code = YEN.code).execute(
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
            OnWhereYouGotItCurrencyPickedAction(code = YEN.code).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.whereYouGotItDraft.shouldBeNull()
        }
    }
}
