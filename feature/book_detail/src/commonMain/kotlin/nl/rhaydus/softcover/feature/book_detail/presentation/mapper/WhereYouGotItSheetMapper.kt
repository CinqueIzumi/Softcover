package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.field.SelectFieldUiModel
import nl.rhaydus.softcover.core.component.field.TextFieldError
import nl.rhaydus.softcover.core.component.field.TextFieldKeyboard
import nl.rhaydus.softcover.core.component.field.TextFieldUiModel
import nl.rhaydus.softcover.core.component.field.TextFieldVariant
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetFooterButton
import nl.rhaydus.softcover.core.component.sheet.SheetFooterEmphasis
import nl.rhaydus.softcover.core.component.sheet.SheetFooterUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetHeaderTrailing
import nl.rhaydus.softcover.core.component.sheet.SheetHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetInputRowUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetLayout
import nl.rhaydus.softcover.core.component.sheet.SheetRowFace
import nl.rhaydus.softcover.core.component.sheet.SheetRowGroupUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetRowUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel
import nl.rhaydus.softcover.core.domain.model.AcquisitionKind
import nl.rhaydus.softcover.core.domain.model.Currency
import nl.rhaydus.softcover.core.uibinding.acquisition.toLabel
import nl.rhaydus.softcover.core.uibinding.money.toCurrencyRows
import nl.rhaydus.softcover.feature.book_detail.presentation.state.WhereYouGotItBody
import nl.rhaydus.softcover.feature.book_detail.presentation.state.WhereYouGotItBodyUiModel
import nl.rhaydus.softcover.feature.book_detail.presentation.state.WhereYouGotItDraft
import nl.rhaydus.softcover.feature.book_detail.presentation.state.WhereYouGotItSheetUiModel
import nl.rhaydus.softcover.feature.book_detail.presentation.state.WhereYouGotItSource

internal const val WHERE_YOU_GOT_IT_INPUT_KEY = "where-you-got-it-other"
internal const val WHERE_YOU_GOT_IT_AMOUNT_KEY = "where-you-got-it-amount"
internal const val WHERE_YOU_GOT_IT_CURRENCY_KEY = "where-you-got-it-currency"

internal fun WhereYouGotItDraft.toWhereYouGotItSheet(
    bookTitle: String,
    currencies: List<Currency>,
    usualCurrencyCode: String?,
): WhereYouGotItSheetUiModel {
    val currencyBody = body as? WhereYouGotItBody.Currency

    return WhereYouGotItSheetUiModel(
        scaffold = if (currencyBody == null) {
            toChoicesScaffold(bookTitle = bookTitle)
        } else {
            toCurrencyScaffold(bookTitle = bookTitle)
        },
        body = if (currencyBody == null) {
            toChoicesBody(currencies = currencies)
        } else {
            WhereYouGotItBodyUiModel.Currency(
                searchQuery = currencyBody.query,
                searchPlaceholder = "Search currencies",
                rows = currencies.toCurrencyRows(
                    selectedCode = currencyCode,
                    usualCode = usualCurrencyCode,
                    query = currencyBody.query,
                ),
            )
        },
    )
}

private fun WhereYouGotItDraft.toChoicesScaffold(bookTitle: String) = SheetScaffoldUiModel(
    header = SheetHeaderUiModel(
        section = SectionHeaderUiModel.Section(
            eyebrow = bookTitle,
            headline = "Where you got it",
        ),
    ),
    footer = SheetFooterUiModel.Inline(
        primary = SheetFooterButton(
            label = "Save",
            enabled = amountError == null,
            dismissesSheet = true,
        ),
        secondary = SheetFooterButton(
            label = "Cancel",
            emphasis = SheetFooterEmphasis.Outlined,
            dismissesSheet = true,
        ),
    ),
    layout = SheetLayout.Pinned,
)

private fun toCurrencyScaffold(bookTitle: String) = SheetScaffoldUiModel(
    header = SheetHeaderUiModel(
        section = SectionHeaderUiModel.Section(
            eyebrow = bookTitle,
            headline = "Currency",
        ),
        trailing = SheetHeaderTrailing.Action(label = "Back"),
    ),
    layout = SheetLayout.Pinned,
    dismissible = false,
)

private fun WhereYouGotItDraft.toChoicesBody(currencies: List<Currency>): WhereYouGotItBodyUiModel.Choices {
    val selectedKey = (source as? WhereYouGotItSource.Preset)?.key
    val customText = (source as? WhereYouGotItSource.Custom)?.text
    val digits = currencies.fractionDigitsOf(code = currencyCode)

    return WhereYouGotItBodyUiModel.Choices(
        group = SheetRowGroupUiModel(
            rows = AcquisitionKind.entries
                .map { kind ->
                    SheetRowUiModel(
                        key = kind.toKey(),
                        label = kind.toLabel(),
                        selected = kind.toKey() == selectedKey,
                        gloss = kind.toGloss(),
                    )
                }
                .toImmutableList(),
            dismissesSheet = false,
            input = SheetInputRowUiModel(
                field = TextFieldUiModel(
                    key = WHERE_YOU_GOT_IT_INPUT_KEY,
                    text = customText.orEmpty(),
                    placeholder = WHERE_YOU_GOT_IT_PLACEHOLDER,
                    variant = TextFieldVariant.Plain,
                ),
                selected = customText != null,
            ),
            face = SheetRowFace.Body,
        ),
        priceHeader = SectionHeaderUiModel.Label(eyebrow = "Price paid"),
        amount = TextFieldUiModel(
            key = WHERE_YOU_GOT_IT_AMOUNT_KEY,
            text = amountText,
            placeholder = if (digits <= 0) "0" else "0." + "0".repeat(digits),
            variant = TextFieldVariant.Outlined(keyboard = TextFieldKeyboard.Decimal),
            error = amountError?.let {
                TextFieldError(
                    message = it,
                    shaking = false,
                )
            },
        ),
        currency = SelectFieldUiModel(
            key = WHERE_YOU_GOT_IT_CURRENCY_KEY,
            value = currencyCode,
            contentDescription = "Currency, $currencyCode",
        ),
        caption = "Optional. Your usual currency is set in Settings; change it here for a book bought elsewhere.",
    )
}
