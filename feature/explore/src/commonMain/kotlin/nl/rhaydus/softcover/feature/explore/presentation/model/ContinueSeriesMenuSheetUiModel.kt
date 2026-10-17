package nl.rhaydus.softcover.feature.explore.presentation.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import nl.rhaydus.softcover.core.component.sheet.SheetActionRowUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetModel
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel

/** The up-next menu sheet for [bookId]: its [scaffold] chrome over the dismiss [options]. */
@Immutable
internal data class ContinueSeriesMenuSheetUiModel(
    val bookId: Int,
    val scaffold: SheetScaffoldUiModel,
    val options: ImmutableList<SheetActionRowUiModel<ContinueSeriesDismissalKind>>,
) : SheetModel<ContinueSeriesMenuSheetUiModel> {
    override val closeRequested: Boolean get() = scaffold.closeRequested

    override fun requestingClose(): ContinueSeriesMenuSheetUiModel = copy(scaffold = scaffold.requestingClose())
}
