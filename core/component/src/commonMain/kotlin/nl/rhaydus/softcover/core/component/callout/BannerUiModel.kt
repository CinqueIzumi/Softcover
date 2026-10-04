package nl.rhaydus.softcover.core.component.callout

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews

/**
 * The content of a [Banner] and whether it is currently showing.
 *
 * @property visible Whether the banner is on screen. It rides on the model rather than the caller
 * wrapping the component in its own `AnimatedVisibility`: the expand/shrink transition is part of the
 * banner's anatomy, and a nullable model could not survive its own exit animation.
 */
@Immutable
data class BannerUiModel(
    val message: String,
    val variant: BannerVariant,
    val visible: Boolean,
) {
    companion object : UiModelPreviews<BannerUiModel> {
        override val previews: ImmutableList<BannerUiModel> = persistentListOf(
            BannerUiModel(
                message = "You're offline. Progress changes are saved and will sync when you reconnect.",
                variant = BannerVariant.Status,
                visible = true,
            ),
            BannerUiModel(
                message = "Read 24 pages today to stay on pace.",
                variant = BannerVariant.Editorial,
                visible = true,
            ),
        )
    }
}
