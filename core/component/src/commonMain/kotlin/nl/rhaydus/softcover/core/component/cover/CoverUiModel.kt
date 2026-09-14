package nl.rhaydus.softcover.core.component.cover

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews

/**
 * Everything [Cover] renders: what to load, what to fall back to when nothing loads, and which
 * surface's treatment ([CoverDimensions.forVariant]) applies.
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
 * Resolved by the mapper (`component-contract.md` § 7.2 R7) — [Cover] never computes one, because a
 * component does not know which surface it is on.
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
         * Per R5, one fixture per anatomy branch rather than per variant — 21 near-identical tiles
         * would be noise in the gallery. This covers: a resolved [source] (the `SubcomposeAsyncImage`
         * rung), a coverless short title, a coverless title long enough that `TextAutoSize` must
         * shrink it, a coverless cover at a thumbnail-sized variant (so the single-initial degrade is
         * visible beside the full-title case), and the loading shimmer.
         *
         * The [source]-present fixture uses [CoverSource.Local] rather than [CoverSource.Remote] —
         * per the gallery's network-independence rule, no fixture in this library may carry a live
         * URL, and a local path exercises the same success-rung anatomy without one.
         */
        override val previews: ImmutableList<CoverUiModel> = persistentListOf(
            CoverUiModel(
                source = CoverSource.Local(
                    path = "/fixtures/piranesi-cover.jpg",
                    cacheKey = null,
                ),
                coverlessTitle = "Piranesi",
                variant = CoverVariant.LibraryShelfItem,
            ),
            CoverUiModel(
                source = null,
                coverlessTitle = "It",
                variant = CoverVariant.LibraryShelfItem,
            ),
            CoverUiModel(
                source = null,
                coverlessTitle = "The Very Long and Overwrought Title of a Book That Will Not Fit",
                variant = CoverVariant.LibraryShelfItem,
            ),
            CoverUiModel(
                source = null,
                coverlessTitle = "The Name of the Wind",
                variant = CoverVariant.EditionListRow,
            ),
            CoverUiModel(
                source = null,
                coverlessTitle = "The Dungeon Anarchist's Cookbook",
                variant = CoverVariant.LibraryShelfItem,
                isLoading = true,
            ),
        )
    }
}
