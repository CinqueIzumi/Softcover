package nl.rhaydus.softcover.core.component.badge

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon

/**
 * Everything [Badge] renders: the label, the colour register it speaks in, which surface's pad
 * ([BadgeDimensions.forVariant]) applies, and an optional leading icon.
 *
 * @property label The badge's text, already resolved by the mapper — a deadline pace status label,
 * or a formatted release date such as "Out Sep 2" / "Arriving Sep 2". [Badge] never formats a date or
 * looks up a status word itself (§ 7.2 R4).
 * @property tone The colour register — see [BadgeTone].
 * @property variant Which surface's pad applies — see [BadgeVariant]. Defaults to
 * [BadgeVariant.Standard], the pad every badge but Explore's featured hero uses.
 * @property icon An optional leading icon, decorative — the label already announces what it marks.
 */
@Immutable
data class BadgeUiModel(
    val label: String,
    val tone: BadgeTone,
    val variant: BadgeVariant = BadgeVariant.Standard,
    val icon: SoftcoverIcon? = null,
) {
    companion object : UiModelPreviews<BadgeUiModel> {
        /**
         * Per R5, one fixture per tone, one per variant that changes the anatomy rather than only the
         * colour, and one with a leading icon.
         */
        override val previews: ImmutableList<BadgeUiModel> = persistentListOf(
            BadgeUiModel(
                label = "On track",
                tone = BadgeTone.PrimaryContainer,
            ),
            BadgeUiModel(
                label = "Behind",
                tone = BadgeTone.ErrorContainer,
            ),
            BadgeUiModel(
                label = "Expired",
                tone = BadgeTone.Muted,
            ),
            BadgeUiModel(
                label = "Out Sep 2",
                tone = BadgeTone.Primary,
            ),
            BadgeUiModel(
                label = "Arriving Sep 2",
                tone = BadgeTone.Primary,
                variant = BadgeVariant.FeaturedRelease,
            ),
            BadgeUiModel(
                label = "TRACKING NOW",
                tone = BadgeTone.Neutral,
                variant = BadgeVariant.TrackingTag,
            ),
            BadgeUiModel(
                label = "Owned",
                tone = BadgeTone.Scrim,
                variant = BadgeVariant.OwnedCover,
                icon = SoftcoverIcon.Check,
            ),
            BadgeUiModel(
                label = "3 days",
                tone = BadgeTone.Primary,
                variant = BadgeVariant.GridCountdown,
                icon = SoftcoverIcon.DateRange,
            ),
        )
    }
}
