package nl.rhaydus.softcover.core.component.verdict

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews
import nl.rhaydus.softcover.core.component.richtext.RichTextUiModel

/** What [VerdictBlock] renders: the invitation to rate ([Empty]) or the reader's verdict ([Filled]). */
@Immutable
sealed interface VerdictBlockUiModel {
    /** No rating and no review yet. */
    data object Empty : VerdictBlockUiModel

    /**
     * A verdict with content. At least one of [rating] and [review] is non-null, and a non-null
     * [review] is never blank — the mapper returns [Empty] otherwise.
     */
    data class Filled(
        val rating: Double?,
        val review: RichTextUiModel?,
        val hasSpoilers: Boolean,
    ) : VerdictBlockUiModel

    companion object : UiModelPreviews<VerdictBlockUiModel> {
        /** One fixture per branch the block renders: empty, rating only, review only, and both with the spoiler note. */
        override val previews: ImmutableList<VerdictBlockUiModel> = persistentListOf(
            Empty,
            Filled(
                rating = 4.0,
                review = null,
                hasSpoilers = false,
            ),
            Filled(
                rating = null,
                review = RichTextUiModel.previews.first(),
                hasSpoilers = false,
            ),
            Filled(
                rating = 4.5,
                review = RichTextUiModel.previews[2],
                hasSpoilers = true,
            ),
        )
    }
}
