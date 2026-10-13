package nl.rhaydus.softcover.feature.scan.presentation.state

import nl.rhaydus.softcover.core.component.sheet.SheetModel
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel

internal data class UnknownIsbnSheetUiModel(
    val isbn: String,
    val scaffold: SheetScaffoldUiModel,
) : SheetModel<UnknownIsbnSheetUiModel> {
    override val closeRequested: Boolean get() = scaffold.closeRequested

    override fun requestingClose(): UnknownIsbnSheetUiModel = copy(scaffold = scaffold.requestingClose())
}
