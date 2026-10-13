package nl.rhaydus.softcover.core.component.richtext

import androidx.compose.runtime.Immutable

/**
 * A formatting mark of [type] covering the half-open range [[start], [end]) of the editor's plain
 * text.
 */
@Immutable
data class RichTextMark(
    val start: Int,
    val end: Int,
    val type: RichTextMarkType,
)
