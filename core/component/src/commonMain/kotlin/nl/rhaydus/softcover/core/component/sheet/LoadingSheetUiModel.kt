package nl.rhaydus.softcover.core.component.sheet

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews

/**
 * Everything [LoadingSheet] renders: the editorial header copy and how far along the wait is.
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
    val eyebrow: String,
    val headline: String,
    val progress: Float?,
    val isLoading: Boolean,
    val description: String? = null,
) {
    companion object : UiModelPreviews<LoadingSheetUiModel> {
        override val previews: ImmutableList<LoadingSheetUiModel> = persistentListOf(
            LoadingSheetUiModel(
                eyebrow = "Setting up",
                headline = "Pulling your library together.",
                description = "Depending on its size, this might take a moment.",
                progress = 0.4f,
                isLoading = true,
            ),
            LoadingSheetUiModel(
                eyebrow = "Setting up",
                headline = "Pulling your library together.",
                description = null,
                progress = null,
                isLoading = true,
            ),
        )
    }
}
