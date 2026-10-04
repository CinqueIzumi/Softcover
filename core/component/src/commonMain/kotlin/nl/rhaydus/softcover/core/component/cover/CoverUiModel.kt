package nl.rhaydus.softcover.core.component.cover

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews

/**
 * Everything [Cover] renders: what to load, what to fall back to when nothing loads, and which
 * look ([CoverVariant]) applies.
 *
 * @property source Where to load the cover from, already resolved — a local file or a remote URL.
 * `null` means the resolution ladder (the mapper's job, not this component's) found nothing to load,
 * so [Cover] falls straight to its coverless rung without attempting a network request.
 * @property coverlessTitle The book/edition title to show as a text-only cover when [source] is
 * `null`, or a known [source] fails to decode. Required — not defaulted — so that a new cover surface
 * cannot silently render a blank tile: a cover slot is never blank. Pass `null` only for purely
 * decorative imagery that is never itself the content the user is looking at (e.g. a blurred backdrop)
 * — and always leave a comment at the *mapper's* call site explaining why a blank fallback is
 * acceptable there.
 * @property sharedTransitionKey The resolved shared-element key, or `null` for no transition.
 */
@Immutable
data class CoverUiModel(
    val source: CoverSource?,
    val coverlessTitle: String?,
    val variant: CoverVariant,
    val isLoading: Boolean = false,
    val sharedTransitionKey: String? = null,
) {
    companion object : UiModelPreviews<CoverUiModel> {
        /**
         * The [source]-present fixture uses [CoverSource.Local] rather than [CoverSource.Remote] —
         * per the gallery's network-independence rule, no fixture in this library may carry a live
         * URL, and a local path exercises the same success-rung anatomy without one.
         */
        override val previews: ImmutableMap<String, CoverUiModel> = persistentMapOf(
            "With cover art" to CoverUiModel(
                source = CoverSource.Local(
                    path = "/fixtures/piranesi-cover.jpg",
                    cacheKey = null,
                ),
                coverlessTitle = "Piranesi",
                variant = CoverVariant.Shelf,
            ),
            "Coverless, long title" to CoverUiModel(
                source = null,
                coverlessTitle = "The Very Long and Overwrought Title of a Book That Will Not Fit",
                variant = CoverVariant.Shelf,
            ),
            "Coverless, thumbnail-sized" to CoverUiModel(
                source = null,
                coverlessTitle = "The Name of the Wind",
                variant = CoverVariant.Flat,
            ),
            "Loading" to CoverUiModel(
                source = null,
                coverlessTitle = "The Dungeon Anarchist's Cookbook",
                variant = CoverVariant.Shelf,
                isLoading = true,
            ),
        )
    }
}
