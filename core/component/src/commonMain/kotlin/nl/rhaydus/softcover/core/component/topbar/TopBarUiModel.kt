package nl.rhaydus.softcover.core.component.topbar

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews

/**
 * Everything [TopBar] renders: the page's name and what the bar is sitting on.
 *
 * @property title Autosizes down within `titleLarge` and wraps to at most two lines, so a long page
 * name shrinks rather than truncating. `null` leaves the bar untitled — the book page shows no title
 * until its hero has scrolled away.
 */
@Immutable
data class TopBarUiModel(
    val title: String? = null,
    val surface: TopBarSurface = TopBarSurface.OPAQUE,
) {
    companion object : UiModelPreviews<TopBarUiModel> {
        override val previews: ImmutableMap<String, TopBarUiModel> = persistentMapOf(
            "With back" to TopBarUiModel(title = "Appearance"),
            "Long title, autosized" to TopBarUiModel(
                title = "A Title Long Enough That The Bar Has To Shrink It Across Two Lines",
            ),
            "Over cover art" to TopBarUiModel(
                surface = TopBarSurface.OVER_MEDIA,
            ),
        )
    }
}
