package nl.rhaydus.softcover.core.component.topbar

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews

/** The placeholder shown when nothing has been typed yet. */
private const val DEFAULT_PLACEHOLDER = "Search books, authors…"

/**
 * Everything [SearchTopBar] renders.
 *
 * @property active True whenever the chrome is not the plain resting feed — focused, loading, or
 * showing results. It grows the pill's primary border and reveals the clear (×).
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
    val placeholder: String = DEFAULT_PLACEHOLDER,
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
        )
    }
}
