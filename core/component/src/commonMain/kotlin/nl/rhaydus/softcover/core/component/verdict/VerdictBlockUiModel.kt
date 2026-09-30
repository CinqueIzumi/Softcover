package nl.rhaydus.softcover.core.component.verdict

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews
import nl.rhaydus.softcover.core.component.richtext.RichTextUiModel

/**
 * Everything [VerdictBlock] renders. [rating] and [review] are independent: the block draws whichever
 * has content, and the empty invitation when neither does. A blank [review] counts as none.
 */
@Immutable
data class VerdictBlockUiModel(
    val rating: Double?,
    val review: RichTextUiModel?,
    val hasSpoilers: Boolean,
) {
    companion object : UiModelPreviews<VerdictBlockUiModel> {
        /** One fixture per branch the block renders: empty, rating only, review only, and both with the spoiler note. */
        override val previews: ImmutableList<VerdictBlockUiModel> = persistentListOf(
            VerdictBlockUiModel(
                rating = null,
                review = null,
                hasSpoilers = false,
            ),
            VerdictBlockUiModel(
                rating = 4.0,
                review = null,
                hasSpoilers = false,
            ),
            VerdictBlockUiModel(
                rating = null,
                review = RichTextUiModel.previews.first(),
                hasSpoilers = false,
            ),
            VerdictBlockUiModel(
                rating = 4.5,
                review = RichTextUiModel.previews[2],
                hasSpoilers = true,
            ),
        )
    }
}
