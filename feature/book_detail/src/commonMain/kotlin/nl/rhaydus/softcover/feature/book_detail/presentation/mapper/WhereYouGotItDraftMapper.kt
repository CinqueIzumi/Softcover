package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

import nl.rhaydus.softcover.core.domain.model.AcquisitionSource
import nl.rhaydus.softcover.core.domain.model.BookCopy
import nl.rhaydus.softcover.core.domain.model.Currency
import nl.rhaydus.softcover.core.domain.model.PricePaid
import nl.rhaydus.softcover.feature.book_detail.presentation.state.WhereYouGotItBody
import nl.rhaydus.softcover.feature.book_detail.presentation.state.WhereYouGotItDraft
import nl.rhaydus.softcover.feature.book_detail.presentation.state.WhereYouGotItSource

internal fun BookCopy.toWhereYouGotItDraft(
    usualCurrency: Currency,
    currencies: List<Currency>,
): WhereYouGotItDraft {
    val currencyCode = price?.currencyCode ?: usualCurrency.code

    return WhereYouGotItDraft(
        source = when (val stored = source) {
            is AcquisitionSource.Preset -> WhereYouGotItSource.Preset(key = stored.kind.toKey())

            is AcquisitionSource.Other -> WhereYouGotItSource.Custom(text = stored.text)

            null -> WhereYouGotItSource.None
        },
        amountText = price?.let {
            formatPriceAmount(
                thousandths = it.amountThousandths,
                fractionDigits = currencies.fractionDigitsOf(code = currencyCode),
            )
        }.orEmpty(),
        currencyCode = currencyCode,
        amountError = null,
        body = WhereYouGotItBody.Choices,
    )
}

internal fun WhereYouGotItDraft.withPresetClicked(key: String): WhereYouGotItDraft = copy(
    source = if (source == WhereYouGotItSource.Preset(key = key)) {
        WhereYouGotItSource.None
    } else {
        WhereYouGotItSource.Preset(key = key)
    },
)

internal fun WhereYouGotItDraft.withCustomText(text: String): WhereYouGotItDraft = copy(
    source = if (text.isEmpty()) WhereYouGotItSource.None else WhereYouGotItSource.Custom(text = text),
)

internal fun WhereYouGotItDraft.withAmountText(
    text: String,
    currencies: List<Currency>,
): WhereYouGotItDraft = copy(
    amountText = text,
    amountError = priceAmountError(
        text = text,
        fractionDigits = currencies.fractionDigitsOf(code = currencyCode),
    ),
)

internal fun WhereYouGotItDraft.withCurrency(
    code: String,
    currencies: List<Currency>,
): WhereYouGotItDraft = copy(
    currencyCode = code,
    amountError = priceAmountError(
        text = amountText,
        fractionDigits = currencies.fractionDigitsOf(code = code),
    ),
    body = WhereYouGotItBody.Choices,
)

internal fun WhereYouGotItDraft.toSource(): AcquisitionSource? = when (val chosen = source) {
    WhereYouGotItSource.None -> null

    is WhereYouGotItSource.Preset -> chosen.key.toAcquisitionKind()?.let { AcquisitionSource.Preset(kind = it) }

    is WhereYouGotItSource.Custom -> chosen.text.trim().takeIf { it.isNotEmpty() }?.let {
        AcquisitionSource.Other(text = it)
    }
}

internal fun WhereYouGotItDraft.toPrice(currencies: List<Currency>): PricePaid? {
    val parsed = parsePriceAmount(
        text = amountText,
        fractionDigits = currencies.fractionDigitsOf(code = currencyCode),
    )

    return (parsed as? PriceAmount.Valid)?.let {
        PricePaid(
            amountThousandths = it.thousandths,
            currencyCode = currencyCode,
        )
    }
}
