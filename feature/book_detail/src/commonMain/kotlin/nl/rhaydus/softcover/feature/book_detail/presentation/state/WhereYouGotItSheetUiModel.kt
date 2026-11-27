package nl.rhaydus.softcover.feature.book_detail.presentation.state

import androidx.compose.runtime.Immutable
import nl.rhaydus.softcover.core.component.sheet.SheetModel
import nl.rhaydus.softcover.core.component.sheet.SheetRowGroupUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel

@Immutable
internal data class WhereYouGotItSheetUiModel(
    val scaffold: SheetScaffoldUiModel,
    val group: SheetRowGroupUiModel<String>,
) : SheetModel<WhereYouGotItSheetUiModel> {
    override val closeRequested: Boolean get() = scaffold.closeRequested

    override fun requestingClose(): WhereYouGotItSheetUiModel = copy(scaffold = scaffold.requestingClose())
}
