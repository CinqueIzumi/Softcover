package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import nl.rhaydus.softcover.core.component.sheet.SheetFooterUiModel
import nl.rhaydus.softcover.core.domain.model.AcquisitionKind
import nl.rhaydus.softcover.feature.book_detail.presentation.DINAR
import nl.rhaydus.softcover.feature.book_detail.presentation.EURO
import nl.rhaydus.softcover.feature.book_detail.presentation.TEST_CURRENCIES
import nl.rhaydus.softcover.feature.book_detail.presentation.YEN
import nl.rhaydus.softcover.feature.book_detail.presentation.buildWhereYouGotItDraft
import nl.rhaydus.softcover.feature.book_detail.presentation.state.WhereYouGotItBody
import nl.rhaydus.softcover.feature.book_detail.presentation.state.WhereYouGotItBodyUiModel
import nl.rhaydus.softcover.feature.book_detail.presentation.state.WhereYouGotItDraft
import nl.rhaydus.softcover.feature.book_detail.presentation.state.WhereYouGotItSheetUiModel
import nl.rhaydus.softcover.feature.book_detail.presentation.state.WhereYouGotItSource
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class WhereYouGotItSheetMapperTest {
    private fun WhereYouGotItDraft.sheet(usualCurrencyCode: String? = EURO.code): WhereYouGotItSheetUiModel = toWhereYouGotItSheet(
        bookTitle = "Piranesi",
        currencies = TEST_CURRENCIES,
        usualCurrencyCode = usualCurrencyCode,
    )

    private fun WhereYouGotItSheetUiModel.choices(): WhereYouGotItBodyUiModel.Choices =
        body.shouldBeInstanceOf<WhereYouGotItBodyUiModel.Choices>()

    @Nested
    inner class ChoicesBody {
        @Test
        fun `selects only the drafted preset row and leaves the input unticked`() {
            // ----- Arrange -----
            val draft = buildWhereYouGotItDraft(source = WhereYouGotItSource.Preset(key = AcquisitionKind.GIFT.toKey()))

            // ----- Act -----
            val group = draft.sheet().choices().group

            // ----- Assert -----
            group.rows.filter { it.selected }.map { it.key } shouldBe listOf(AcquisitionKind.GIFT.toKey())
            group.input?.selected shouldBe false
            group.input?.field?.text shouldBe ""
        }

        @Test
        fun `selects the input row and carries its text for custom text`() {
            // ----- Arrange -----
            val draft = buildWhereYouGotItDraft(source = WhereYouGotItSource.Custom(text = "A street stall"))

            // ----- Act -----
            val group = draft.sheet().choices().group

            // ----- Assert -----
            group.rows.filter { it.selected }.shouldBeEmpty()
            group.input?.selected shouldBe true
            group.input?.field?.text shouldBe "A street stall"
        }

        @Test
        fun `selects nothing for none`() {
            // ----- Arrange -----
            val draft = buildWhereYouGotItDraft()

            // ----- Act -----
            val group = draft.sheet().choices().group

            // ----- Assert -----
            group.rows.filter { it.selected }.shouldBeEmpty()
            group.input?.selected shouldBe false
        }

        @Test
        fun `offers a row for every acquisition kind`() {
            // ----- Arrange -----
            val draft = buildWhereYouGotItDraft()

            // ----- Act -----
            val group = draft.sheet().choices().group

            // ----- Assert -----
            group.rows.map { it.key } shouldBe AcquisitionKind.entries.map { it.toKey() }
        }

        @Test
        fun `carries the typed amount and the drafted currency code`() {
            // ----- Arrange -----
            val draft = buildWhereYouGotItDraft(
                amountText = "14,5",
                currencyCode = DINAR.code,
            )

            // ----- Act -----
            val choices = draft.sheet().choices()

            // ----- Assert -----
            choices.amount.text shouldBe "14,5"
            choices.currency.value shouldBe DINAR.code
        }

        @Test
        fun `shows no amount error while the amount is valid`() {
            // ----- Arrange -----
            val draft = buildWhereYouGotItDraft(amountText = "14.50")

            // ----- Act -----
            val choices = draft.sheet().choices()

            // ----- Assert -----
            choices.amount.error.shouldBeNull()
        }

        @Test
        fun `shows the amount error as a non-shaking message`() {
            // ----- Arrange -----
            val draft = buildWhereYouGotItDraft(
                amountText = "abc",
                amountError = "Enter a price like 14.50.",
            )

            // ----- Act -----
            val error = draft.sheet().choices().amount.error

            // ----- Assert -----
            error.shouldNotBeNull().message shouldBe "Enter a price like 14.50."
            error.shaking shouldBe false
        }

        @Test
        fun `sizes the amount placeholder to the currency's fraction digits`() {
            // ----- Arrange -----
            val euro = buildWhereYouGotItDraft(currencyCode = EURO.code)
            val yen = buildWhereYouGotItDraft(currencyCode = YEN.code)
            val dinar = buildWhereYouGotItDraft(currencyCode = DINAR.code)

            // ----- Act -----
            val placeholders = listOf(euro, yen, dinar).map { it.sheet().choices().amount.placeholder }

            // ----- Assert -----
            placeholders shouldBe listOf("0.00", "0", "0.000")
        }
    }

    @Nested
    inner class Footer {
        @Test
        fun `enables Save while the amount has no error`() {
            // ----- Arrange -----
            val draft = buildWhereYouGotItDraft(amountText = "14.50")

            // ----- Act -----
            val footer = draft.sheet().scaffold.footer

            // ----- Assert -----
            footer.shouldBeInstanceOf<SheetFooterUiModel.Inline>().primary.enabled shouldBe true
        }

        @Test
        fun `disables Save while the amount has an error`() {
            // ----- Arrange -----
            val draft = buildWhereYouGotItDraft(
                amountText = "abc",
                amountError = "Enter a price like 14.50.",
            )

            // ----- Act -----
            val footer = draft.sheet().scaffold.footer

            // ----- Assert -----
            footer.shouldBeInstanceOf<SheetFooterUiModel.Inline>().primary.enabled shouldBe false
        }

        @Test
        fun `keeps Cancel enabled while the amount has an error`() {
            // ----- Arrange -----
            val draft = buildWhereYouGotItDraft(amountError = "Enter a price like 14.50.")

            // ----- Act -----
            val footer = draft.sheet().scaffold.footer

            // ----- Assert -----
            footer.shouldBeInstanceOf<SheetFooterUiModel.Inline>().secondary.enabled shouldBe true
        }
    }

    @Nested
    inner class CurrencyBody {
        @Test
        fun `lists every currency with the drafted one selected and first`() {
            // ----- Arrange -----
            val draft = buildWhereYouGotItDraft(
                currencyCode = YEN.code,
                body = WhereYouGotItBody.Currency(query = ""),
            )

            // ----- Act -----
            val body = draft.sheet().body.shouldBeInstanceOf<WhereYouGotItBodyUiModel.Currency>()

            // ----- Assert -----
            body.rows.map { it.key } shouldBe listOf(YEN.code, EURO.code, DINAR.code)
            body.rows.filter { it.selected }.map { it.key } shouldBe listOf(YEN.code)
        }

        @Test
        fun `puts the usual currency right after the selected one`() {
            // ----- Arrange -----
            val draft = buildWhereYouGotItDraft(
                currencyCode = DINAR.code,
                body = WhereYouGotItBody.Currency(query = ""),
            )

            // ----- Act -----
            val body = draft.sheet(usualCurrencyCode = YEN.code).body
                .shouldBeInstanceOf<WhereYouGotItBodyUiModel.Currency>()

            // ----- Assert -----
            body.rows.map { it.key } shouldBe listOf(DINAR.code, YEN.code, EURO.code)
        }

        @Test
        fun `narrows the rows to the search query and carries it`() {
            // ----- Arrange -----
            val draft = buildWhereYouGotItDraft(body = WhereYouGotItBody.Currency(query = "kuwait"))

            // ----- Act -----
            val body = draft.sheet().body.shouldBeInstanceOf<WhereYouGotItBodyUiModel.Currency>()

            // ----- Assert -----
            body.searchQuery shouldBe "kuwait"
            body.rows.map { it.key } shouldBe listOf(DINAR.code)
        }

        @Test
        fun `uses a pinned back-only scaffold that blocks accidental dismissal`() {
            // ----- Arrange -----
            val draft = buildWhereYouGotItDraft(body = WhereYouGotItBody.Currency(query = ""))

            // ----- Act -----
            val scaffold = draft.sheet().scaffold

            // ----- Assert -----
            scaffold.dismissible shouldBe false
            scaffold.footer.shouldBeNull()
        }
    }
}
