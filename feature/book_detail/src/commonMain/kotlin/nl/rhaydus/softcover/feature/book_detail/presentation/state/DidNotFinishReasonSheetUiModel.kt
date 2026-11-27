package nl.rhaydus.softcover.feature.book_detail.presentation.state

import androidx.compose.runtime.Immutable
import nl.rhaydus.softcover.core.component.chip.ChipSet
import nl.rhaydus.softcover.core.component.field.TextFieldUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetModel
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel
import nl.rhaydus.softcover.core.domain.model.DidNotFinishReason

@Immutable
internal data class DidNotFinishReasonSheetUiModel(
    val scaffold: SheetScaffoldUiModel,
    val reasons: ChipSet<DidNotFinishReason>,
    val ownWordsHeader: SectionHeaderUiModel,
    val ownWords: TextFieldUiModel,
) : SheetModel<DidNotFinishReasonSheetUiModel> {
    override val closeRequested: Boolean get() = scaffold.closeRequested

    override fun requestingClose(): DidNotFinishReasonSheetUiModel = copy(scaffold = scaffold.requestingClose())
}
