package nl.rhaydus.softcover.core.component.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews
import nl.rhaydus.softcover.core.component.generated.resources.Res
import nl.rhaydus.softcover.core.component.generated.resources.connectivity_offline_screen_body
import nl.rhaydus.softcover.core.component.generated.resources.connectivity_offline_screen_title
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import org.jetbrains.compose.resources.stringResource

/**
 * The copy an [EmptyState] renders in place of content a surface has nothing to draw for.
 *
 * @property button Rendered by [EmptyStateVariant.Centered] only; [EmptyStateVariant.Inline] drops it.
 */
@Immutable
data class EmptyStateUiModel(
    val title: String,
    val body: String? = null,
    val button: EmptyStateButtonUiModel? = null,
    val variant: EmptyStateVariant = EmptyStateVariant.Centered,
) {
    companion object : UiModelPreviews<EmptyStateUiModel> {
        override val previews: ImmutableList<EmptyStateUiModel> = persistentListOf(
            EmptyStateUiModel(
                title = "An open page awaits",
                body = "Nothing is currently between your fingers. Find a title worth losing an evening to.",
                button = EmptyStateButtonUiModel(
                    label = "Find a book",
                    icon = SoftcoverIcon.Search,
                ),
            ),
            EmptyStateUiModel(title = "Select a book to see its details."),
            EmptyStateUiModel(
                title = "No custom lists yet",
                body = "Create your first list below.",
                variant = EmptyStateVariant.Inline,
            ),
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
