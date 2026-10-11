package nl.rhaydus.softcover.core.component.topbar

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews

/**
 * Everything [SearchTopBar] renders.
 *
 * @property active True whenever the chrome is not the plain resting feed — focused, loading, or
 * showing results. It grows the pill's primary border and reveals the clear (×). The caller derives
 * it once off its own state (Explore's `SearchTopBarCollector`) rather than this model re-deriving
 * "resting feed or not" every frame.
 * @property focused **The caller's** search state, which the platform field follows. It is never the
 * other way round: the field's platform focus dies with its composition while the caller's state
 * outlives it, so re-entering a screen with a search still active must not yank the keyboard open.
 * @property eyebrow The pull-to-refresh label above the row; null draws the bar as the row alone.
 */
@Immutable
data class SearchTopBarUiModel(
    val query: String,
    val active: Boolean,
    val focused: Boolean,
    val isLoading: Boolean,
    val eyebrow: SearchTopBarEyebrowUiModel? = null,
) {
    companion object : UiModelPreviews<SearchTopBarUiModel> {
        override val previews: ImmutableMap<String, SearchTopBarUiModel> = persistentMapOf(
            "Resting" to SearchTopBarUiModel(
                query = "",
                active = false,
                focused = false,
                isLoading = false,
            ),
            "Focused, empty" to SearchTopBarUiModel(
                query = "",
                active = true,
                focused = true,
                isLoading = false,
            ),
            "Searching" to SearchTopBarUiModel(
                query = "Piranesi",
                active = true,
                focused = true,
                isLoading = true,
            ),
            "Results, unfocused" to SearchTopBarUiModel(
                query = "Piranesi",
                active = true,
                focused = false,
                isLoading = false,
            ),
            "With eyebrow" to SearchTopBarUiModel(
                query = "",
                active = false,
                focused = false,
                isLoading = false,
                eyebrow = SearchTopBarEyebrowUiModel(
                    text = "Discover",
                    refreshingText = "Finding new shelves…",
                    isRefreshing = false,
                ),
            ),
        )
    }
}
