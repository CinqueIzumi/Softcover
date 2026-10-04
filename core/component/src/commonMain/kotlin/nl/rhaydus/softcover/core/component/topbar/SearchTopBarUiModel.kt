package nl.rhaydus.softcover.core.component.topbar

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
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
 */
@Immutable
data class SearchTopBarUiModel(
    val query: String,
    val active: Boolean,
    val focused: Boolean,
    val isLoading: Boolean,
) {
    companion object : UiModelPreviews<SearchTopBarUiModel> {
        override val previews: ImmutableList<SearchTopBarUiModel> = persistentListOf(
            SearchTopBarUiModel(
                query = "",
                active = false,
                focused = false,
                isLoading = false,
            ),
            SearchTopBarUiModel(
                query = "",
                active = true,
                focused = true,
                isLoading = false,
            ),
            SearchTopBarUiModel(
                query = "Piranesi",
                active = true,
                focused = true,
                isLoading = true,
            ),
            SearchTopBarUiModel(
                query = "Piranesi",
                active = true,
                focused = false,
                isLoading = false,
            ),
        )
    }
}
