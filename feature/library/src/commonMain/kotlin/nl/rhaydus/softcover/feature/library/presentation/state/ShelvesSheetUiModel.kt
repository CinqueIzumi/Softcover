package nl.rhaydus.softcover.feature.library.presentation.state

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import nl.rhaydus.softcover.core.component.sheet.SheetModel
import nl.rhaydus.softcover.core.component.sheet.SheetRowGroupUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetRowUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel

@Immutable
internal data class ShelvesSheetUiModel(
    val scaffold: SheetScaffoldUiModel,
    val group: SheetRowGroupUiModel<String>,
) : SheetModel<ShelvesSheetUiModel> {
    override val closeRequested: Boolean get() = scaffold.closeRequested

    override fun requestingClose(): ShelvesSheetUiModel = copy(scaffold = scaffold.requestingClose())
}

internal fun shelvesSheetUiModel(rows: ImmutableList<SheetRowUiModel<String>>): ShelvesSheetUiModel =
    ShelvesSheetUiModel(
        scaffold = shelvesSheetScaffold(),
        group = SheetRowGroupUiModel(
            rows = rows,
            dismissesSheet = false,
        ),
    )
