package nl.rhaydus.softcover.core.component.sheet

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel

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

    companion object : UiModelPreviews<LoadingSheetUiModel> {
        private val scaffold = SheetScaffoldUiModel(
            header = SheetHeaderUiModel(
                section = SectionHeaderUiModel.Section(
                    eyebrow = "Setting up",
                    headline = "Getting your shelves ready",
                    description = "Pulling your library from Hardcover.",
                ),
            ),
            dismissible = false,
        )

        override val previews: ImmutableMap<String, LoadingSheetUiModel> = persistentMapOf(
            "Indeterminate" to LoadingSheetUiModel(
                scaffold = scaffold,
                progress = null,
            ),
            "Part way" to LoadingSheetUiModel(
                scaffold = scaffold,
                progress = 0.45f,
            ),
        )
    }
}
