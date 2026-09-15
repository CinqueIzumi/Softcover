package nl.rhaydus.softcover.core.component.callout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.stringResource
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews
import nl.rhaydus.softcover.core.component.generated.resources.Res
import nl.rhaydus.softcover.core.component.generated.resources.connectivity_offline_banner

/**
 * Everything [Banner] renders: the line of copy, the register it speaks in, and whether it is
 * currently showing.
 *
 * @property visible Whether the banner is on screen. It rides on the model rather than being a second
 * parameter (§ 7.1 admits none) *and* rather than the caller wrapping the component in its own
 * `AnimatedVisibility`: the expand/shrink transition is part of the banner's anatomy, and a nullable
 * model could not survive its own exit animation.
 */
@Immutable
data class BannerUiModel(
    val message: String,
    val tone: BannerTone,
    val visible: Boolean,
) {
    companion object : UiModelPreviews<BannerUiModel> {
        override val previews: ImmutableList<BannerUiModel> = persistentListOf(
            BannerUiModel(
                message = "You're offline. Progress changes are saved and will sync when you reconnect.",
                tone = BannerTone.WARNING,
                visible = true,
            ),
        )
    }
}

/**
 * The offline notice, in the library's own words.
 *
 * The copy belongs to the component rather than to a feature — every surface that reports being
 * offline reports it identically — so the library owns the string and this factory resolves it.
 * Reading a string resource is a composition-scoped platform read, not a domain mapping, so it is
 * outside R9's reach (the same carve-out `ThemeMode.isDark()` sits in).
 */
@Composable
fun offlineBannerUiModel(visible: Boolean): BannerUiModel = BannerUiModel(
    message = stringResource(Res.string.connectivity_offline_banner),
    tone = BannerTone.WARNING,
    visible = visible,
)
