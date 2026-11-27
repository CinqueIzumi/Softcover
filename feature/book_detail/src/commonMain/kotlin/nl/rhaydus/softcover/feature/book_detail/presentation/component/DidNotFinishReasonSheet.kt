package nl.rhaydus.softcover.feature.book_detail.presentation.component

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.designsystem.theme.Spacing
import nl.rhaydus.softcover.core.component.chip.ChipEvent
import nl.rhaydus.softcover.core.component.chip.ChipFlow
import nl.rhaydus.softcover.core.component.field.TextField
import nl.rhaydus.softcover.core.component.field.TextFieldEvent
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.component.sheet.SheetScaffold
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.action.BookDetailAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnDidNotFinishOwnWordsChangedAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnDidNotFinishReasonToggledAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnDismissDidNotFinishReasonSheetAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnSaveDidNotFinishReasonsAction
import nl.rhaydus.softcover.feature.book_detail.presentation.state.DidNotFinishReasonSheetUiModel

@Composable
internal fun DidNotFinishReasonSheet(
    model: DidNotFinishReasonSheetUiModel,
    runAction: (BookDetailAction) -> Unit,
) {
    SheetScaffold(
        model = model.scaffold,
        onEvent = { event ->
            when (event) {
                SheetScaffoldEvent.PrimaryClicked -> runAction(OnSaveDidNotFinishReasonsAction())

                SheetScaffoldEvent.Dismissed -> runAction(OnDismissDidNotFinishReasonSheetAction())

                SheetScaffoldEvent.SecondaryClicked,
                SheetScaffoldEvent.HeaderActionClicked -> Unit
            }
        },
    ) {
        ChipFlow(
            set = model.reasons,
            onEvent = { event, reason ->
                when (event) {
                    is ChipEvent.Clicked -> runAction(OnDidNotFinishReasonToggledAction(reason = reason))

                    is ChipEvent.Dismissed,
                    is ChipEvent.SpoilerToggled -> Unit
                }
            },
        )

        Spacer(modifier = Modifier.height(Spacing.md))

        SectionHeader(model = model.ownWordsHeader)

        Spacer(modifier = Modifier.height(Spacing.sm))

        TextField(
            model = model.ownWords,
            onEvent = { event ->
                when (event) {
                    is TextFieldEvent.ValueChanged -> runAction(OnDidNotFinishOwnWordsChangedAction(text = event.text))

                    is TextFieldEvent.Submitted,
                    is TextFieldEvent.TrailingClicked,
                    is TextFieldEvent.ShakeEnded -> Unit
                }
            },
        )
    }
}
