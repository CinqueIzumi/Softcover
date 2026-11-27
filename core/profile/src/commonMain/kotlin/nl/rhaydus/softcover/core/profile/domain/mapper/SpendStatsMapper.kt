package nl.rhaydus.softcover.core.profile.domain.mapper

import nl.rhaydus.softcover.core.domain.model.AcquisitionSource
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookCopy
import nl.rhaydus.softcover.core.profile.domain.model.CurrencySpend
import nl.rhaydus.softcover.core.profile.domain.model.SourceSpend
import nl.rhaydus.softcover.core.profile.domain.model.SpendSource
import nl.rhaydus.softcover.core.profile.domain.model.SpendStats

private class PricedBook(
    val currencyCode: String,
    val amountThousandths: Long,
    val source: SpendSource,
)

internal fun List<Book>.toSpendStats(copiesByBookId: Map<Int, BookCopy>): SpendStats {
    val priced = mapNotNull { book ->
        val copy = copiesByBookId[book.id] ?: return@mapNotNull null

        val price = copy.price ?: return@mapNotNull null

        PricedBook(
            currencyCode = price.currencyCode,
            amountThousandths = price.amountThousandths,
            source = copy.source.toSpendSource(),
        )
    }

    return SpendStats(
        countedBooks = size,
        pricedBooks = priced.size,
        currencies = priced
            .groupBy { it.currencyCode }
            .map { (code, books) -> books.toCurrencySpend(code) }
            .sortedBy { it.currencyCode },
    )
}

private fun List<PricedBook>.toCurrencySpend(currencyCode: String) = CurrencySpend(
    currencyCode = currencyCode,
    totalThousandths = sumOf { it.amountThousandths },
    pricedBooks = size,
    medianThousandths = map { it.amountThousandths }.median(),
    sources = groupBy { it.source }
        .map { (source, books) ->
            SourceSpend(
                source = source,
                totalThousandths = books.sumOf { it.amountThousandths },
                books = books.size,
            )
        }
        .sortedByDescending { it.totalThousandths },
)

private fun AcquisitionSource?.toSpendSource(): SpendSource = when (this) {
    is AcquisitionSource.Preset -> SpendSource.Preset(kind)
    is AcquisitionSource.Other -> SpendSource.SomewhereElse
    null -> SpendSource.NotNoted
}
