package nl.rhaydus.softcover.feature.settings.presentation.screen

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.designsystem.editorial.component.EditorialSearchField
import nl.rhaydus.designsystem.layout.rememberBottomBarPadding
import nl.rhaydus.designsystem.theme.Spacing
import nl.rhaydus.softcover.core.component.header.PageMasthead
import nl.rhaydus.softcover.core.component.sheet.SheetRowEvent
import nl.rhaydus.softcover.core.component.sheet.sheetRowItems
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.feature.settings.presentation.action.CurrencySettingsAction
import nl.rhaydus.softcover.feature.settings.presentation.action.OnCurrencyQueryChangedAction
import nl.rhaydus.softcover.feature.settings.presentation.action.OnCurrencySelectedAction
import nl.rhaydus.softcover.feature.settings.presentation.state.CurrencySettingsUiState

@Composable
internal fun CurrencySettingsContent(
    state: CurrencySettingsUiState,
    runAction: (CurrencySettingsAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val bottomBarPadding = rememberBottomBarPadding()

    LazyColumn(modifier = modifier) {
        item {
            PageMasthead(model = state.masthead)

            Spacer(modifier = Modifier.height(Spacing.xl))
        }

        item {
            EditorialSearchField(
                query = state.query,
                onQueryChange = { runAction(OnCurrencyQueryChangedAction(query = it)) },
                onClearClick = { runAction(OnCurrencyQueryChangedAction(query = "")) },
                searchIcon = drawableIconResource(
                    icon = SoftcoverIcon.Search,
                    contentDescription = "Search",
                ),
                clearIcon = drawableIconResource(
                    icon = SoftcoverIcon.Close,
                    contentDescription = "Clear search",
                ),
                placeholder = state.searchPlaceholder,
            )

            Spacer(modifier = Modifier.height(Spacing.md))
        }

        sheetRowItems(
            rows = state.rows,
            onEvent = { event ->
                if (event is SheetRowEvent.Selected) {
                    runAction(OnCurrencySelectedAction(code = event.key))
                }
            },
        )

        item {
            Spacer(modifier = Modifier.height(Spacing.section + bottomBarPadding))
        }
    }
}
