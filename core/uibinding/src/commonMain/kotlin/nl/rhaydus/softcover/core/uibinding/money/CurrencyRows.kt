package nl.rhaydus.softcover.core.uibinding.money

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.sheet.SheetRowUiModel
import nl.rhaydus.softcover.core.domain.model.Currency

fun List<Currency>.toCurrencyRows(
    selectedCode: String,
    usualCode: String?,
    query: String,
): ImmutableList<SheetRowUiModel<String>> {
    val needle = query.trim()

    return sortedWith(
        compareBy<Currency> { currency ->
            when (currency.code) {
                selectedCode -> 0
                usualCode -> 1
                else -> 2
            }
        }.thenBy { it.name },
    ).filter {
        needle.isBlank() || it.code.contains(
            needle,
            ignoreCase = true,
        ) || it.name.contains(
            needle,
            ignoreCase = true,
        )
    }
        .map { currency ->
            SheetRowUiModel(
                key = currency.code,
                label = currency.name,
                selected = currency.code == selectedCode,
                leadingText = currency.code,
            )
        }.toImmutableList()
}
