package nl.rhaydus.softcover.showcase.live.verdict.model

import androidx.compose.runtime.Immutable
import nl.rhaydus.softcover.core.component.sheet.SheetModel
import nl.rhaydus.softcover.core.component.verdict.VerdictSheetUiModel

@Immutable
internal data class VerdictDemoUiModel(val sheet: VerdictSheetUiModel) : SheetModel<VerdictDemoUiModel> {
    override val closeRequested: Boolean get() = sheet.scaffold.closeRequested

    override fun requestingClose(): VerdictDemoUiModel = copy(
        sheet = sheet.copy(scaffold = sheet.scaffold.requestingClose()),
    )
}
