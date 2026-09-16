package nl.rhaydus.softcover.core.component.badge

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews

/**
 * Everything [Badge] renders: the label, the colour register it speaks in, and which surface's pad
 * ([BadgeDimensions.forVariant]) applies.
 *
 * @property label The badge's text, already resolved by the mapper — a deadline pace status label,
 * or a formatted release date such as "Out Sep 2" / "Arriving Sep 2". [Badge] never formats a date or
 * looks up a status word itself (§ 7.2 R4).
 * @property tone The colour register — see [BadgeTone].
 * @property variant Which surface's pad applies — see [BadgeVariant]. Defaults to
 * [BadgeVariant.Standard], the pad every badge but Explore's featured hero uses.
 */
@Immutable
data class BadgeUiModel(
    val label: String,
    val tone: BadgeTone,
    val variant: BadgeVariant = BadgeVariant.Standard,
) {
    companion object : UiModelPreviews<BadgeUiModel> {
        /**
         * Per R5, one fixture per anatomy branch: the four tones, plus the one variant that changes
         * the pad rather than the colour.
         */
        override val previews: ImmutableList<BadgeUiModel> = persistentListOf(
            BadgeUiModel(
                label = "On track",
                tone = BadgeTone.OnTrack,
            ),
            BadgeUiModel(
                label = "Behind",
                tone = BadgeTone.Behind,
            ),
            BadgeUiModel(
                label = "Expired",
                tone = BadgeTone.Expired,
            ),
            BadgeUiModel(
                label = "Out Sep 2",
                tone = BadgeTone.Release,
            ),
            BadgeUiModel(
                label = "Arriving Sep 2",
                tone = BadgeTone.Release,
                variant = BadgeVariant.FeaturedRelease,
            ),
        )
    }
}
