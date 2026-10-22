package nl.rhaydus.softcover.feature.library.presentation.state

import androidx.compose.runtime.Immutable
import nl.rhaydus.softcover.core.component.sheet.SheetActionRowUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetModel
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel

/** The overflow menu of one recommended-rail book: its [scaffold] chrome over the single [hideOption] row. */
@Immutable
internal data class RecommendedMenuSheetUiModel(
    val scaffold: SheetScaffoldUiModel,
    val hideOption: SheetActionRowUiModel<PendingRecommendedHide>,
) : SheetModel<RecommendedMenuSheetUiModel> {
    override val closeRequested: Boolean get() = scaffold.closeRequested

    override fun requestingClose(): RecommendedMenuSheetUiModel = copy(scaffold = scaffold.requestingClose())
}
