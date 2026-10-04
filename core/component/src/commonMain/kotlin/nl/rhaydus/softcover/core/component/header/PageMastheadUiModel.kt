package nl.rhaydus.softcover.core.component.header

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews

/**
 * Everything [PageMasthead] renders: the page's own title, an optional no-bar eyebrow above it, and
 * an optional subtitle below — sized by [size] for the surface it names.
 */
@Immutable
data class PageMastheadUiModel(
    val title: String,
    val eyebrow: String? = null,
    val subtitle: String? = null,
    val size: PageMastheadSize = PageMastheadSize.Regular,
) {
    companion object : UiModelPreviews<PageMastheadUiModel> {
        /** One fixture per [PageMastheadSize], plus one with an eyebrow. */
        override val previews: ImmutableMap<String, PageMastheadUiModel> = persistentMapOf(
            "Regular, with eyebrow" to PageMastheadUiModel(
                title = "Roadmap",
                eyebrow = "Personalise",
                subtitle = "What we're building next, and roughly when.",
            ),
            "Regular" to PageMastheadUiModel(
                title = "Settings",
                subtitle = "Tune Softcover to match how you read.",
            ),
            "Compact" to PageMastheadUiModel(
                title = "Settings",
                subtitle = "Tune Softcover to match how you read.",
                size = PageMastheadSize.Compact,
            ),
        )
    }
}
