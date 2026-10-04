package nl.rhaydus.softcover.core.component.badge

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon

/**
 * Everything [CoverOverlay] draws on top of its `content`: the [Badge] to place in its corner,
 * which corner it uses, and whether the content itself should render desaturated.
 *
 * @property badge The badge to overlay. Its [BadgeUiModel.size] also sets the overlay's corner pad.
 * @property corner Which top corner the badge pins to.
 * @property grayscale Whether [CoverOverlay]'s `content` — the cover, never the badge — renders in
 * grayscale: what `DeadlineStatus.Expired` used to mean at the call site, a deadline that has expired
 * dims its cover so the eye reads "stopped" before it reads the badge's text.
 */
@Immutable
data class CoverOverlayUiModel(
    val badge: BadgeUiModel,
    val corner: CoverOverlayCorner,
    val grayscale: Boolean,
) {
    companion object : UiModelPreviews<CoverOverlayUiModel> {
        /** Per R5: both grayscale states on [CoverOverlayCorner.TopEnd], plus the other corner. */
        override val previews: ImmutableMap<String, CoverOverlayUiModel> = persistentMapOf(
            "On track" to CoverOverlayUiModel(
                badge = BadgeUiModel(
                    label = "On track",
                    tone = BadgeTone.PrimaryContainer,
                ),
                corner = CoverOverlayCorner.TopEnd,
                grayscale = false,
            ),
            "Expired (grayscale cover)" to CoverOverlayUiModel(
                badge = BadgeUiModel(
                    label = "Expired",
                    tone = BadgeTone.Neutral,
                ),
                corner = CoverOverlayCorner.TopEnd,
                grayscale = true,
            ),
            "Library countdown (top-start)" to CoverOverlayUiModel(
                badge = BadgeUiModel(
                    label = "3 days",
                    tone = BadgeTone.Primary,
                    size = BadgeSize.Regular,
                    icon = SoftcoverIcon.DateRange,
                ),
                corner = CoverOverlayCorner.TopStart,
                grayscale = false,
            ),
        )
    }
}
