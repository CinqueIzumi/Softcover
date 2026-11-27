package nl.rhaydus.softcover.feature.book_detail.presentation

import nl.rhaydus.softcover.core.domain.model.Currency
import nl.rhaydus.softcover.feature.book_detail.presentation.state.WhereYouGotItBody
import nl.rhaydus.softcover.feature.book_detail.presentation.state.WhereYouGotItDraft
import nl.rhaydus.softcover.feature.book_detail.presentation.state.WhereYouGotItSource

internal val EURO = Currency(
    code = "EUR",
    name = "Euro",
    symbol = "€",
    fractionDigits = 2,
)

internal val YEN = Currency(
    code = "JPY",
    name = "Japanese yen",
    symbol = "¥",
    fractionDigits = 0,
)

internal val DINAR = Currency(
    code = "KWD",
    name = "Kuwaiti dinar",
    symbol = "KD",
    fractionDigits = 3,
)

internal val TEST_CURRENCIES = listOf(EURO, YEN, DINAR)

internal fun buildWhereYouGotItDraft(
    source: WhereYouGotItSource = WhereYouGotItSource.None,
    amountText: String = "",
    currencyCode: String = EURO.code,
    amountError: String? = null,
    body: WhereYouGotItBody = WhereYouGotItBody.Choices,
): WhereYouGotItDraft = WhereYouGotItDraft(
    source = source,
    amountText = amountText,
    currencyCode = currencyCode,
    amountError = amountError,
    body = body,
)
