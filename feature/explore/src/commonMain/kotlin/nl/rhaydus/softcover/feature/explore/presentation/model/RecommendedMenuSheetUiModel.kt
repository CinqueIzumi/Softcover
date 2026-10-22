package nl.rhaydus.softcover.feature.explore.presentation.model

import androidx.compose.runtime.Immutable
import nl.rhaydus.softcover.core.component.sheet.SheetActionRowUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetModel
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel

/** The recommended-rail menu sheet: its [scaffold] chrome over the one [hideOption], keyed by the book's id. */
@Immutable
internal data class RecommendedMenuSheetUiModel(
    val scaffold: SheetScaffoldUiModel,
    val hideOption: SheetActionRowUiModel<Int>,
) : SheetModel<RecommendedMenuSheetUiModel> {
    override val closeRequested: Boolean get() = scaffold.closeRequested

    override fun requestingClose(): RecommendedMenuSheetUiModel = copy(scaffold = scaffold.requestingClose())
}
