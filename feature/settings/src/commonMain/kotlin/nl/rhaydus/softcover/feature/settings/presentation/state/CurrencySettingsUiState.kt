package nl.rhaydus.softcover.feature.settings.presentation.state

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.header.PageMastheadSubtitle
import nl.rhaydus.softcover.core.component.header.PageMastheadUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetRowUiModel
import nl.rhaydus.softcover.core.component.topbar.TopBarUiModel
import nl.rhaydus.softcover.core.domain.model.Currency
import nl.rhaydus.softcover.core.uibinding.money.toCurrencyRows
import nl.rhaydus.toad.UiState

internal data class CurrencySettingsUiState(
    val topBar: TopBarUiModel = TopBarUiModel(),
    val masthead: PageMastheadUiModel = PageMastheadUiModel(
        eyebrow = "Your private data",
        title = "Currency",
        subtitle = PageMastheadSubtitle.Text(
            "Prices you note start in this currency. Any book can still use another. Nothing is converted.",
        ),
    ),
    val searchPlaceholder: String = "Search currencies",
    val query: String = "",
    val currencies: ImmutableList<Currency> = persistentListOf(),
    val selectedCode: String = "",
    val rows: ImmutableList<SheetRowUiModel<String>> = persistentListOf(),
) : UiState

internal fun CurrencySettingsUiState.withRows(): CurrencySettingsUiState = copy(
    rows = currencies.toCurrencyRows(
        selectedCode = selectedCode,
        usualCode = null,
        query = query,
    ),
)
