package nl.rhaydus.softcover.core.component.state

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import nl.rhaydus.softcover.core.component.button.PillButtonContent
import nl.rhaydus.softcover.core.component.button.PillButtonSize
import nl.rhaydus.softcover.core.component.button.PillButtonTone
import nl.rhaydus.softcover.core.component.button.PillButtonUiModel
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon

/**
 * The copy an [EmptyState] renders in place of content a surface has nothing to draw for.
 *
 * [EmptyState] adds no gutter of its own: the caller pads a full-pane placement, or an in-flow one.
 */
@Immutable
data class EmptyStateUiModel(
    val title: String,
    val body: String? = null,
    val variant: EmptyStateVariant = EmptyStateVariant.Centered(),
) {
    companion object : UiModelPreviews<EmptyStateUiModel> {
        override val previews: ImmutableMap<String, EmptyStateUiModel> = persistentMapOf(
            "Between books" to EmptyStateUiModel(
                title = "Between books",
                body = "Your next one might already be on your Want to read shelf.",
                variant = EmptyStateVariant.Centered(
                    button = PillButtonUiModel(
                        key = "find-a-book",
                        content = PillButtonContent.Label(
                            text = "Find a book",
                            leadingIcon = SoftcoverIcon.Search,
                        ),
                        tone = PillButtonTone.Filled,
                        size = PillButtonSize.Large,
                    ),
                ),
            ),
            "Select a book to see its details." to EmptyStateUiModel(title = "Select a book to see its details."),
            "No custom lists yet" to EmptyStateUiModel(
                title = "No custom lists yet",
                body = "Create your first list below.",
                variant = EmptyStateVariant.Inline,
            ),
            "Nothing on the shelf" to EmptyStateUiModel(
                title = "Nothing on the shelf",
                body = "Whatever belongs here will turn up as soon as there is something to show.",
            ),
        )
    }
}
