package nl.rhaydus.softcover.core.component.progress

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews

/**
 * A determinate progress bar.
 *
 * [fraction] is drawn as given; the mapper clamps it to 0..1.
 */
@Immutable
data class ProgressBarUiModel(
    val fraction: Float,
    val isLoading: Boolean = false,
) {
    companion object : UiModelPreviews<ProgressBarUiModel> {
        override val previews: ImmutableList<ProgressBarUiModel> = persistentListOf(
            ProgressBarUiModel(fraction = 0.42f),
            ProgressBarUiModel(fraction = 0f),
            ProgressBarUiModel(fraction = 1f),
            ProgressBarUiModel(
                fraction = 0f,
                isLoading = true,
            ),
        )
    }
}
