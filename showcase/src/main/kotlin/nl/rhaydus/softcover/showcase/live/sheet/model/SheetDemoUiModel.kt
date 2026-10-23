package nl.rhaydus.softcover.showcase.live.sheet.model

import androidx.compose.runtime.Immutable
import nl.rhaydus.softcover.core.component.sheet.LoadingSheetUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetModel
import nl.rhaydus.softcover.core.component.sheet.SheetRowGroupUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel

@Immutable
internal sealed interface SheetDemoUiModel : SheetModel<SheetDemoUiModel> {
    data class Scrolling(
        val scaffold: SheetScaffoldUiModel,
        val body: String,
    ) : SheetDemoUiModel {
        override val closeRequested: Boolean get() = scaffold.closeRequested

        override fun requestingClose(): SheetDemoUiModel = copy(scaffold = scaffold.requestingClose())
    }

    data class Pinned(
        val scaffold: SheetScaffoldUiModel,
        val rows: SheetRowGroupUiModel<String>,
    ) : SheetDemoUiModel {
        override val closeRequested: Boolean get() = scaffold.closeRequested

        override fun requestingClose(): SheetDemoUiModel = copy(scaffold = scaffold.requestingClose())
    }

    data class Loading(val sheet: LoadingSheetUiModel) : SheetDemoUiModel {
        override val closeRequested: Boolean get() = sheet.closeRequested

        override fun requestingClose(): SheetDemoUiModel = copy(sheet = sheet.requestingClose())
    }
}
