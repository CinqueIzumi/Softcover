package nl.rhaydus.softcover.core.component.badge

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews

/**
 * Everything [CoverOverlay] draws on top of its `content`: the [Badge] to place in the top-end
 * corner, and whether the content itself should render desaturated.
 *
 * @property badge The badge to overlay.
 * @property grayscale Whether the overlaid content renders in grayscale — what
 * `DeadlineStatus.Expired` used to mean at the call site: a deadline that has expired dims its cover
 * so the eye reads "stopped" before it reads the badge's text.
 */
@Immutable
data class CoverOverlayUiModel(
    val badge: BadgeUiModel,
    val grayscale: Boolean,
) {
    companion object : UiModelPreviews<CoverOverlayUiModel> {
        /** Per R5: an on-track badge over an untouched cover, and an expired badge over a grayscale one. */
        override val previews: ImmutableList<CoverOverlayUiModel> = persistentListOf(
            CoverOverlayUiModel(
                badge = BadgeUiModel(
                    label = "On track",
                    tone = BadgeTone.OnTrack,
                ),
                grayscale = false,
            ),
            CoverOverlayUiModel(
                badge = BadgeUiModel(
                    label = "Expired",
                    tone = BadgeTone.Expired,
                ),
                grayscale = true,
            ),
        )
    }
}
