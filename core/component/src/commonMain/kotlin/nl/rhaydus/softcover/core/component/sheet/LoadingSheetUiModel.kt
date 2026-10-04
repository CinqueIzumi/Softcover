package nl.rhaydus.softcover.core.component.sheet

import androidx.compose.runtime.Immutable

/**
 * Everything [LoadingSheet] renders: the [scaffold] chrome and how far along the wait is.
 *
 * @property progress Fraction complete, or `null` for an indeterminate wait. A non-null value that
 * reaches `1f` fires [LoadingSheetEvent.LoaderFinished] after a one-second settle, so the reader sees
 * the indicator complete before the sheet is dismissed out from under it.
 * @property isLoading Whether the sheet is showing at all. It rides on the model rather than the
 * caller conditionally composing [LoadingSheet], the same reasoning as
 * [nl.rhaydus.softcover.core.component.callout.BannerUiModel.visible].
 */
@Immutable
data class LoadingSheetUiModel(
    val scaffold: SheetScaffoldUiModel,
    val progress: Float?,
    val isLoading: Boolean,
)
