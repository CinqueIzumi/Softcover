package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.domain.model.AcquisitionKind
import nl.rhaydus.softcover.core.domain.model.AcquisitionSource
import nl.rhaydus.softcover.core.domain.model.BookCopy
import nl.rhaydus.softcover.core.domain.model.PricePaid
import nl.rhaydus.softcover.feature.book_detail.presentation.DINAR
import nl.rhaydus.softcover.feature.book_detail.presentation.EURO
import nl.rhaydus.softcover.feature.book_detail.presentation.TEST_CURRENCIES
import nl.rhaydus.softcover.feature.book_detail.presentation.YEN
import nl.rhaydus.softcover.feature.book_detail.presentation.buildWhereYouGotItDraft
import nl.rhaydus.softcover.feature.book_detail.presentation.state.WhereYouGotItBody
import nl.rhaydus.softcover.feature.book_detail.presentation.state.WhereYouGotItSource
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class WhereYouGotItDraftMapperTest {
    @Nested
    inner class ToWhereYouGotItDraft {
        @Test
        fun `drafts a preset source as that preset's key`() {
            // ----- Arrange -----
            val copy = BookCopy(
                bookId = 1,
                source = AcquisitionSource.Preset(kind = AcquisitionKind.GIFT),
            )

            // ----- Act -----
            val result = copy.toWhereYouGotItDraft(
                usualCurrency = EURO,
                currencies = TEST_CURRENCIES,
            )

            // ----- Assert -----
            result.source shouldBe WhereYouGotItSource.Preset(key = AcquisitionKind.GIFT.toKey())
        }

        @Test
        fun `drafts another source as custom text`() {
            // ----- Arrange -----
            val copy = BookCopy(
                bookId = 1,
                source = AcquisitionSource.Other(text = "A street stall"),
            )

            // ----- Act -----
            val result = copy.toWhereYouGotItDraft(
                usualCurrency = EURO,
                currencies = TEST_CURRENCIES,
            )

            // ----- Assert -----
            result.source shouldBe WhereYouGotItSource.Custom(text = "A street stall")
        }

        @Test
        fun `drafts a copy without a source as none`() {
            // ----- Arrange -----
            val copy = BookCopy(bookId = 1)

            // ----- Act -----
            val result = copy.toWhereYouGotItDraft(
                usualCurrency = EURO,
                currencies = TEST_CURRENCIES,
            )

            // ----- Assert -----
            result.source shouldBe WhereYouGotItSource.None
        }

        @Test
        fun `seeds the usual currency and an empty amount when there is no price`() {
            // ----- Arrange -----
            val copy = BookCopy(bookId = 1)

            // ----- Act -----
            val result = copy.toWhereYouGotItDraft(
                usualCurrency = YEN,
                currencies = TEST_CURRENCIES,
            )

            // ----- Assert -----
            result shouldBe buildWhereYouGotItDraft(currencyCode = YEN.code)
        }

        @Test
        fun `seeds the stored price in its own currency, not the usual one`() {
            // ----- Arrange -----
            val copy = BookCopy(
                bookId = 1,
                price = PricePaid(
                    amountThousandths = 14_500,
                    currencyCode = EURO.code,
                ),
            )

            // ----- Act -----
            val result = copy.toWhereYouGotItDraft(
                usualCurrency = YEN,
                currencies = TEST_CURRENCIES,
            )

            // ----- Assert -----
            result.currencyCode shouldBe EURO.code
            result.amountText shouldBe "14.50"
        }

        @Test
        fun `formats the stored amount with the currency's digits`() {
            // ----- Arrange -----
            val yen = BookCopy(
                bookId = 1,
                price = PricePaid(
                    amountThousandths = 1_450_000,
                    currencyCode = YEN.code,
                ),
            )

            val dinar = BookCopy(
                bookId = 1,
                price = PricePaid(
                    amountThousandths = 14_505,
                    currencyCode = DINAR.code,
                ),
            )

            // ----- Act -----
            val yenDraft = yen.toWhereYouGotItDraft(
                usualCurrency = EURO,
                currencies = TEST_CURRENCIES,
            )

            val dinarDraft = dinar.toWhereYouGotItDraft(
                usualCurrency = EURO,
                currencies = TEST_CURRENCIES,
            )

            // ----- Assert -----
            yenDraft.amountText shouldBe "1450"
            dinarDraft.amountText shouldBe "14.505"
        }

        @Test
        fun `seeds a free price as zero rather than an empty amount`() {
            // ----- Arrange -----
            val copy = BookCopy(
                bookId = 1,
                price = PricePaid(
                    amountThousandths = 0,
                    currencyCode = EURO.code,
                ),
            )

            // ----- Act -----
            val result = copy.toWhereYouGotItDraft(
                usualCurrency = EURO,
                currencies = TEST_CURRENCIES,
            )

            // ----- Assert -----
            result.amountText shouldBe "0.00"
        }

        @Test
        fun `opens on the choices without an error`() {
            // ----- Arrange -----
            val copy = BookCopy(bookId = 1)

            // ----- Act -----
            val result = copy.toWhereYouGotItDraft(
                usualCurrency = EURO,
                currencies = TEST_CURRENCIES,
            )

            // ----- Assert -----
            result.body shouldBe WhereYouGotItBody.Choices
            result.amountError shouldBe null
        }
    }

    @Nested
    inner class WithPresetClicked {
        @Test
        fun `picks the tapped preset from none`() {
            // ----- Arrange -----
            val draft = buildWhereYouGotItDraft()

            // ----- Act -----
            val result = draft.withPresetClicked(key = AcquisitionKind.LIBRARY.toKey())

            // ----- Assert -----
            result.source shouldBe WhereYouGotItSource.Preset(key = AcquisitionKind.LIBRARY.toKey())
        }

        @Test
        fun `replaces the picked preset when a different one is tapped`() {
            // ----- Arrange -----
            val draft = buildWhereYouGotItDraft(source = WhereYouGotItSource.Preset(key = AcquisitionKind.LIBRARY.toKey()))

            // ----- Act -----
            val result = draft.withPresetClicked(key = AcquisitionKind.GIFT.toKey())

            // ----- Assert -----
            result.source shouldBe WhereYouGotItSource.Preset(key = AcquisitionKind.GIFT.toKey())
        }

        @Test
        fun `clears to none when the picked preset is tapped again`() {
            // ----- Arrange -----
            val draft = buildWhereYouGotItDraft(source = WhereYouGotItSource.Preset(key = AcquisitionKind.LIBRARY.toKey()))

            // ----- Act -----
            val result = draft.withPresetClicked(key = AcquisitionKind.LIBRARY.toKey())

            // ----- Assert -----
            result.source shouldBe WhereYouGotItSource.None
        }

        @Test
        fun `replaces custom text with the tapped preset`() {
            // ----- Arrange -----
            val draft = buildWhereYouGotItDraft(source = WhereYouGotItSource.Custom(text = "A street stall"))

            // ----- Act -----
            val result = draft.withPresetClicked(key = AcquisitionKind.BORROWED.toKey())

            // ----- Assert -----
            result.source shouldBe WhereYouGotItSource.Preset(key = AcquisitionKind.BORROWED.toKey())
        }

        @Test
        fun `keeps the price fields when the source changes`() {
            // ----- Arrange -----
            val draft = buildWhereYouGotItDraft(
                amountText = "14.50",
                currencyCode = YEN.code,
                amountError = "Enter a price like 1450.",
            )

            // ----- Act -----
            val result = draft.withPresetClicked(key = AcquisitionKind.GIFT.toKey())

            // ----- Assert -----
            result.amountText shouldBe "14.50"
            result.currencyCode shouldBe YEN.code
            result.amountError shouldBe "Enter a price like 1450."
        }
    }

    @Nested
    inner class WithCustomText {
        @Test
        fun `turns typed text into custom text`() {
            // ----- Arrange -----
            val draft = buildWhereYouGotItDraft()

            // ----- Act -----
            val result = draft.withCustomText(text = "A street stall")

            // ----- Assert -----
            result.source shouldBe WhereYouGotItSource.Custom(text = "A street stall")
        }

        @Test
        fun `unticks a picked preset when text is typed`() {
            // ----- Arrange -----
            val draft = buildWhereYouGotItDraft(source = WhereYouGotItSource.Preset(key = AcquisitionKind.LIBRARY.toKey()))

            // ----- Act -----
            val result = draft.withCustomText(text = "A")

            // ----- Assert -----
            result.source shouldBe WhereYouGotItSource.Custom(text = "A")
        }

        @Test
        fun `clears to none when the text is emptied`() {
            // ----- Arrange -----
            val draft = buildWhereYouGotItDraft(source = WhereYouGotItSource.Custom(text = "A"))

            // ----- Act -----
            val result = draft.withCustomText(text = "")

            // ----- Assert -----
            result.source shouldBe WhereYouGotItSource.None
        }

        @Test
        fun `keeps whitespace-only text as custom text so the field keeps what was typed`() {
            // ----- Arrange -----
            val draft = buildWhereYouGotItDraft()

            // ----- Act -----
            val result = draft.withCustomText(text = "  ")

            // ----- Assert -----
            result.source shouldBe WhereYouGotItSource.Custom(text = "  ")
        }
    }

    @Nested
    inner class WithAmountText {
        @Test
        fun `stores valid text without an error`() {
            // ----- Arrange -----
            val draft = buildWhereYouGotItDraft(amountError = "Enter a price like 14.50.")

            // ----- Act -----
            val result = draft.withAmountText(
                text = "14,50",
                currencies = TEST_CURRENCIES,
            )

            // ----- Assert -----
            result.amountText shouldBe "14,50"
            result.amountError shouldBe null
        }

        @Test
        fun `flags text with too many fraction digits for the drafted currency`() {
            // ----- Arrange -----
            val draft = buildWhereYouGotItDraft(currencyCode = YEN.code)

            // ----- Act -----
            val result = draft.withAmountText(
                text = "14.50",
                currencies = TEST_CURRENCIES,
            )

            // ----- Assert -----
            result.amountText shouldBe "14.50"
            result.amountError shouldBe "Enter a price like 1450."
        }

        @Test
        fun `clears the error when the text is emptied`() {
            // ----- Arrange -----
            val draft = buildWhereYouGotItDraft(
                amountText = "abc",
                amountError = "Enter a price like 14.50.",
            )

            // ----- Act -----
            val result = draft.withAmountText(
                text = "",
                currencies = TEST_CURRENCIES,
            )

            // ----- Assert -----
            result.amountError shouldBe null
        }
    }

    @Nested
    inner class WithCurrency {
        @Test
        fun `switches the currency and returns to the choices`() {
            // ----- Arrange -----
            val draft = buildWhereYouGotItDraft(body = WhereYouGotItBody.Currency(query = "yen"))

            // ----- Act -----
            val result = draft.withCurrency(
                code = YEN.code,
                currencies = TEST_CURRENCIES,
            )

            // ----- Assert -----
            result.currencyCode shouldBe YEN.code
            result.body shouldBe WhereYouGotItBody.Choices
        }

        @Test
        fun `flags an amount the new currency cannot hold`() {
            // ----- Arrange -----
            val draft = buildWhereYouGotItDraft(amountText = "14.50")

            // ----- Act -----
            val result = draft.withCurrency(
                code = YEN.code,
                currencies = TEST_CURRENCIES,
            )

            // ----- Assert -----
            result.amountText shouldBe "14.50"
            result.amountError shouldBe "Enter a price like 1450."
        }

        @Test
        fun `clears an error the new currency resolves`() {
            // ----- Arrange -----
            val draft = buildWhereYouGotItDraft(
                amountText = "14.505",
                currencyCode = EURO.code,
                amountError = "Enter a price like 14.50.",
            )

            // ----- Act -----
            val result = draft.withCurrency(
                code = DINAR.code,
                currencies = TEST_CURRENCIES,
            )

            // ----- Assert -----
            result.amountError shouldBe null
        }
    }

    @Nested
    inner class ToSource {
        @Test
        fun `maps none to no source`() {
            // ----- Arrange -----
            val draft = buildWhereYouGotItDraft()

            // ----- Act -----
            val result = draft.toSource()

            // ----- Assert -----
            result shouldBe null
        }

        @Test
        fun `maps a preset to its kind`() {
            // ----- Arrange -----
            val draft = buildWhereYouGotItDraft(source = WhereYouGotItSource.Preset(key = AcquisitionKind.BOUGHT_ONLINE.toKey()))

            // ----- Act -----
            val result = draft.toSource()

            // ----- Assert -----
            result shouldBe AcquisitionSource.Preset(kind = AcquisitionKind.BOUGHT_ONLINE)
        }

        @Test
        fun `maps a preset with an unknown key to no source`() {
            // ----- Arrange -----
            val draft = buildWhereYouGotItDraft(source = WhereYouGotItSource.Preset(key = "acquired-unknown"))

            // ----- Act -----
            val result = draft.toSource()

            // ----- Assert -----
            result shouldBe null
        }

        @Test
        fun `trims custom text`() {
            // ----- Arrange -----
            val draft = buildWhereYouGotItDraft(source = WhereYouGotItSource.Custom(text = "  A street stall \n"))

            // ----- Act -----
            val result = draft.toSource()

            // ----- Assert -----
            result shouldBe AcquisitionSource.Other(text = "A street stall")
        }

        @Test
        fun `maps blank custom text to no source`() {
            // ----- Arrange -----
            val draft = buildWhereYouGotItDraft(source = WhereYouGotItSource.Custom(text = " \t "))

            // ----- Act -----
            val result = draft.toSource()

            // ----- Assert -----
            result shouldBe null
        }
    }

    @Nested
    inner class ToPrice {
        @Test
        fun `maps an empty amount to no price`() {
            // ----- Arrange -----
            val draft = buildWhereYouGotItDraft(amountText = "")

            // ----- Act -----
            val result = draft.toPrice(currencies = TEST_CURRENCIES)

            // ----- Assert -----
            result shouldBe null
        }

        @Test
        fun `maps an invalid amount to no price`() {
            // ----- Arrange -----
            val draft = buildWhereYouGotItDraft(amountText = "1.234,50")

            // ----- Act -----
            val result = draft.toPrice(currencies = TEST_CURRENCIES)

            // ----- Assert -----
            result shouldBe null
        }

        @Test
        fun `maps a valid amount to thousandths in the drafted currency`() {
            // ----- Arrange -----
            val draft = buildWhereYouGotItDraft(
                amountText = "14,50",
                currencyCode = EURO.code,
            )

            // ----- Act -----
            val result = draft.toPrice(currencies = TEST_CURRENCIES)

            // ----- Assert -----
            result shouldBe PricePaid(
                amountThousandths = 14_500,
                currencyCode = EURO.code,
            )
        }

        @Test
        fun `keeps a free price`() {
            // ----- Arrange -----
            val draft = buildWhereYouGotItDraft(amountText = "0")

            // ----- Act -----
            val result = draft.toPrice(currencies = TEST_CURRENCIES)

            // ----- Assert -----
            result shouldBe PricePaid(
                amountThousandths = 0,
                currencyCode = EURO.code,
            )
        }
    }
}
