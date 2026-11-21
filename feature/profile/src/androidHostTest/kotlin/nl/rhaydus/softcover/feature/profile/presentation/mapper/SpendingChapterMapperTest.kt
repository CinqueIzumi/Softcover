package nl.rhaydus.softcover.feature.profile.presentation.mapper

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.component.chart.ChartTone
import nl.rhaydus.softcover.core.component.chart.ProportionChartVariant
import nl.rhaydus.softcover.core.domain.model.AcquisitionKind
import nl.rhaydus.softcover.core.domain.model.Currency
import nl.rhaydus.softcover.core.profile.domain.model.CurrencySpend
import nl.rhaydus.softcover.core.profile.domain.model.SourceSpend
import nl.rhaydus.softcover.core.profile.domain.model.SpendSource
import nl.rhaydus.softcover.core.profile.domain.model.SpendStats
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SpendingChapterMapperTest {
    private val eur = Currency(
        code = "EUR",
        name = "Euro",
        symbol = "€",
        fractionDigits = 2,
    )
    private val gbp = Currency(
        code = "GBP",
        name = "Pound",
        symbol = "£",
        fractionDigits = 2,
    )
    private val usd = Currency(
        code = "USD",
        name = "Dollar",
        symbol = "US$",
        fractionDigits = 2,
    )
    private val currencies = listOf(eur, gbp, usd)

    private fun spend(
        code: String,
        total: Long,
        books: Int,
        median: Long = 10_000,
        sources: List<SourceSpend> = listOf(
            SourceSpend(
                SpendSource.NotNoted,
                total,
                books,
            ),
        ),
    ) = CurrencySpend(
        currencyCode = code,
        totalThousandths = total,
        pricedBooks = books,
        medianThousandths = median,
        sources = sources,
    )

    private fun stats(vararg currencySpends: CurrencySpend) = SpendStats(
        countedBooks = currencySpends.sumOf { it.pricedBooks },
        pricedBooks = currencySpends.sumOf { it.pricedBooks },
        currencies = currencySpends.toList(),
    )

    private fun description(
        stats: SpendStats,
        usual: Currency? = eur,
    ) = spendingChapterUiModel(
        stats,
        currencies,
        usual,
    ).header.description?.joinToString("") { it.text }

    @Nested
    inner class Loading {
        @Test
        fun `no stats yet gives a masked chapter`() {
            // ----- Arrange -----
            val stats: SpendStats? = null

            // ----- Act -----
            val result = spendingChapterUiModel(
                stats = stats,
                currencies = currencies,
                usual = eur,
            )

            // ----- Assert -----
            listOf(result.header.isLoading, result.spent?.stat?.isLoading, result.emptyLine) shouldBe
                listOf(true, true, null)
        }
    }

    @Nested
    inner class Empty {
        @Test
        fun `a scope with no priced book shows the note and no figures`() {
            // ----- Arrange -----
            val stats = SpendStats(countedBooks = 4)

            // ----- Act -----
            val result = spendingChapterUiModel(
                stats,
                currencies,
                eur,
            )

            // ----- Assert -----
            listOf(
                result.emptyLine,
                result.header.description,
                result.spent,
                result.typical,
                result.blocks.size,
            ) shouldBe listOf("Note a price under Your copy and what you paid shows here.", null, null, null, 0)
        }
    }

    @Nested
    inner class Description {
        @Test
        fun `counts the books with a price in the plural`() {
            // ----- Arrange -----
            val stats = stats(
                spend(
                    "EUR",
                    180_000,
                    18,
                ),
            )

            // ----- Act -----
            val result = description(stats)

            // ----- Assert -----
            result shouldBe "Across the 18 books with a price. Each currency keeps its own total."
        }

        @Test
        fun `is singular for one book`() {
            // ----- Arrange -----
            val stats = stats(
                spend(
                    "EUR",
                    9_000,
                    1,
                ),
            )

            // ----- Act -----
            val result = description(stats)

            // ----- Assert -----
            result shouldBe "Across the 1 book with a price. Each currency keeps its own total."
        }
    }

    @Nested
    inner class LeadCurrency {
        @Test
        fun `is the usual currency when a priced book is in it`() {
            // ----- Arrange -----
            val stats = stats(
                spend(
                    "EUR",
                    100_000,
                    10,
                ),
                spend(
                    "GBP",
                    36_000,
                    2,
                ),
            )

            // ----- Act -----
            val result = spendingChapterUiModel(
                stats,
                currencies,
                gbp,
            )

            // ----- Assert -----
            listOf(result.spent?.stat?.prefix, result.spent?.stat?.value) shouldBe listOf("£", 36.0)
        }

        @Test
        fun `falls back to the currency with the most priced books when the usual one has none`() {
            // ----- Arrange -----
            val stats = stats(
                spend(
                    "GBP",
                    36_000,
                    2,
                ),
                spend(
                    "USD",
                    50_000,
                    5,
                ),
            )

            // ----- Act -----
            val result = spendingChapterUiModel(
                stats,
                currencies,
                eur,
            )

            // ----- Assert -----
            result.spent?.stat?.prefix shouldBe "US$"
        }

        @Test
        fun `breaks a tie on priced books by currency code`() {
            // ----- Arrange -----
            val stats = stats(
                spend(
                    "USD",
                    50_000,
                    3,
                ),
                spend(
                    "GBP",
                    36_000,
                    3,
                ),
            )

            // ----- Act -----
            val result = spendingChapterUiModel(
                stats,
                currencies,
                eur,
            )

            // ----- Assert -----
            result.spent?.stat?.prefix shouldBe "£"
        }

        @Test
        fun `a currency missing from the list shows its code`() {
            // ----- Arrange -----
            val stats = stats(
                spend(
                    "CHF",
                    20_000,
                    2,
                    median = 10_000,
                ),
            )

            // ----- Act -----
            val result = spendingChapterUiModel(
                stats,
                currencies,
                usual = null,
            )

            // ----- Assert -----
            result.spent?.stat?.prefix shouldBe "CHF"
        }
    }

    @Nested
    inner class SpentCell {
        @Test
        fun `says everything is in the lead currency when there is only one`() {
            // ----- Arrange -----
            val stats = stats(
                spend(
                    "EUR",
                    98_000,
                    6,
                ),
            )

            // ----- Act -----
            val result = spendingChapterUiModel(
                stats,
                currencies,
                eur,
            )

            // ----- Assert -----
            result.spent?.caption shouldBe "All in EUR."
        }

        @Test
        fun `names another currency's total and its singular book`() {
            // ----- Arrange -----
            val stats = stats(
                spend(
                    "EUR",
                    98_000,
                    6,
                ),
                spend(
                    "GBP",
                    36_000,
                    1,
                ),
            )

            // ----- Act -----
            val result = spendingChapterUiModel(
                stats,
                currencies,
                eur,
            )

            // ----- Assert -----
            result.spent?.caption shouldBe "plus £36 on 1 book."
        }

        @Test
        fun `joins several other currencies ordered by priced books`() {
            // ----- Arrange -----
            val stats = stats(
                spend(
                    "EUR",
                    98_000,
                    6,
                ),
                spend(
                    "GBP",
                    36_000,
                    2,
                ),
                spend(
                    "USD",
                    20_000,
                    3,
                ),
            )

            // ----- Act -----
            val result = spendingChapterUiModel(
                stats,
                currencies,
                eur,
            )

            // ----- Assert -----
            result.spent?.caption shouldBe "plus US$20 on 3 books and £36 on 2 books."
        }

        @Test
        fun `rounds the total to whole units`() {
            // ----- Arrange -----
            val stats = stats(
                spend(
                    "EUR",
                    98_500,
                    6,
                ),
            )

            // ----- Act -----
            val result = spendingChapterUiModel(
                stats,
                currencies,
                eur,
            )

            // ----- Assert -----
            result.spent?.stat?.value shouldBe 99.0
        }
    }

    @Nested
    inner class TypicalCell {
        @Test
        fun `shows the lead currency's median in whole units`() {
            // ----- Arrange -----
            val stats = stats(
                spend(
                    "EUR",
                    98_000,
                    6,
                    median = 14_600,
                ),
            )

            // ----- Act -----
            val result = spendingChapterUiModel(
                stats,
                currencies,
                eur,
            )

            // ----- Assert -----
            listOf(result.typical?.stat?.value, result.typical?.stat?.prefix) shouldBe listOf(15.0, "€")
        }
    }

    @Nested
    inner class Blocks {
        @Test
        fun `titles the lead block with rows and each other currency with a legend`() {
            // ----- Arrange -----
            val stats = stats(
                spend(
                    "EUR",
                    98_000,
                    6,
                ),
                spend(
                    "GBP",
                    36_000,
                    2,
                ),
                spend(
                    "USD",
                    20_000,
                    1,
                ),
            )

            // ----- Act -----
            val result = spendingChapterUiModel(
                stats,
                currencies,
                eur,
            )

            // ----- Assert -----
            result.blocks.map { it.title to it.chart.variant } shouldBe listOf(
                "Where it went, in EUR" to ProportionChartVariant.Rows,
                "In GBP" to ProportionChartVariant.Legend,
                "In USD" to ProportionChartVariant.Legend,
            )
        }

        @Test
        fun `labels each source with its spend, book count, share and tone`() {
            // ----- Arrange -----
            val stats = stats(
                spend(
                    code = "EUR",
                    total = 100_000,
                    books = 10,
                    sources = listOf(
                        SourceSpend(
                            SpendSource.Preset(AcquisitionKind.BOUGHT_ONLINE),
                            50_000,
                            6,
                        ),
                        SourceSpend(
                            SpendSource.Preset(AcquisitionKind.GIFT),
                            25_000,
                            1,
                        ),
                        SourceSpend(
                            SpendSource.SomewhereElse,
                            15_000,
                            2,
                        ),
                        SourceSpend(
                            SpendSource.NotNoted,
                            10_000,
                            1,
                        ),
                    ),
                ),
            )

            // ----- Act -----
            val result = spendingChapterUiModel(
                stats,
                currencies,
                eur,
            )

            // ----- Assert -----
            result.blocks.single().chart.segments.map {
                listOf(it.label, it.share, it.fraction, it.tone)
            } shouldBe listOf(
                listOf("Bought online", "€50 · 6 books", 0.5f, ChartTone.forRank(0)),
                listOf("A gift", "€25 · 1 book", 0.25f, ChartTone.forRank(1)),
                listOf("Somewhere else", "€15 · 2 books", 0.15f, ChartTone.Fifth),
                listOf("Not noted", "€10 · 1 book", 0.1f, ChartTone.Unknown),
            )
        }

        @Test
        fun `a free currency total gives every source a zero fraction`() {
            // ----- Arrange -----
            val stats = stats(
                spend(
                    "EUR",
                    0,
                    2,
                    median = 0,
                ),
            )

            // ----- Act -----
            val result = spendingChapterUiModel(
                stats,
                currencies,
                eur,
            )

            // ----- Assert -----
            result.blocks.single().chart.segments.map { it.fraction } shouldBe listOf(0f)
        }
    }
}
