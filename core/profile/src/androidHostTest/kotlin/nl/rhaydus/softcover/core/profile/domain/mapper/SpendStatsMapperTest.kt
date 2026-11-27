package nl.rhaydus.softcover.core.profile.domain.mapper

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.domain.model.AcquisitionKind
import nl.rhaydus.softcover.core.domain.model.AcquisitionSource
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookCopy
import nl.rhaydus.softcover.core.domain.model.PricePaid
import nl.rhaydus.softcover.core.profile.domain.model.SourceSpend
import nl.rhaydus.softcover.core.profile.domain.model.SpendSource
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SpendStatsMapperTest {
    private fun copy(
        id: Int,
        thousandths: Long?,
        currency: String = "EUR",
        source: AcquisitionSource? = null,
    ) = id to BookCopy(
        bookId = id,
        source = source,
        price = thousandths?.let {
            PricePaid(
                amountThousandths = it,
                currencyCode = currency,
            )
        },
    )

    @Nested
    inner class ToSpendStats {
        @Test
        fun `no books yields no currencies`() {
            // ----- Arrange -----
            val books = emptyList<Book>()

            // ----- Act -----
            val result = books.toSpendStats(emptyMap())

            // ----- Assert -----
            listOf(result.countedBooks, result.pricedBooks, result.currencies.size) shouldBe listOf(0, 0, 0)
        }

        @Test
        fun `books without a copy or a price count but add no spend`() {
            // ----- Arrange -----
            val books = listOf(readBook(id = 1), readBook(id = 2), readBook(id = 3))

            val copies = mapOf(
                copy(
                    id = 1,
                    thousandths = 10_000,
                ),
                copy(
                    id = 2,
                    thousandths = null,
                ),
            )

            // ----- Act -----
            val result = books.toSpendStats(copies)

            // ----- Assert -----
            listOf(result.countedBooks, result.pricedBooks, result.currencies.single().totalThousandths) shouldBe
                listOf(3, 1, 10_000L)
        }

        @Test
        fun `a copy for a book outside the list is ignored`() {
            // ----- Arrange -----
            val books = listOf(readBook(id = 1))

            val copies = mapOf(
                copy(
                    id = 1,
                    thousandths = 5_000,
                ),
                copy(
                    id = 9,
                    thousandths = 99_000,
                ),
            )

            // ----- Act -----
            val result = books.toSpendStats(copies)

            // ----- Assert -----
            result.currencies.single().totalThousandths shouldBe 5_000L
        }

        @Test
        fun `each currency keeps its own total, count and median, ordered by code`() {
            // ----- Arrange -----
            val books = (1..5).map { readBook(id = it) }

            val copies = mapOf(
                copy(
                    id = 1,
                    thousandths = 10_000,
                    currency = "GBP",
                ),
                copy(
                    id = 2,
                    thousandths = 20_000,
                    currency = "EUR",
                ),
                copy(
                    id = 3,
                    thousandths = 40_000,
                    currency = "EUR",
                ),
                copy(
                    id = 4,
                    thousandths = 90_000,
                    currency = "EUR",
                ),
                copy(
                    id = 5,
                    thousandths = 30_000,
                    currency = "GBP",
                ),
            )

            // ----- Act -----
            val result = books.toSpendStats(copies)

            // ----- Assert -----
            result.currencies.map { listOf(it.currencyCode, it.totalThousandths, it.pricedBooks, it.medianThousandths) } shouldBe
                listOf(
                    listOf("EUR", 150_000L, 3, 40_000L),
                    listOf("GBP", 40_000L, 2, 20_000L),
                )
        }

        @Test
        fun `buckets by source, orders them by spend and files custom text under somewhere else`() {
            // ----- Arrange -----
            val books = (1..5).map { readBook(id = it) }

            val copies = mapOf(
                copy(
                    1,
                    10_000,
                    source = AcquisitionSource.Preset(AcquisitionKind.GIFT),
                ),
                copy(
                    2,
                    30_000,
                    source = AcquisitionSource.Preset(AcquisitionKind.BOUGHT_ONLINE),
                ),
                copy(
                    3,
                    5_000,
                    source = AcquisitionSource.Other("a market"),
                ),
                copy(
                    4,
                    7_000,
                    source = AcquisitionSource.Other("a swap"),
                ),
                copy(
                    5,
                    20_000,
                    source = null,
                ),
            )

            // ----- Act -----
            val result = books.toSpendStats(copies)

            // ----- Assert -----
            result.currencies.single().sources shouldBe listOf(
                SourceSpend(
                    SpendSource.Preset(AcquisitionKind.BOUGHT_ONLINE),
                    30_000,
                    1,
                ),
                SourceSpend(
                    SpendSource.NotNoted,
                    20_000,
                    1,
                ),
                SourceSpend(
                    SpendSource.SomewhereElse,
                    12_000,
                    2,
                ),
                SourceSpend(
                    SpendSource.Preset(AcquisitionKind.GIFT),
                    10_000,
                    1,
                ),
            )
        }

        @Test
        fun `a free book still counts as priced`() {
            // ----- Arrange -----
            val books = listOf(readBook(id = 1))

            val copies = mapOf(
                copy(
                    id = 1,
                    thousandths = 0,
                ),
            )

            // ----- Act -----
            val result = books.toSpendStats(copies)

            // ----- Assert -----
            listOf(result.pricedBooks, result.currencies.single().medianThousandths) shouldBe listOf(1, 0L)
        }
    }
}
