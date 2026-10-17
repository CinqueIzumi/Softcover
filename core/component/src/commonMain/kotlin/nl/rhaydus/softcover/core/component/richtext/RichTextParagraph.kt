package nl.rhaydus.softcover.core.component.richtext

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

/** One block of a [RichTextUiModel] — the runs that make up a single paragraph, in order. */
@Immutable
data class RichTextParagraph(
    val runs: ImmutableList<RichTextRun>,
)
