package nl.rhaydus.softcover.feature.book_detail.presentation.component

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import nl.rhaydus.designsystem.editorial.component.EditorialSearchField
import nl.rhaydus.designsystem.motion.playDecorativeMotion
import nl.rhaydus.designsystem.theme.Spacing
import nl.rhaydus.softcover.core.component.field.SelectField
import nl.rhaydus.softcover.core.component.field.SelectFieldEvent
import nl.rhaydus.softcover.core.component.field.TextField
import nl.rhaydus.softcover.core.component.field.TextFieldEvent
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.component.sheet.LazySheetScaffold
import nl.rhaydus.softcover.core.component.sheet.SheetRowEvent
import nl.rhaydus.softcover.core.component.sheet.SheetRowGroup
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldEvent
import nl.rhaydus.softcover.core.component.sheet.sheetRowItems
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.feature.book_detail.presentation.action.BookDetailAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnDismissWhereYouGotItSheetAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnSaveWhereYouGotItAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnWhereYouGotItAmountChangedAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnWhereYouGotItCurrencyBackAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnWhereYouGotItCurrencyFieldClickedAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnWhereYouGotItCurrencyPickedAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnWhereYouGotItCurrencyQueryChangedAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnWhereYouGotItPresetClickedAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnWhereYouGotItTextChangedAction
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.WHERE_YOU_GOT_IT_AMOUNT_KEY
import nl.rhaydus.softcover.feature.book_detail.presentation.state.WhereYouGotItBodyUiModel
import nl.rhaydus.softcover.feature.book_detail.presentation.state.WhereYouGotItSheetUiModel

@Composable
internal fun WhereYouGotItSheet(
    model: WhereYouGotItSheetUiModel,
    runAction: (BookDetailAction) -> Unit,
) {
    val body = model.body
    val sizeSpec = MaterialTheme.motionScheme.defaultSpatialSpec<IntSize>()

    if (body is WhereYouGotItBodyUiModel.Currency) {
        NavigationBackHandler(
            state = rememberNavigationEventState(NavigationEventInfo.None),
            onBackCancelled = {},
            onBackCompleted = { runAction(OnWhereYouGotItCurrencyBackAction()) },
        )
    }

    LazySheetScaffold(
        model = model.scaffold,
        onEvent = { event ->
            when (event) {
                SheetScaffoldEvent.PrimaryClicked -> runAction(OnSaveWhereYouGotItAction())

                SheetScaffoldEvent.Dismissed -> runAction(OnDismissWhereYouGotItSheetAction())

                SheetScaffoldEvent.HeaderActionClicked -> runAction(OnWhereYouGotItCurrencyBackAction())

                SheetScaffoldEvent.SecondaryClicked -> Unit
            }
        },
        modifier = if (playDecorativeMotion()) Modifier.animateContentSize(animationSpec = sizeSpec) else Modifier,
        fillHeight = body is WhereYouGotItBodyUiModel.Currency,
        pinned = {
            if (body is WhereYouGotItBodyUiModel.Currency) {
                CurrencySearch(
                    model = body,
                    runAction = runAction,
                )
            }
        },
    ) {
        when (body) {
            is WhereYouGotItBodyUiModel.Choices -> choicesItems(
                model = body,
                runAction = runAction,
            )

            is WhereYouGotItBodyUiModel.Currency -> sheetRowItems(
                rows = body.rows,
                onEvent = { event ->
                    when (event) {
                        is SheetRowEvent.Selected -> runAction(OnWhereYouGotItCurrencyPickedAction(code = event.key))

                        is SheetRowEvent.InputChanged -> Unit
                    }
                },
            )
        }
    }
}

@Composable
private fun ColumnScope.CurrencySearch(
    model: WhereYouGotItBodyUiModel.Currency,
    runAction: (BookDetailAction) -> Unit,
) {
    EditorialSearchField(
        query = model.searchQuery,
        onQueryChange = { runAction(OnWhereYouGotItCurrencyQueryChangedAction(query = it)) },
        onClearClick = { runAction(OnWhereYouGotItCurrencyQueryChangedAction(query = "")) },
        searchIcon = drawableIconResource(
            icon = SoftcoverIcon.Search,
            contentDescription = "Search",
        ),
        clearIcon = drawableIconResource(
            icon = SoftcoverIcon.Close,
            contentDescription = "Clear search",
        ),
        placeholder = model.searchPlaceholder,
    )

    Spacer(modifier = Modifier.height(Spacing.md))
}

private fun LazyListScope.choicesItems(
    model: WhereYouGotItBodyUiModel.Choices,
    runAction: (BookDetailAction) -> Unit,
) {
    item(key = "presets") {
        SheetRowGroup(
            model = model.group,
            onEvent = { event ->
                when (event) {
                    is SheetRowEvent.Selected -> runAction(OnWhereYouGotItPresetClickedAction(key = event.key))

                    is SheetRowEvent.InputChanged -> when (val input = event.event) {
                        is TextFieldEvent.ValueChanged -> runAction(OnWhereYouGotItTextChangedAction(text = input.text))

                        is TextFieldEvent.Submitted,
                        is TextFieldEvent.TrailingClicked,
                        is TextFieldEvent.ShakeEnded -> Unit
                    }
                }
            },
        )
    }

    item(key = "price-header") {
        Column {
            Spacer(modifier = Modifier.height(Spacing.md))

            SectionHeader(model = model.priceHeader)

            Spacer(modifier = Modifier.height(Spacing.sm))
        }
    }

    item(key = WHERE_YOU_GOT_IT_AMOUNT_KEY) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
            verticalAlignment = Alignment.Top,
        ) {
            TextField(
                model = model.amount,
                onEvent = { event ->
                    when (event) {
                        is TextFieldEvent.ValueChanged -> runAction(
                            OnWhereYouGotItAmountChangedAction(text = event.text),
                        )

                        is TextFieldEvent.Submitted,
                        is TextFieldEvent.TrailingClicked,
                        is TextFieldEvent.ShakeEnded -> Unit
                    }
                },
                modifier = Modifier.weight(1f),
            )

            SelectField(
                model = model.currency,
                onEvent = { event ->
                    when (event) {
                        is SelectFieldEvent.Clicked -> runAction(OnWhereYouGotItCurrencyFieldClickedAction())
                    }
                },
                modifier = Modifier.width(104.dp),
            )
        }
    }

    item(key = "price-caption") {
        SoftcoverText(
            text = model.caption,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = Spacing.sm),
        )
    }
}
