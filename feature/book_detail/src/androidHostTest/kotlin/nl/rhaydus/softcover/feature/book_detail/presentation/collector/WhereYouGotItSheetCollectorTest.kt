package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.sheet.isClosing
import nl.rhaydus.softcover.core.component.sheet.isOpen
import nl.rhaydus.softcover.core.component.sheet.modelOrNull
import nl.rhaydus.softcover.core.domain.model.AcquisitionKind
import nl.rhaydus.softcover.core.domain.model.Currency
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetCurrenciesUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetUsualCurrencyAsFlowUseCase
import nl.rhaydus.softcover.feature.book_detail.presentation.DINAR
import nl.rhaydus.softcover.feature.book_detail.presentation.EURO
import nl.rhaydus.softcover.feature.book_detail.presentation.TEST_CURRENCIES
import nl.rhaydus.softcover.feature.book_detail.presentation.YEN
import nl.rhaydus.softcover.feature.book_detail.presentation.buildBookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.buildQuoteBook
import nl.rhaydus.softcover.feature.book_detail.presentation.buildWhereYouGotItDraft
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.toKey
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.toWhereYouGotItSheet
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.state.WhereYouGotItBody
import nl.rhaydus.softcover.feature.book_detail.presentation.state.WhereYouGotItSource
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class WhereYouGotItSheetCollectorTest {
    private lateinit var usualCurrency: MutableStateFlow<Currency>
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

        usualCurrency = MutableStateFlow(EURO)

        val getCurrenciesUseCase = mockk<GetCurrenciesUseCase>().also {
            every {
                it()
            } returns TEST_CURRENCIES
        }

        val getUsualCurrencyAsFlowUseCase = mockk<GetUsualCurrencyAsFlowUseCase>().also {
            every {
                it()
            } returns usualCurrency
        }

        dependencies = mockk<BookDetailDependencies>(relaxed = true).also {
            every {
                it.getCurrenciesUseCase
            } returns getCurrenciesUseCase

            every {
                it.getUsualCurrencyAsFlowUseCase
            } returns getUsualCurrencyAsFlowUseCase
        }
    }

    private fun TestScope.launchCollector() {
        backgroundScope.launch {
            WhereYouGotItSheetCollector().onLaunch(
                scope = scope,
                dependencies = dependencies,
            )
        }
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `opens the sheet with a model of the draft while a draft exists`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val draft = buildWhereYouGotItDraft(source = WhereYouGotItSource.Preset(key = AcquisitionKind.GIFT.toKey()))

            stateFlow.value = buildBookDetailUiState().copy(
                book = buildQuoteBook(title = "Piranesi"),
                whereYouGotItDraft = draft,
            )

            // ----- Act -----
            launchCollector()

            // ----- Assert -----
            stateFlow.value.whereYouGotItSheet.modelOrNull shouldBe draft.toWhereYouGotItSheet(
                bookTitle = "Piranesi",
                currencies = TEST_CURRENCIES,
                usualCurrencyCode = EURO.code,
            )
        }

        @Test
        fun `follows the draft as the reader edits it`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(
                book = buildQuoteBook(title = "Piranesi"),
                whereYouGotItDraft = buildWhereYouGotItDraft(),
            )

            launchCollector()
            val edited = buildWhereYouGotItDraft(source = WhereYouGotItSource.Custom(text = "A street stall"))

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(whereYouGotItDraft = edited)

            // ----- Assert -----
            stateFlow.value.whereYouGotItSheet.modelOrNull shouldBe edited.toWhereYouGotItSheet(
                bookTitle = "Piranesi",
                currencies = TEST_CURRENCIES,
                usualCurrencyCode = EURO.code,
            )
        }

        @Test
        fun `asks the sheet to hide once the draft is cleared`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(
                book = buildQuoteBook(title = "Piranesi"),
                whereYouGotItDraft = buildWhereYouGotItDraft(),
            )

            launchCollector()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(whereYouGotItDraft = null)

            // ----- Assert -----
            stateFlow.value.whereYouGotItSheet.isOpen shouldBe true
            stateFlow.value.whereYouGotItSheet.isClosing shouldBe true
        }

        @Test
        fun `keeps the sheet closed while the book has not loaded`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(whereYouGotItDraft = buildWhereYouGotItDraft())

            // ----- Act -----
            launchCollector()

            // ----- Assert -----
            stateFlow.value.whereYouGotItSheet.isOpen shouldBe false
        }

        @Test
        fun `follows the draft into the currency picker and back`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(
                book = buildQuoteBook(title = "Piranesi"),
                whereYouGotItDraft = buildWhereYouGotItDraft(),
            )

            launchCollector()
            val picking = buildWhereYouGotItDraft(body = WhereYouGotItBody.Currency(query = "yen"))

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(whereYouGotItDraft = picking)

            // ----- Assert -----
            stateFlow.value.whereYouGotItSheet.modelOrNull shouldBe picking.toWhereYouGotItSheet(
                bookTitle = "Piranesi",
                currencies = TEST_CURRENCIES,
                usualCurrencyCode = EURO.code,
            )
        }

        @Test
        fun `reorders the currency rows when the usual currency changes`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val picking = buildWhereYouGotItDraft(
                currencyCode = DINAR.code,
                body = WhereYouGotItBody.Currency(query = ""),
            )

            stateFlow.value = buildBookDetailUiState().copy(
                book = buildQuoteBook(title = "Piranesi"),
                whereYouGotItDraft = picking,
            )

            launchCollector()

            // ----- Act -----
            usualCurrency.value = YEN

            // ----- Assert -----
            stateFlow.value.whereYouGotItSheet.modelOrNull shouldBe picking.toWhereYouGotItSheet(
                bookTitle = "Piranesi",
                currencies = TEST_CURRENCIES,
                usualCurrencyCode = YEN.code,
            )
        }
    }
}
