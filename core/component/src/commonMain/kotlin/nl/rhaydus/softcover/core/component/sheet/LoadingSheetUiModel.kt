package nl.rhaydus.softcover.core.component.sheet

import androidx.compose.runtime.Immutable

/**
 * Everything [LoadingSheet] renders: the [scaffold] chrome and how far along the wait is.
 *
 * @property progress Fraction complete, or `null` for an indeterminate wait. A non-null value that
 * reaches `1f` fires [LoadingSheetEvent.LoaderFinished] after a one-second settle, so the reader sees
 * the indicator complete before the sheet is dismissed out from under it.
 */
@Immutable
data class LoadingSheetUiModel(
    val scaffold: SheetScaffoldUiModel,
    val progress: Float?,
) : SheetModel<LoadingSheetUiModel> {
    override val closeRequested: Boolean get() = scaffold.closeRequested

    override fun requestingClose(): LoadingSheetUiModel = copy(scaffold = scaffold.requestingClose())
}
