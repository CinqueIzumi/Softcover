package nl.rhaydus.softcover.core.component.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.stringResource
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews
import nl.rhaydus.softcover.core.component.generated.resources.Res
import nl.rhaydus.softcover.core.component.generated.resources.connectivity_offline_screen_body
import nl.rhaydus.softcover.core.component.generated.resources.connectivity_offline_screen_title

/**
 * The copy an [EmptyState] renders in place of content a screen could not show: a headline naming
 * the situation and a body saying what would change it.
 */
@Immutable
data class EmptyStateUiModel(
    val title: String,
    val body: String,
) {
    companion object : UiModelPreviews<EmptyStateUiModel> {
        override val previews: ImmutableList<EmptyStateUiModel> = persistentListOf(
            EmptyStateUiModel(
                title = "No internet connection",
                body = "This screen needs internet to load. Reconnect to try again.",
            ),
        )
    }
}

/**
 * The offline screen's copy, in the library's own words — the [EmptyState] counterpart to
 * [nl.rhaydus.softcover.core.component.callout.offlineBannerUiModel], and owned by the library for
 * the same reason: every screen that cannot load without a connection says this identically.
 */
@Composable
fun offlineEmptyStateUiModel(): EmptyStateUiModel = EmptyStateUiModel(
    title = stringResource(Res.string.connectivity_offline_screen_title),
    body = stringResource(Res.string.connectivity_offline_screen_body),
)
