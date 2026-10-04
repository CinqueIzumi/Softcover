package nl.rhaydus.softcover.core.component.richtext

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews

/**
 * Everything [RichTextFormattingToolbar] renders: which marks the whole current selection already
 * carries ([activeMarks]), and whether there is a selection to act on at all ([enabled]).
 *
 * Built from the editor's live selection, a composition-only value — the contract's documented unsettled case.
 */
@Immutable
data class RichTextFormattingToolbarUiModel(
    val activeMarks: ImmutableSet<RichTextMarkType>,
    val enabled: Boolean,
) {
    companion object : UiModelPreviews<RichTextFormattingToolbarUiModel> {
        override val previews: ImmutableList<RichTextFormattingToolbarUiModel> = persistentListOf(
            RichTextFormattingToolbarUiModel(
                activeMarks = persistentSetOf(),
                enabled = true,
            ),
            RichTextFormattingToolbarUiModel(
                activeMarks = persistentSetOf(RichTextMarkType.BOLD, RichTextMarkType.SPOILER),
                enabled = true,
            ),
            RichTextFormattingToolbarUiModel(
                activeMarks = persistentSetOf(),
                enabled = false,
            ),
        )
    }
}
