package nl.rhaydus.softcover.core.component.topbar

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews

/**
 * Everything [TopBar] renders: the page's name, an optional second line under it, whether the screen
 * leads back out, and what the bar is sitting on.
 *
 * @property title Autosizes down within `titleLarge` and wraps to at most two lines, so a long page
 * name shrinks rather than truncating. An empty string is legitimate — the book page shows no title
 * until its hero has scrolled away.
 */
@Immutable
data class TopBarUiModel(
    val title: String,
    val subtitle: String? = null,
    val navigation: TopBarNavigation = TopBarNavigation.None,
    val surface: TopBarSurface = TopBarSurface.OPAQUE,
) {
    companion object : UiModelPreviews<TopBarUiModel> {
        override val previews: ImmutableList<TopBarUiModel> = persistentListOf(
            TopBarUiModel(title = "Appearance"),
            TopBarUiModel(
                title = "About",
                navigation = TopBarNavigation.Back,
            ),
            TopBarUiModel(
                title = "Hidden suggestions",
                subtitle = "Books you told us to stop showing",
                navigation = TopBarNavigation.Back,
            ),
            TopBarUiModel(
                title = "A Title Long Enough That The Bar Has To Shrink It Across Two Lines",
                navigation = TopBarNavigation.Back,
            ),
            TopBarUiModel(
                title = "",
                navigation = TopBarNavigation.Back,
                surface = TopBarSurface.OVER_MEDIA,
            ),
        )
    }
}
