package nl.rhaydus.softcover.core.component.state

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon

/**
 * The copy an [EmptyState] renders in place of content a surface has nothing to draw for.
 */
@Immutable
data class EmptyStateUiModel(
    val title: String,
    val body: String? = null,
    val variant: EmptyStateVariant = EmptyStateVariant.Centered(),
) {
    companion object : UiModelPreviews<EmptyStateUiModel> {
        override val previews: ImmutableList<EmptyStateUiModel> = persistentListOf(
            EmptyStateUiModel(
                title = "An open page awaits",
                body = "Nothing is currently between your fingers. Find a title worth losing an evening to.",
                variant = EmptyStateVariant.Centered(
                    button = EmptyStateButtonUiModel(
                        label = "Find a book",
                        icon = SoftcoverIcon.Search,
                    ),
                ),
            ),
            EmptyStateUiModel(title = "Select a book to see its details."),
            EmptyStateUiModel(
                title = "No custom lists yet",
                body = "Create your first list below.",
                variant = EmptyStateVariant.Inline,
            ),
            EmptyStateUiModel(
                title = "Nothing on the shelf",
                body = "Whatever belongs here will turn up as soon as there is something to show.",
            ),
        )
    }
}
