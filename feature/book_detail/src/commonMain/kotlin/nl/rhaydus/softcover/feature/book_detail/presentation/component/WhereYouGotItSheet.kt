package nl.rhaydus.softcover.feature.book_detail.presentation.component

import androidx.compose.runtime.Composable
import nl.rhaydus.softcover.core.component.field.TextFieldEvent
import nl.rhaydus.softcover.core.component.sheet.SheetRowEvent
import nl.rhaydus.softcover.core.component.sheet.SheetRowGroup
import nl.rhaydus.softcover.core.component.sheet.SheetScaffold
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.action.BookDetailAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnDismissWhereYouGotItSheetAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnSaveWhereYouGotItAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnWhereYouGotItPresetClickedAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnWhereYouGotItTextChangedAction
import nl.rhaydus.softcover.feature.book_detail.presentation.state.WhereYouGotItSheetUiModel

@Composable
internal fun WhereYouGotItSheet(
    model: WhereYouGotItSheetUiModel,
    runAction: (BookDetailAction) -> Unit,
) {
    SheetScaffold(
        model = model.scaffold,
        onEvent = { event ->
            when (event) {
                SheetScaffoldEvent.PrimaryClicked -> runAction(OnSaveWhereYouGotItAction())

                SheetScaffoldEvent.Dismissed -> runAction(OnDismissWhereYouGotItSheetAction())

                SheetScaffoldEvent.SecondaryClicked,
                SheetScaffoldEvent.HeaderActionClicked -> Unit
            }
        },
    ) {
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
}
