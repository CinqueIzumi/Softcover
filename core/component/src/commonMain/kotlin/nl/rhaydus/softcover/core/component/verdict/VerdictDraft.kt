package nl.rhaydus.softcover.core.component.verdict

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import nl.rhaydus.softcover.core.component.richtext.RichTextMark

/**
 * What a verdict sheet's reader can change, without the selection and composing range that edit nothing:
 * two drafts are equal exactly when saving either would persist the same verdict.
 */
@Immutable
data class VerdictDraft(
    val rating: Double?,
    val reviewText: String,
    val marks: ImmutableList<RichTextMark>,
    val spoilersChecked: Boolean,
)
