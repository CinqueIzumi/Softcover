package nl.rhaydus.softcover.feature.explore.presentation.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel

/** The up-next menu sheet for one book: its [scaffold] chrome over the dismiss [options]. */
@Immutable
internal data class ContinueSeriesMenuSheetUiModel(
    val scaffold: SheetScaffoldUiModel,
    val options: ImmutableList<ContinueSeriesMenuOptionUiModel>,
)
