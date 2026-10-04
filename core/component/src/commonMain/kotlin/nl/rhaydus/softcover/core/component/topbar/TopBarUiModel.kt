package nl.rhaydus.softcover.core.component.topbar

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
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
        override val previews: ImmutableList<TopBarUiModel> = persistentListOf(
            TopBarUiModel(title = "Appearance"),
            TopBarUiModel(
                title = "A Title Long Enough That The Bar Has To Shrink It Across Two Lines",
            ),
            TopBarUiModel(
                surface = TopBarSurface.OVER_MEDIA,
            ),
        )
    }
}
