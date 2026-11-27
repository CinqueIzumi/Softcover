package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.yourcopy.YourCopyPanelUiModel
import nl.rhaydus.softcover.core.component.yourcopy.YourCopyRowUiModel
import nl.rhaydus.softcover.core.component.yourcopy.YourCopyValue
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.domain.model.AcquisitionSource
import nl.rhaydus.softcover.core.domain.model.BookCopy
import nl.rhaydus.softcover.core.domain.model.Currency
import nl.rhaydus.softcover.core.uibinding.acquisition.toLabel
import nl.rhaydus.softcover.core.uibinding.money.toLabel as toPriceLabel

internal const val WHERE_YOU_GOT_IT_ROW_KEY = "where-you-got-it"

internal fun BookCopy.toYourCopyPanel(currencies: List<Currency>): YourCopyPanelUiModel<String> = YourCopyPanelUiModel(
    rows = persistentListOf(
        YourCopyRowUiModel(
            key = WHERE_YOU_GOT_IT_ROW_KEY,
            icon = SoftcoverIcon.ShoppingBag,
            label = "Where you got it",
            value = when (val stored = source) {
                is AcquisitionSource.Preset -> YourCopyValue.Set(text = stored.kind.toLabel())

                is AcquisitionSource.Other -> YourCopyValue.Set(text = stored.text)

                null -> YourCopyValue.Empty(invitation = "Add where you got it")
            },
            trailing = price?.let { paid ->
                currencies.firstOrNull { it.code == paid.currencyCode }?.let { paid.toPriceLabel(currency = it) }
            },
        ),
    ),
)
