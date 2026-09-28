package nl.rhaydus.softcover.core.component.badge

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon

/**
 * Everything [CoverOverlay] draws on top of its `content`: the [Badge] to place in its corner,
 * which corner and pad it uses, and whether the content itself should render desaturated.
 *
 * @property badge The badge to overlay.
 * @property variant Which corner/pad the badge uses — see [CoverOverlayVariant].
 * @property grayscale Whether [CoverOverlay]'s `content` — the cover, never the badge — renders in
 * grayscale: what `DeadlineStatus.Expired` used to mean at the call site, a deadline that has expired
 * dims its cover so the eye reads "stopped" before it reads the badge's text.
 */
@Immutable
data class CoverOverlayUiModel(
    val badge: BadgeUiModel,
    val variant: CoverOverlayVariant,
    val grayscale: Boolean,
) {
    companion object : UiModelPreviews<CoverOverlayUiModel> {
        /** Per R5: both grayscale states on [CoverOverlayVariant.ReadingStatus], plus the other variant. */
        override val previews: ImmutableList<CoverOverlayUiModel> = persistentListOf(
            CoverOverlayUiModel(
                badge = BadgeUiModel(
                    label = "On track",
                    tone = BadgeTone.PrimaryContainer,
                ),
                variant = CoverOverlayVariant.ReadingStatus,
                grayscale = false,
            ),
            CoverOverlayUiModel(
                badge = BadgeUiModel(
                    label = "Expired",
                    tone = BadgeTone.Muted,
                ),
                variant = CoverOverlayVariant.ReadingStatus,
                grayscale = true,
            ),
            CoverOverlayUiModel(
                badge = BadgeUiModel(
                    label = "3 days",
                    tone = BadgeTone.Primary,
                    variant = BadgeVariant.GridCountdown,
                    icon = SoftcoverIcon.DateRange,
                ),
                variant = CoverOverlayVariant.LibraryCountdown,
                grayscale = false,
            ),
        )
    }
}
