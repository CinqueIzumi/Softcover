package nl.rhaydus.softcover.core.component.badge

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon

/**
 * Everything [Badge] renders: the label, the colour register it speaks in, which pad tier it uses,
 * and an optional leading icon.
 *
 * @property label The badge's text, already resolved by the mapper — a deadline pace status label,
 * or a formatted release date such as "Out Sep 2" / "Arriving Sep 2". [Badge] never formats a date or
 * looks up a status word itself (§ 7.2 R4).
 * @property tone The colour register — see [BadgeTone].
 * @property size The pad tier — see [BadgeSize]. Defaults to [BadgeSize.Compact].
 * @property icon An optional leading icon, decorative — the label already announces what it marks.
 * @property contentDescription What a screen reader speaks instead of the bare label; null = the label is read as-is.
 */
@Immutable
data class BadgeUiModel(
    val label: String,
    val tone: BadgeTone,
    val size: BadgeSize = BadgeSize.Compact,
    val icon: SoftcoverIcon? = null,
    val contentDescription: String? = null,
) {
    companion object : UiModelPreviews<BadgeUiModel> {
        /** Per R5, one fixture per tone, one Regular, and one with a leading icon. */
        override val previews: ImmutableMap<String, BadgeUiModel> = persistentMapOf(
            "On track" to BadgeUiModel(
                label = "On track",
                tone = BadgeTone.PrimaryContainer,
            ),
            "Behind" to BadgeUiModel(
                label = "Behind",
                tone = BadgeTone.ErrorContainer,
            ),
            "Rating" to BadgeUiModel(
                label = "4.0",
                tone = BadgeTone.TertiaryContainer,
                size = BadgeSize.Regular,
                icon = SoftcoverIcon.StarFilled,
                contentDescription = "Rated 4.0",
            ),
            "Expired" to BadgeUiModel(
                label = "Expired",
                tone = BadgeTone.Neutral,
            ),
            "Release" to BadgeUiModel(
                label = "Out Sep 2",
                tone = BadgeTone.Primary,
            ),
            "TRACKING NOW, regular" to BadgeUiModel(
                label = "TRACKING NOW",
                tone = BadgeTone.Neutral,
                size = BadgeSize.Regular,
            ),
            "Owned, with icon" to BadgeUiModel(
                label = "Owned",
                tone = BadgeTone.Scrim,
                size = BadgeSize.Regular,
                icon = SoftcoverIcon.Check,
            ),
            "3 days, with icon" to BadgeUiModel(
                label = "3 days",
                tone = BadgeTone.Primary,
                size = BadgeSize.Regular,
                icon = SoftcoverIcon.DateRange,
            ),
        )
    }
}
