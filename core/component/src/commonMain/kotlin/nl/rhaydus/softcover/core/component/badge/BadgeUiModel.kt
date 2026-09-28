package nl.rhaydus.softcover.core.component.badge

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
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
 */
@Immutable
data class BadgeUiModel(
    val label: String,
    val tone: BadgeTone,
    val size: BadgeSize = BadgeSize.Compact,
    val icon: SoftcoverIcon? = null,
) {
    companion object : UiModelPreviews<BadgeUiModel> {
        /** Per R5, one fixture per tone, one Regular, and one with a leading icon. */
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
                tone = BadgeTone.Neutral,
            ),
            BadgeUiModel(
                label = "Out Sep 2",
                tone = BadgeTone.Primary,
            ),
            BadgeUiModel(
                label = "TRACKING NOW",
                tone = BadgeTone.Neutral,
                size = BadgeSize.Regular,
            ),
            BadgeUiModel(
                label = "Owned",
                tone = BadgeTone.Scrim,
                size = BadgeSize.Regular,
                icon = SoftcoverIcon.Check,
            ),
            BadgeUiModel(
                label = "3 days",
                tone = BadgeTone.Primary,
                size = BadgeSize.Regular,
                icon = SoftcoverIcon.DateRange,
            ),
        )
    }
}
