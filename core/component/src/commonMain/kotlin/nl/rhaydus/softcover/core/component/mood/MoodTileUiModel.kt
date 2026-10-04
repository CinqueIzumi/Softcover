package nl.rhaydus.softcover.core.component.mood

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews

/**
 * The component fixes every measurement and the "MOOD" eyebrow; [title] and [countLabel] arrive resolved
 * and cased by the mapper.
 *
 * @property key Identifies the mood in [MoodTileEvent.Clicked].
 * @property isLoading Masks the tile's leaves in place and ignores presses.
 */
@Immutable
data class MoodTileUiModel(
    val key: Int,
    val title: String,
    val countLabel: String,
    val ink: MoodTileInk,
    val isLoading: Boolean = false,
) {
    companion object : UiModelPreviews<MoodTileUiModel> {
        override val previews: ImmutableList<MoodTileUiModel> = persistentListOf(
            MoodTileUiModel(
                key = 1,
                title = "Cosy & Comforting",
                countLabel = "820 books",
                ink = MoodTileInk.Cosy,
            ),
            MoodTileUiModel(
                key = 2,
                title = "Dread & Unease",
                countLabel = "1,540 books",
                ink = MoodTileInk.Dread,
            ),
            MoodTileUiModel(
                key = 3,
                title = "Adventurous",
                countLabel = "2,310 books",
                ink = MoodTileInk.Neutral,
            ),
            MoodTileUiModel(
                key = 4,
                title = "Heartbreaking",
                countLabel = "640 books",
                ink = MoodTileInk.HeartWrench,
            ),
            MoodTileUiModel(
                key = 1,
                title = "Cosy & Comforting",
                countLabel = "820 books",
                ink = MoodTileInk.Neutral,
                isLoading = true,
            ),
        )
    }
}
